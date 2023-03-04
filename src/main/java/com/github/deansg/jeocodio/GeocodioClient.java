package com.github.deansg.jeocodio;

import com.github.deansg.jeocodio.models.GeocodingRequest;
import com.github.deansg.jeocodio.models.GeocodingRequestBuilder;
import com.github.deansg.jeocodio.models.GeocodingResponse;
import com.github.deansg.jeocodio.models.ReverseGeocodingResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GeocodioClient {
    private static final String DEFAULT_BASE_URL = "https://api.geocod.io/v1.7/";
    private final HttpClient httpClient;
    private final String apiKey;
    private final Gson gson;
    private final String baseUrl;

    /**
     * Creates a new GeocodioClient with a default {@link HttpClient} & base Geocodio URL
     * @param apiKey The Geocodio API key
     */
    public GeocodioClient(String apiKey) {
        this(HttpClient.newHttpClient(), apiKey, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new GeocodioClient with the provided {@link HttpClient} & the default base Geocodio URL
     * @param apiKey The Geocodio API key
     */
    public GeocodioClient(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new GeocodioClient with the provided {@link HttpClient}, default base Geocodio URL
     * @param apiKey The Geocodio API key
     */
    public GeocodioClient(HttpClient httpClient, String apiKey, String baseUrl) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        this.baseUrl = baseUrl;
    }

    public CompletableFuture<GeocodingResponse> geocodeAsync(String q) {
        return geocodeAsync(GeocodingRequestBuilder.builder().q(q).build());
    }

    /**
     * See <a href="https://www.geocod.io/docs/#single-address">this</a> for full documentation
     * @param request The geocoding request
     * @return a {@link GeocodingResponse} object
     */
    public CompletableFuture<GeocodingResponse> geocodeAsync(GeocodingRequest request) {
        var query = new HashMap<String, String>();
        query.put("q", request.q());
        query.put("api_key", this.apiKey);
        query.put("country", request.country());
        query.put("fields", formatFieldsParam(request.fields()));
        query.put("limit", Optional.ofNullable(request.limit()).map(Object::toString).orElse(null));
        query.put("format", request.format());
        query.put("street", request.street());
        query.put("city", request.city());
        query.put("state", request.state());
        query.put("postal_code", request.postalCode());
        var uri = buildURI("geocode", query);
        var httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        return sendAsync(httpRequest, GeocodingResponse.class);
    }

    public CompletableFuture<ReverseGeocodingResponse> reverseGeocodeAsync(GeocodingRequest request) {
        var query = new HashMap<String, String>();
        query.put("api_key", this.apiKey);
        var uri = buildURI("reverse", query);
        var httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        return sendAsync(httpRequest, ReverseGeocodingResponse.class);
    }

    private String formatFieldsParam(List<String> fields) {
        if (fields == null) {
            return null;
        }
        return String.join(",", fields);
    }

    private <T> CompletableFuture<T> sendAsync(HttpRequest httpRequest, Class<T> clazz) {
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(str -> this.gson.fromJson(str, clazz));
    }

    private URI buildURI(String endpoint, Map<String, String> query) {
        var queryString = query.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> entry.getValue() != null)
                .map(entry -> String.format("%s=%s", entry.getKey(), URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining("&"));
        String uriString = String.format("%s%s?%s", this.baseUrl, endpoint, queryString);
        return URI.create(uriString);
    }
}
