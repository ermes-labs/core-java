package io.ermes.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GeoCoordinates (
		@JsonProperty(value = "latitude", required = true) double latitude,
		@JsonProperty(value = "longitude", required = true) double longitude) {
	@JsonCreator
	public GeoCoordinates {
		assert latitude  >= -90.0 && latitude  <= 90.0;
		assert longitude >= -180.0 && longitude <= 180.0;
	}

}