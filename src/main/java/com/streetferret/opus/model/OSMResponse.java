package com.streetferret.opus.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OSMResponse {
	
	@JsonProperty("elements")
	private List<OSMElement> elements;

	public List<OSMElement> getElements() {
		return elements;
	}

	public void setElements(List<OSMElement> elements) {
		this.elements = elements;
	}

}
