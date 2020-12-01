package com.streetferret.opus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.streetferret.opus.osmdb.OSMProtectedAreaRecord;
import com.streetferret.opus.osmdb.StateProtectedAreaDatabase;

public class OverpassLookup {

	static StateProtectedAreaDatabase downloadOSMProtectedAreas(String state) throws IOException {
		InputStream inputStream = OverpassLookup.class.getResourceAsStream("/state_protected_area_lookup.overpass");
		String opNameTemplate = StringUtil.readFromInputStream(inputStream);
		opNameTemplate = opNameTemplate.replace("$AREA", StateGeocode.LIST.getProperty(state).trim());

		String areasLookup = RestUtil.queryOverpass(opNameTemplate);

		String[] areas = areasLookup.split("\n");

		StateProtectedAreaDatabase statePad = new StateProtectedAreaDatabase();

		for (String area : areas) {
			String[] data = area.split("\\|");
			OSMProtectedAreaRecord r = new OSMProtectedAreaRecord();

			populateString(data, 0, r::setType);
			populateLong(data, 1, r::setId);
			populateString(data, 2, r::setName);
			populateString(data, 3, r::setProtectClass);
			populateString(data, 4, r::setIucnLevel);

			statePad.getRecords().add(r);
		}
		statePad.index();
		return statePad;
	}

	private static void populateString(String[] data, int index, Consumer<String> c) {
		if (data.length > index) {
			c.accept(data[index]);
		}
	}

	private static void populateLong(String[] data, int index, Consumer<Long> c) {
		if (data.length > index) {
			c.accept(Long.valueOf(data[index]));
		}
	}

	static void populateTaggedUnlistedAreas(String state,
			SortedMap<String, List<ProtectedAreaTagging>> protectedAreaMap, StateProtectedAreaDatabase osmData)
			throws IOException {

		Set<String> unlisted = new TreeSet<>();

		osmData.getRecords().forEach(record -> {
			if (!protectedAreaMap.containsKey(record.getName())) {
				unlisted.add(StringUtil.cleanAreaName(record.getName()));
			}
		});

		for (String name : unlisted) {
			ProtectedAreaTagging t = new ProtectedAreaTagging();
			t.setIucnClass("Not Found");
			ProtectedAreaMapLoader.storeTagging(protectedAreaMap, name, t);
		}
	}

	static String overpassProtectedAreaLookupHTML(String name, StateProtectedAreaDatabase db) throws IOException {

		List<OSMProtectedAreaRecord> records = db.lookupByName(name);

		if (records == null) {
			return "";
		}

		StringBuilder objectList = new StringBuilder();
		objectList.append("<ul>");

		int count = 0;

		for (OSMProtectedAreaRecord record : records) {

			++count;

			if (count == 4 && records.size() > 4) {
				int unlistedCount = records.size() - 3;

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

			String protectClass = record.getProtectClass() == null ? "_" : record.getProtectClass();

			item = item.replace("$OBJ_ID", String.valueOf(record.getId())).replace("$PROTECT_CLASS", protectClass);

			objectList.append(item);
		}

		return objectList.toString();
	}
}
