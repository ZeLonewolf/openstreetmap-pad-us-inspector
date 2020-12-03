package com.streetferret.opus.osmdb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.streetferret.opus.StringUtil;
import com.streetferret.opus.model.OSMBounds;

public class OSMProtectedAreaRecord {
	private String type;
	private long id;
	private OSMBounds bounds;
	private String conflationNote = "";
	private Map<String, String> tags;

	private static final List<String> CONFLATION_KEYS = Arrays.asList("protect_class", "iucn_level", "leisure",
			"landuse", "natural");

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		String name = getTag("name");
		return name == null ? "(unnamed)" : StringUtil.cleanAreaName(name);
	}

	public OSMBounds getBounds() {
		return bounds;
	}

	public void setBounds(OSMBounds bounds) {
		this.bounds = bounds;
	}

	public String getConflationNote() {
		if (conflationNote.isEmpty()) {
			return "";
		}
		return "[" + conflationNote + "]";
	}

	public void setConflationNote(String conflationNote) {
		this.conflationNote = conflationNote;
	}

	public String getTag(String key) {
		return tags.get(key);
	}

	public boolean hasTag(String key) {
		return tags.containsKey(key);
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public String getConflationKey() {
		for(String key: CONFLATION_KEYS) {
			if(hasTag(key)) {
				return key;
			}
		}
		return null;
	}
}
