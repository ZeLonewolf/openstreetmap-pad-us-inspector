package com.streetferret.opus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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

public class OPUSInspect {
	private static SortedMap<String, List<ProtectedAreaTagging>> protectedAreaMap = new TreeMap<>();
	private static SortedMap<String, Integer> stateProtectedAreaCount = new TreeMap<>();

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

			System.out.println("Parsing " + state);

			protectedAreaMap.clear();
			parseState(state);

			Iterator<List<ProtectedAreaTagging>> iterator = protectedAreaMap.values().iterator();
			while (iterator.hasNext()) {

				List<ProtectedAreaTagging> tags = iterator.next();
				tags.removeIf(tag -> !IUCN.VALID_IUCN.contains(tag.getIucnClass()));
				if (tags.isEmpty()) {
					iterator.remove();
				}

			}

			WikiGenerator.generateWiki(state, protectedAreaMap);
			stateProtectedAreaCount.put(state, protectedAreaMap.size());
		}

		WikiGenerator.generateSummaryWiki(stateProtectedAreaCount);
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
					name = nextEvent.asCharacters().toString().replace("_", " ").replace("*", "");
					name = name.replaceAll("([A-Za-z])\\s*,\\s*([A-Za-z])", "$1, $2");
					ProtectedAreaTagging tagging = new ProtectedAreaTagging();
					populateFromDescription(reader, tagging);
					populateCoordinates(reader, tagging);
					if (protectedAreaMap.containsKey(name)) {
						protectedAreaMap.get(name).add(tagging);
					} else {
						List<ProtectedAreaTagging> tagList = new ArrayList<>();
						tagList.add(tagging);
						protectedAreaMap.put(name, tagList);
					}
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
					String rawDescription = nextEvent.asCharacters().toString();
					tagging.setIucnClass(parseField(rawDescription, "IUCN_Cat"));
					tagging.setAccess(parseField(rawDescription, "d_Access"));
					tagging.setOwnership(parseField(rawDescription, "d_Own_Type"));
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