package com.streetferret.opus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.streetferret.opus.location.LocationMatch;
import com.streetferret.opus.location.LocationMatchSorter;

public class HTMLItemGenerator {

	private static final int MAX_ROWS = 5;

	private static final LocationMatchSorter SORTER = new LocationMatchSorter();

	public static String matchingItemHTML(String name, Collection<LocationMatch> osmMatches, boolean showConflateNote) {

		if (osmMatches.isEmpty()) {
			return "";
		}

		StringBuilder objectList = new StringBuilder();
		objectList.append("<ul>");

		int count = 0;

		List<LocationMatch> matches = new ArrayList<>(osmMatches);
		Collections.sort(matches, SORTER.reversed());

		for (LocationMatch record : matches) {

			if (record.noAreaConflation()) {
//				continue;
			}

			String conflationNote = showConflateNote ? record.getConflationNote(name) : "";

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
				.replace("$CONFLATE_NOTE", conflationNote);

			objectList.append(item);
		}

		return objectList.toString();
	}
}
