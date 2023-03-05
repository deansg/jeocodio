package com.github.deansg.jeocodio.models;

public record GeocodingResponseInput(AddressComponents addressComponents,
                                     String formattedAddress) {
}
