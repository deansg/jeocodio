package io.github.deansg.jeocodio.models;

public record BatchGeocodingResponseItem(String query,
                                         GeocodingResponse response) {
}
