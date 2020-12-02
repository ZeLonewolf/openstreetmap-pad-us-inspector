package com.streetferret.opus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OSMBounds {

	@JsonProperty("minlat")
	private double minLat;

	@JsonProperty("minlon")
	private double minLon;

	@JsonProperty("maxlat")
	private double maxLat;

	@JsonProperty("maxlon")
	private double maxLon;

	public void setMinLat(double minLat) {
		this.minLat = minLat;
	}

	public void setMinLon(double minLon) {
		this.minLon = minLon;
	}

	public void setMaxLat(double maxLat) {
		this.maxLat = maxLat;
	}

	public void setMaxLon(double maxLon) {
		this.maxLon = maxLon;
	}

}
