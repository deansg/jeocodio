package io.github.deansg.jeocodio.models;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record GeocodingResponse(GeocodingResponseInput input,
                                List<GeocodingResult> results,
                                @SerializedName("_warnings") List<String> warnings) {
}
