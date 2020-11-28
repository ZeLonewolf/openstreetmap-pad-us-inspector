package com.streetferret.opus;

import java.io.File;
import java.io.FileInputStream;
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
	private static SortedMap<String, ProtectedAreaTagging> protectedAreaMap = new TreeMap<>();

	public static void main(String... args) throws Exception {

		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLEventReader reader = xmlInputFactory
				.createXMLEventReader(new FileInputStream("download" + File.separator + "RI.kml"));

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

		WikiGenerator.generateWiki("RI", protectedAreaMap);
	}

	private static void extractPlacemarkData(XMLEventReader reader) throws Exception {
		String name = "";
		String iucn = "";

		while (reader.hasNext()) {
			XMLEvent nextEvent = reader.nextEvent();
			if (nextEvent.isStartElement()) {
				StartElement startElement = nextEvent.asStartElement();
				switch (startElement.getName().getLocalPart()) {
				case "name":
					nextEvent = reader.nextEvent();
					name = nextEvent.asCharacters().toString();
				}
			}
			if (nextEvent.isEndElement()) {
				EndElement endElement = nextEvent.asEndElement();
				switch (endElement.getName().getLocalPart()) {
				case "Placemark":
					iucn = extractIUCNFromDescription(reader);
					ProtectedAreaTagging tagging = new ProtectedAreaTagging();
					tagging.setIucnClass(iucn);
					protectedAreaMap.put(name, tagging);
					return;
				}
			}
		}
	}

	private static String extractIUCNFromDescription(XMLEventReader reader) throws Exception {
		while (reader.hasNext()) {
			XMLEvent nextEvent = reader.nextEvent();
			if (nextEvent.isStartElement()) {
				StartElement startElement = nextEvent.asStartElement();
				switch (startElement.getName().getLocalPart()) {
				case "description":
					nextEvent = reader.nextEvent();
					String rawDescription = nextEvent.asCharacters().toString();
					return parseIUCN(rawDescription);
				}
			}
		}
		return null;
	}

	private static String parseIUCN(String rawDescription) {

		Pattern p = Pattern.compile("IUCN_Cat(.|\\n)*?>(.*?)<", Pattern.MULTILINE);
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