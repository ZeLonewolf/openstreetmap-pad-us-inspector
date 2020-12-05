package com.streetferret.opus.location;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import com.streetferret.opus.osmdb.OSMProtectedAreaLookupResults;

public class LocationDatabase {
	private static final long UNMATCHED = -1;
	private ArrayListValuedHashMap<Long, LocationMatch> matchMap = new ArrayListValuedHashMap<>();
	private ArrayListValuedHashMap<String, Long> nameMap = new ArrayListValuedHashMap<>();
	private Map<Long, PADDetails> padDetails = new HashMap<>();

	private ArrayListValuedHashMap<String, LocationMatch> osmUnmatched;

	public ArrayListValuedHashMap<Long, LocationMatch> getMatchMap() {
		return matchMap;
	}

	public ArrayListValuedHashMap<String, Long> getNameMap() {
		return nameMap;
	}

	public Map<Long, PADDetails> getPadDetails() {
		return padDetails;
	}

	public void merge(OSMProtectedAreaLookupResults osmDB) {
		osmUnmatched = osmDB.getNameIndex();
		osmUnmatched.asMap().entrySet().forEach(e -> {

			String name = e.getKey();
			Collection<LocationMatch> osmMatches = e.getValue();

			if (nameMap.containsKey(name)) {
				Collection<Long> padIDs = nameMap.get(name);
				padIDs.forEach(padID -> osmMatches.forEach(lm -> matchMap.put(padID, lm)));
			} else {
				nameMap.put(name, UNMATCHED);
			}
		});
	}

	public ArrayListValuedHashMap<String, LocationMatch> getOsmUnmatched() {
		return osmUnmatched;
	}
}
