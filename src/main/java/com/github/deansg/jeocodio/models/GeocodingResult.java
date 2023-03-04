package com.github.deansg.jeocodio.models;

public record GeocodingResult(AddressComponents addressComponents,
                              String formattedAddress,
                              Double accuracy,
                              String accuracyType,
                              String source) {
}
