package com.github.deansg.jeocodio.models;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder
public record BatchGeocodingRequest(List<String> qs,
                                    List<String> fields,
                                    Integer limit) {
}
