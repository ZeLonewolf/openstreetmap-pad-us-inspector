package com.streetferret.opus.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OSMElement {

	public enum Type {
		node, way, relation
	};

	@JsonProperty("type")
	private Type type;

	@JsonProperty("id")
	private long id;

	@JsonProperty("tags")
	private Map<String, String> tags;

	@JsonProperty("bounds")
	private OSMBounds bounds;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
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

	public OSMBounds getBounds() {
		return bounds;
	}

	public void setBounds(OSMBounds bounds) {
		this.bounds = bounds;
	}

}
