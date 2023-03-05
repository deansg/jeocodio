package com.github.deansg.jeocodio.models;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder
public record GeocodingRequest(String q,
                               String country,
                               List<String> fields,
                               Integer limit,
                               String format,
                               String street,
                               String city,
                               String state,
                               String postalCode) {
}
