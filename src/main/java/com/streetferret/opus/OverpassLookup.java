package com.streetferret.opus;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streetferret.opus.location.LocationMatch;
import com.streetferret.opus.model.OSMElement;
import com.streetferret.opus.model.OSMResponse;
import com.streetferret.opus.osmdb.OSMProtectedAreaLookupResults;

public class OverpassLookup {

	static OSMProtectedAreaLookupResults downloadOSMProtectedAreas(String state) throws IOException {
		return downloadOSM(state, "state_protected_area_lookup.overpass");
	}

	static OSMProtectedAreaLookupResults downloadOSM(String state, String query) throws IOException {
		InputStream inputStream = OverpassLookup.class.getResourceAsStream("/" + query);
		String opNameTemplate = StringUtil.readFromInputStream(inputStream);

		String areaID = StateGeocode.ABBREV_TO_AREA_ID.getProperty(state);
		if (areaID == null) {
			System.err.println("Missing area ID mapping for " + state);
			System.exit(-1);
		}
		opNameTemplate = opNameTemplate.replace("$AREA", areaID.trim());

		String areasLookup = RestUtil.queryOverpass(opNameTemplate);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

		OSMResponse areas = mapper.readValue(areasLookup, OSMResponse.class);
		OSMProtectedAreaLookupResults statePad = new OSMProtectedAreaLookupResults();

		for (OSMElement area : areas.getElements()) {
			LocationMatch r = new LocationMatch();

			r.setId(area.getId());
			r.setType(area.getType().toString());
			r.setTags(area.getTags());
			r.setMatched(false);

			statePad.getRecords().add(r);
		}

		statePad.index();
		return statePad;
	}

}