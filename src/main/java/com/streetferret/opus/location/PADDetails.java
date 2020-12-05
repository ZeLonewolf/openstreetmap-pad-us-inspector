package com.streetferret.opus.location;

public class PADDetails {
	private String operator;
	private String owner;
	private String iucn;
	private String ownershipType;

	private double north;
	private double south;
	private double east;
	private double west;

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getIucn() {
		return iucn;
	}

	public void setIucn(String iucn) {
		this.iucn = iucn;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwnershipType() {
		return ownershipType;
	}

	public void setOwnershipType(String ownershipType) {
		this.ownershipType = ownershipType;
	}

	public void setBounds(double n, double s, double e, double w) {
		this.north = n;
		this.south = s;
		this.east = e;
		this.west = w;
	}

	public double getNorth() {
		return north;
	}

	public double getSouth() {
		return south;
	}

	public double getEast() {
		return east;
	}

	public double getWest() {
		return west;
	}
}
