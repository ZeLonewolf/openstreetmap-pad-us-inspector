package com.streetferret.opus.osmdb;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class StateProtectedAreaDatabase {

	private List<OSMProtectedAreaRecord> records = new ArrayList<>();

	private TreeMap<String, List<OSMProtectedAreaRecord>> nameIndex = new TreeMap<>();

	public List<OSMProtectedAreaRecord> getRecords() {
		return records;
	}

	public void setRecords(List<OSMProtectedAreaRecord> records) {
		this.records = records;
	}

	public void index() {
		records.forEach(r -> {
			if (nameIndex.containsKey(r.getName())) {
				nameIndex.get(r.getName()).add(r);
			} else {
				List<OSMProtectedAreaRecord> areaList = new ArrayList<>();
				areaList.add(r);
				nameIndex.put(r.getName(), areaList);
			}
		});
	}

	public TreeMap<String, List<OSMProtectedAreaRecord>> getNameIndex() {
		return nameIndex;
	}

	public void removeRecordsNamed(List<String> list) {
		new TreeSet<String>(list).forEach(this::removeRecordNamed);
	}

	public void removeRecordNamed(String rec) {
		List<OSMProtectedAreaRecord> removedRecords = nameIndex.remove(rec);
		records.removeAll(removedRecords);
	}
}