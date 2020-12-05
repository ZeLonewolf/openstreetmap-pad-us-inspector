package com.streetferret.opus.location;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.streetferret.opus.StringUtil;

public class LocationMatch {
	private long id;
	private String type; // relation, way, node
	private Map<String, String> tags = new HashMap<>();
	private double matchExact = 0;
	private double matchInside = 0;
	private double matchOverlap = 0;
	private boolean matched = true;

	private static final List<String> CONFLATION_KEYS = Arrays.asList("protect_class", "iucn_level", "leisure",
			"landuse", "natural");

	private static NumberFormat pctFormat = NumberFormat.getPercentInstance();

	static {
		pctFormat.setMaximumFractionDigits(1);
		pctFormat.setMinimumFractionDigits(1);
	}

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
		String rawName = tags.get("name");
		if (rawName == null || rawName.isBlank()) {
			return "(unnamed)";
		}
		return StringUtil.cleanAreaName(rawName);
	}

	public boolean hasTag(String key) {
		return tags.containsKey(key);
	}

	public String getConflationKey() {
		if (hasTag("boundary") && tags.get("boundary").equals("national_park")) {
			return "boundary";
		}
		for (String key : CONFLATION_KEYS) {
			if (hasTag(key)) {
				return key;
			}
		}
		return null;
	}

	public boolean noAreaConflation() {
		return matchExact < 0.75 && matchInside < 0.95 && matchOverlap < 0.95;
	}

	public String getConflationNote(String name) {
		List<String> conflateParts = new ArrayList<>();
		if (name.equals(getName())) {
			conflateParts.add("exact name");
		}
		String numConflation = getGeoConflation();
		if (!numConflation.isBlank()) {
			conflateParts.add(numConflation);
		}
		return conflateParts.stream().collect(Collectors.joining(", "));
	}

	private String getGeoConflation() {
		if (matchExact >= 0.75) {
			String pct = pctFormat.format(matchExact);
			return pct + " identical";
		}
		if (matchInside >= 0.95) {
			String pct = pctFormat.format(matchInside);
			return pct + " overlaps";
		}
		if (matchOverlap >= 0.95) {
			String pct = pctFormat.format(matchOverlap);
			return pct + " within";
		}
		return "";
	}

	public boolean isMatched() {
		return matched;
	}

	public void setMatched(boolean matched) {
		this.matched = matched;
	}
}
