package com.streetferret.opus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class OverpassLookup {

	static String overpassProtectedAreaLookup(String name, String state) throws IOException {

		InputStream inputStream = OverpassLookup.class.getResourceAsStream("/lookup_by_name.overpass");
		String opNameTemplate = StringUtil.readFromInputStream(inputStream);
		opNameTemplate = opNameTemplate.replace("$NAME", name);
		opNameTemplate = opNameTemplate.replace("$AREA", StateGeocode.LIST.getProperty(state).trim());

		String nameLookup = RestUtil.queryOverpass(opNameTemplate);

		String[] rows = nameLookup.split("\n");

		if (rows.length == 0) {
			return "";
		}

		StringBuilder objectList = new StringBuilder();

		int count = 0;

		for (String row : rows) {
			String[] objectData = row.split("\\|");

			if (objectData.length >= 4) {

				++count;

				if (count == 4 && rows.length > 4) {

					int unlistedCount = rows.length - 3;

					objectList.append("\n* plus '''");
					objectList.append(unlistedCount);
					objectList.append("''' more");
					return objectList.toString();

				}

				objectList.append("\n* {{");
				objectList.append(objectData[0]);
				objectList.append("|");
				objectList.append(objectData[1]);
				objectList.append("}}");

				List<String> iucnTags = new ArrayList<>();

				if (!objectData[3].isEmpty()) {
					iucnTags.add("{{tag|protect_class|" + objectData[3] + "}}");
				}
				if (objectData.length >= 5 && !objectData[4].isEmpty()) {
					iucnTags.add("{{tag|iucn_level|" + objectData[3] + "}}");
				}

				if (!iucnTags.isEmpty()) {
					objectList.append(" (");
					objectList.append(iucnTags.stream().collect(Collectors.joining(" + ")));
					objectList.append(")");
				}

				objectList.append("\n");
			}
		}

		return objectList.toString();
	}

	static void populateTaggedUnlistedAreas(String state,
			SortedMap<String, List<ProtectedAreaTagging>> protectedAreaMap) throws IOException {

		InputStream inputStream = OverpassLookup.class.getResourceAsStream("/state_protected_area_lookup.overpass");
		String opNameTemplate = StringUtil.readFromInputStream(inputStream);
		opNameTemplate = opNameTemplate.replace("$AREA", StateGeocode.LIST.getProperty(state).trim());

		String areasLookup = RestUtil.queryOverpass(opNameTemplate);

		String[] areas = areasLookup.split("\n");

		Set<String> unlisted = new TreeSet<>();

		for (String area : areas) {
			String[] objectData = area.split("\\|");

			if (objectData.length >= 4) {
				if (!protectedAreaMap.containsKey(objectData[2])) {
					unlisted.add(StringUtil.cleanAreaName(objectData[2]));
				}
			}
		}

		for (String name : unlisted) {
			ProtectedAreaTagging t = new ProtectedAreaTagging();
			t.setIucnClass("Not Found");
			ProtectedAreaMapLoader.storeTagging(protectedAreaMap, name, t);
		}

	}

}
