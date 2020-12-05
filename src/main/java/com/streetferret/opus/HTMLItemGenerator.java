package com.streetferret.opus;

import java.util.Collection;

import com.streetferret.opus.location.LocationMatch;

public class HTMLItemGenerator {

	private static final int MAX_ROWS = 5;

	public static String matchingItemHTML(String name, Collection<LocationMatch> osmMatches) {

		if (osmMatches.isEmpty()) {
			return "";
		}

		StringBuilder objectList = new StringBuilder();
		objectList.append("<ul>");

		int count = 0;

		for (LocationMatch record : osmMatches) {

			String conflation = record.getConflationNote(name);
			if (conflation == null) {
				continue;
			}

			++count;

			if (count == MAX_ROWS && osmMatches.size() > MAX_ROWS) {
				int unlistedCount = osmMatches.size() - (MAX_ROWS - 1);

				objectList.append("<li>plus <b>");
				objectList.append(unlistedCount);
				objectList.append("</b> more</li>");
				objectList.append("</ul>");
				return objectList.toString();
			}

			String item = "";

			switch (record.getType()) {
			case "relation":
				item = HTMLGenerator.T_REL_ITEM;
				break;
			case "way":
				item = HTMLGenerator.T_WAY_ITEM;
				break;
			case "node":
				item = HTMLGenerator.T_NODE_ITEM;
				break;
			}

			String key = record.getConflationKey();
			if (key == null) {
				continue;
			}

			String value = record.getTags().get(key);

			String itemName = record.getName();
			if (itemName.isEmpty()) {
				itemName = "(unnamed)";
			}

			item = item.replace("$OBJ_ID", String.valueOf(record.getId()))
				.replace("$KEY", key)
				.replace("$VALUE", value)
				.replace("$NAME", itemName)
				.replace("$CONFLATE_NOTE", conflation);

			objectList.append(item);
		}

		return objectList.toString();
	}
}
