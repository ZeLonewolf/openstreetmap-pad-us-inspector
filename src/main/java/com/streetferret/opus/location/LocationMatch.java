package com.streetferret.opus.location;

import java.util.HashMap;
import java.util.Map;

public class LocationMatch {
	private long id;
	private String type; // relation, way, node
	private String name;
	private Map<String, String> tags = new HashMap<>();
	private double matchExact = 0;
	private double matchInside = 0;
	private double matchOverlap = 0;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public double getMatchExact() {
		return matchExact;
	}

	public void setMatchExact(double matchExact) {
		this.matchExact = matchExact;
	}

	public double getMatchInside() {
		return matchInside;
	}

	public void setMatchInside(double matchInside) {
		this.matchInside = matchInside;
	}

	public double getMatchOverlap() {
		return matchOverlap;
	}

	public void setMatchOverlap(double matchOverlap) {
		this.matchOverlap = matchOverlap;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
