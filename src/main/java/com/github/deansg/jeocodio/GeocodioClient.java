package com.github.deansg.jeocodio;

import java.net.http.HttpClient;

public class GeocodioClient {
    private final HttpClient httpClient;

    public GeocodioClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void tempMethod() {
        httpClient.sendAsync(null, null);
    }
}
