package com.streetferret.opus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OverpassLookup {

	static String overpassProtectedAreaLookup(String name) throws IOException {

		InputStream inputStream = OverpassLookup.class.getResourceAsStream("/lookup_by_name.overpass");
		String opNameTemplate = StringUtil.readFromInputStream(inputStream);
		opNameTemplate = opNameTemplate.replace("$NAME", name);

		String nameLookup = RestUtil.queryOverpass(opNameTemplate);

		String[] rows = nameLookup.split("\n");

		if (rows.length == 0) {
			return "";
		}

		StringBuilder objectList = new StringBuilder();

		for (String row : rows) {
			String[] objectData = row.split("\\|");

			if (objectData.length >= 4) {

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

}
