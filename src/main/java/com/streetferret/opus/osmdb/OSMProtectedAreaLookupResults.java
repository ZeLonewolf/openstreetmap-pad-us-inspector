package com.streetferret.opus.osmdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import com.streetferret.opus.location.LocationDatabase;
import com.streetferret.opus.location.LocationMatch;

public class OSMProtectedAreaLookupResults {

	private List<LocationMatch> records = new ArrayList<>();

	private ArrayListValuedHashMap<String, LocationMatch> nameIndex = new ArrayListValuedHashMap<>();

	public List<LocationMatch> getRecords() {
		return records;
	}

	public void setRecords(List<LocationMatch> records) {
		this.records = records;
	}

	public void index() {
		nameIndex.clear();
		records.forEach(r -> nameIndex.put(r.getName(), r));
	}

	public ArrayListValuedHashMap<String, LocationMatch> getNameIndex() {
		return nameIndex;
	}

	public void removeByID(long id) {
		Iterator<LocationMatch> it = records.iterator();
		while (it.hasNext()) {
			LocationMatch rec = it.next();
			if (rec.getId() == id) {
				it.remove();
			}
		}
	}

	public void removeRecordsNamed(List<String> list) {
		new TreeSet<String>(list).forEach(this::removeRecordNamed);
	}

	public void removeRecordNamed(String rec) {
		List<LocationMatch> removedRecords = nameIndex.remove(rec);
		records.removeAll(removedRecords);
	}

	public void removeAssociatedAreas(LocationDatabase db) {
		db.getMatchMap()
			.entries()
			.stream()
			.map(e -> e.getValue())
			.filter(LocationMatch::noAreaConflation)
			.map(LocationMatch::getId)
			.forEach(this::removeByID);
		index();
	}
}