package io.github.deansg.jeocodio.models;

public record AddressComponents(String number,
                                String predirectional,
                                String street,
                                String suffix,
                                String formattedStreet,
                                String city,
                                String county,
                                String state,
                                String zip,
                                String country) {
}
