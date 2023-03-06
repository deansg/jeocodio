package com.github.deansg.jeocodio.models;

import java.util.Map;

public record GeocodingResult(AddressComponents addressComponents,
                              String formattedAddress,
                              Location location,
                              Double accuracy,
                              String accuracyType,
                              String source,
                              Map<String, Object> fields) {
}
