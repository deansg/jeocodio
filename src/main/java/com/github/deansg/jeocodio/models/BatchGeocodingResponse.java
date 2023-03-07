package com.github.deansg.jeocodio.models;

import java.util.List;

public record BatchGeocodingResponse(List<BatchGeocodingResponseItem> results) {
}
