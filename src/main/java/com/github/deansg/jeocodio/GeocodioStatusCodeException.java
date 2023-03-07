package com.github.deansg.jeocodio;

public class GeocodioStatusCodeException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public GeocodioStatusCodeException(int statusCode, String responseBody) {
        super(String.format("Received status code %s from Geocodio API. Response body is '%s'",
                statusCode, responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }
}
