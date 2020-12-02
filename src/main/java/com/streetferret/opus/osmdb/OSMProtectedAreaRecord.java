package com.streetferret.opus.osmdb;

import com.streetferret.opus.model.OSMBounds;

public class OSMProtectedAreaRecord {
	private String type;
	private long id;
	private String name = "(untagged)";
	private String protectClass = null;
	private String iucnLevel = null;
	private OSMBounds bounds;

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
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProtectClass() {
		return protectClass;
	}

	public void setProtectClass(String protectClass) {
		this.protectClass = protectClass;
	}

	public String getIucnLevel() {
		return iucnLevel;
	}

	public void setIucnLevel(String iucnLevel) {
		this.iucnLevel = iucnLevel;
	}

	public OSMBounds getBounds() {
		return bounds;
	}

	public void setBounds(OSMBounds bounds) {
		this.bounds = bounds;
	}
}
