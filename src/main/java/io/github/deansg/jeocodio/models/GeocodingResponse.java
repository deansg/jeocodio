package io.github.deansg.jeocodio.models;

import java.util.List;

public record GeocodingResponse(GeocodingResponseInput input,
                                List<GeocodingResult> results) {
}
