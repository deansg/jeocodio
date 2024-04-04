package io.github.deansg.jeocodio.models;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public record GeocodingResult(AddressComponents addressComponents,
                              String formattedAddress,
                              Location location,
                              Double accuracy,
                              String accuracyType,
                              String source,
                              Map<String, Object> fields,
                              @SerializedName("_warnings") List<String> warnings) {
}
