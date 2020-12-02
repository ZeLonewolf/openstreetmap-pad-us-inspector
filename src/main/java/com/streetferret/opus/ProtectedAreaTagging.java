package com.streetferret.opus;

public class ProtectedAreaTagging {
	private String iucnClass = null;
	private String access = null;
	private String ownership = null;
	private String owner = null;
	private String operator = null;
	private String name = "";

	private double minLat = Double.MAX_VALUE;
	private double minLon = Double.MAX_VALUE;
	private double maxLat = Double.MIN_VALUE;
	private double maxLon = Double.MIN_VALUE;

	public String getOwnership() {
		return ownership;
	}

	public void setOwnership(String ownership) {
		this.ownership = ownership;
	}

	public String getIucnClass() {
		return iucnClass;
	}

	public void setIucnClass(String iucnClass) {
		this.iucnClass = iucnClass;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public double getMinLat() {
		return minLat;
	}

	public void setMinLat(double minLat) {
		this.minLat = minLat;
	}

	public double getMinLon() {
		return minLon;
	}

	public void setMinLon(double minLon) {
		this.minLon = minLon;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public void setMaxLat(double maxLat) {
		this.maxLat = maxLat;
	}

	public double getMaxLon() {
		return maxLon;
	}

	public void setMaxLon(double maxLon) {
		this.maxLon = maxLon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
}
