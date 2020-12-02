package com.streetferret.opus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.streetferret.opus.osmdb.OSMProtectedAreaRecord;

public class ProtectedAreaConflation {
	private List<ProtectedAreaTagging> padAreas = new ArrayList<>();
	private List<OSMProtectedAreaRecord> osmAreas = new ArrayList<>();

	public List<ProtectedAreaTagging> getPadAreas() {
		return padAreas;
	}

	public void setPadAreas(List<ProtectedAreaTagging> padAreas) {
		this.padAreas = padAreas;
	}

	public List<OSMProtectedAreaRecord> getDistinctOsmAreas() {
		HashMap<Long, OSMProtectedAreaRecord> osmMap = new HashMap<>();
		osmAreas.forEach(a -> osmMap.put(a.getId(), a));
		return osmAreas.stream().collect(Collectors.toList());
	}

	public List<OSMProtectedAreaRecord> getOsmAreas() {
		return osmAreas;
	}

	public void setOsmAreas(List<OSMProtectedAreaRecord> osmAreas) {
		this.osmAreas = osmAreas;
	}
}
