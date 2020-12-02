package com.streetferret.opus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streetferret.opus.model.OSMElement;
import com.streetferret.opus.model.OSMResponse;
import com.streetferret.opus.osmdb.OSMProtectedAreaRecord;
import com.streetferret.opus.osmdb.StateProtectedAreaDatabase;

public class OverpassLookup {

	static StateProtectedAreaDatabase downloadOSMProtectedAreas(String state) throws IOException {
		InputStream inputStream = OverpassLookup.class.getResourceAsStream("/state_protected_area_lookup.overpass");
		String opNameTemplate = StringUtil.readFromInputStream(inputStream);
		opNameTemplate = opNameTemplate.replace("$AREA", StateGeocode.LIST.getProperty(state).trim());

		String areasLookup = RestUtil.queryOverpass(opNameTemplate);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

		OSMResponse areas = mapper.readValue(areasLookup, OSMResponse.class);
		StateProtectedAreaDatabase statePad = new StateProtectedAreaDatabase();

		for (OSMElement area : areas.getElements()) {
			OSMProtectedAreaRecord r = new OSMProtectedAreaRecord();

			r.setId(area.getId());
			r.setType(area.getType().toString());

			Map<String, String> tags = area.getTags();
			r.setName(tags.get("name"));
			r.setIucnLevel(tags.get("iucn_level"));
			r.setProtectClass(tags.get("protect_class"));
			r.setBounds(area.getBounds());

			statePad.getRecords().add(r);
		}

		statePad.index();
		return statePad;
	}

	static void populateTaggedUnlistedAreas(String state, SortedMap<String, ProtectedAreaConflation> protectedAreaMap,
			StateProtectedAreaDatabase osmData) throws IOException {

		osmData.getNameIndex().entrySet().forEach(e -> {
			ProtectedAreaConflation c = new ProtectedAreaConflation();
			ProtectedAreaTagging t = new ProtectedAreaTagging();
			String name = e.getKey();

			t.setName(name);
			t.setIucnClass("Not Found");

			c.setOsmAreas(e.getValue());
			c.setPadAreas(Arrays.asList(t));

			protectedAreaMap.put(name, c);
		});
	}

	public static String overpassProtectedAreaLookupHTML(List<OSMProtectedAreaRecord> records) throws IOException {

		if (records.isEmpty()) {
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
