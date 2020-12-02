package com.streetferret.opus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.streetferret.opus.osmdb.StateProtectedAreaDatabase;

public class OPUSInspect {

	private static final SortedMap<String, ProtectedAreaConflation> protectedAreaMap = new TreeMap<>();

	public static void main(String... args) throws Exception {

		if ("-parse".equals(args[0])) {
			parse(args[1]);
		}
		if ("-download".equals(args[0])) {
			download();
		}
	}

	private static void download() throws IOException {
		PADUSDownloader.downloadPADStates();
	}

	private static void parse(String overpassURL) throws Exception {

		RestUtil.OVERPASS_API = overpassURL;

		File kmls = Paths.get("download", "kml").toFile();
		String[] kmlPaths = kmls.list();

		for (String kml : kmlPaths) {
			String state = kml.replace(".kml", "");

			protectedAreaMap.clear();

			parseState(state);

			Iterator<ProtectedAreaConflation> iterator = protectedAreaMap.values().iterator();

			while (iterator.hasNext()) {
				ProtectedAreaConflation conflation = iterator.next();

				List<ProtectedAreaTagging> tags = conflation.getPadAreas();
				if (!IUCN.hasValidAreas(tags)) {
					iterator.remove();
				} else {
					IUCN.sanitizeInvalidAreas(tags);
				}
			}

			StateProtectedAreaDatabase db = OverpassLookup.downloadOSMProtectedAreas(state);
			System.out.println("Downloaded protected areas for " + state);

			StateProtectedAreaDatabase dbParks = OverpassLookup.downloadOSMLeisurePark(state);
			System.out.println("Downloaded leisure=park for " + state);

			StateProtectedAreaDatabase dbNatureReserve = OverpassLookup.downloadOSMLeisureNature(state);
			System.out.println("Downloaded leisure=nature_reserve for " + state);

			StateProtectedAreaDatabase dbRecGround= OverpassLookup.downloadOSMLeisureNature(state);
			System.out.println("Downloaded leisure=recreation_ground for " + state);

			Conflator.conflateByName(protectedAreaMap, dbParks, "leisure_park");
			Conflator.conflateByName(protectedAreaMap, dbNatureReserve, "nature_reserve");
			Conflator.conflateByName(protectedAreaMap, dbRecGround, "recreation_ground");
			Conflator.conflateByName(protectedAreaMap, db);
			// Need to de-dupe conflation

			OverpassLookup.populateTaggedUnlistedAreas(state, protectedAreaMap, db);
			HTMLGenerator.generateHTML(state, protectedAreaMap, db);
		}
	}

	private static void parseState(String state) throws Exception {
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

		File kmlFile = Paths.get("download", "kml", state + ".kml").toFile();

		XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(kmlFile));

		while (reader.hasNext()) {
			XMLEvent nextEvent = reader.nextEvent();
			if (nextEvent.isStartElement()) {
				StartElement startElement = nextEvent.asStartElement();
				switch (startElement.getName().getLocalPart()) {
				case "Placemark":
					extractPlacemarkData(reader);
				}
			}
		}

	}

	private static void extractPlacemarkData(XMLEventReader reader) throws Exception {
		String name = "";

		while (reader.hasNext()) {
			XMLEvent nextEvent = reader.nextEvent();
			if (nextEvent.isStartElement()) {
				StartElement startElement = nextEvent.asStartElement();
				switch (startElement.getName().getLocalPart()) {
				case "name":
					nextEvent = reader.nextEvent();
					name = StringUtil.cleanAreaName(nextEvent.asCharacters().toString());

					ProtectedAreaTagging tagging = new ProtectedAreaTagging();
					tagging.setName(name);
					populateFromDescription(reader, tagging);
					populateCoordinates(reader, tagging);
					ProtectedAreaMapLoader.storeTagging(protectedAreaMap, name, tagging);
					return;
				}
			}
			if (nextEvent.isEndElement()) {
				EndElement endElement = nextEvent.asEndElement();
				switch (endElement.getName().getLocalPart()) {
				case "Placemark":
					return;
				}
			}
		}
	}

	private static void populateFromDescription(XMLEventReader reader, ProtectedAreaTagging tagging) throws Exception {
		while (reader.hasNext()) {
			XMLEvent nextEvent = reader.nextEvent();
			if (nextEvent.isStartElement()) {
				StartElement startElement = nextEvent.asStartElement();
				switch (startElement.getName().getLocalPart()) {
				case "description":
					nextEvent = reader.nextEvent();

					StringBuilder rawText = new StringBuilder();
					while (nextEvent.isCharacters()) {
						rawText.append(nextEvent.asCharacters().getData());
						nextEvent = reader.nextEvent();
					}

					String rawDescription = rawText.toString();

					tagging.setIucnClass(parseField(rawDescription, "IUCN_Cat"));
					tagging.setAccess(parseField(rawDescription, "d_Access"));
					tagging.setOwnership(parseField(rawDescription, "d_Own_Type"));
					tagging.setOwner(parseField(rawDescription, "Loc_Own"));
					tagging.setOperator(parseField(rawDescription, "Loc_Mang"));
					return;
				}
			}
		}
	}

	private static void populateCoordinates(XMLEventReader reader, ProtectedAreaTagging tagging) throws Exception {
		while (reader.hasNext()) {
			XMLEvent nextEvent = reader.nextEvent();
			if (nextEvent.isStartElement()) {
				StartElement startElement = nextEvent.asStartElement();
				switch (startElement.getName().getLocalPart()) {
				case "coordinates":
					nextEvent = reader.nextEvent();
					String rawCoords = nextEvent.asCharacters().toString().trim();
					String[] coordGroups = rawCoords.split("\\s");

					double minLat = 90;
					double minLon = 180;
					double maxLat = -90;
					double maxLon = -180;

					for (String coords : coordGroups) {
						String[] coordParts = coords.split(",");
						if (coordParts.length < 2) {
							continue;
						}
						double lon = Double.parseDouble(coordParts[0]);
						double lat = Double.parseDouble(coordParts[1]);

						if (lat > maxLat) {
							maxLat = lat;
						}
						if (lon > maxLon) {
							maxLon = lon;
						}
						if (lat < minLat) {
							minLat = lat;
						}
						if (lon < minLon) {
							minLon = lon;
						}
					}

					tagging.setMinLat(minLat);
					tagging.setMaxLat(maxLat);
					tagging.setMinLon(minLon);
					tagging.setMaxLon(maxLon);

					return;
				}
			}
		}
	}

	private static String parseField(String rawDescription, String key) {

		Pattern p = Pattern.compile(">" + key + "<(.|\\n)*?>(.*?)<", Pattern.MULTILINE);
		Matcher m = p.matcher(rawDescription);
		m.find();

		try {
			return m.group(2);
		} catch (Exception e) {
			return "Unlisted";
		}

//		System.exit(0);
	}

}