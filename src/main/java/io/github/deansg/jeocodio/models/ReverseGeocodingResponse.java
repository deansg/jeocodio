package io.github.deansg.jeocodio.models;

import java.util.List;

public record ReverseGeocodingResponse(List<GeocodingResult> results) {
}
