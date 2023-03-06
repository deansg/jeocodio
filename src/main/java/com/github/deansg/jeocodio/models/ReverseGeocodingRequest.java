package com.github.deansg.jeocodio.models;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder
public record ReverseGeocodingRequest(double latitude,
                                      double longitude,
                                      List<String> fields,
                                      Integer limit,
                                      String format) {
}
