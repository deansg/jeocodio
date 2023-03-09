package io.github.deansg.jeocodio;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.deansg.jeocodio.models.*;

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

    //region Constructors

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

    //endregion

    /**
     * Wrapper for {@link #geocodeAsync(GeocodingRequest)} in case you only want to provide the q parameter.
     * See <a href="https://www.geocod.io/docs/#single-address">this</a> for full documentation
     * @param q The geocoding query
     * @return a future of {@link GeocodingResponse}
     */
    public CompletableFuture<GeocodingResponse> geocodeAsync(String q) {
        return geocodeAsync(GeocodingRequestBuilder.builder().q(q).build());
    }

    /**
     * See <a href="https://www.geocod.io/docs/#single-address">this</a> for full documentation
     * @param request The full geocoding request
     * @return a future of {@link GeocodingResponse}
     */
    public CompletableFuture<GeocodingResponse> geocodeAsync(GeocodingRequest request) {
        var query = initializeRequestQuery();
        query.put("q", request.q());
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

    /**
     * See <a href="https://www.geocod.io/docs/#batch-geocoding">this</a> for full documentation
     * @param request The full geocoding request
     * @return a future of {@link BatchGeocodingResponse}
     */
    public CompletableFuture<BatchGeocodingResponse> batchGeocodeAsync(BatchGeocodingRequest request) {
        var query = initializeRequestQuery();
        query.put("fields", formatFieldsParam(request.fields()));
        query.put("limit", Optional.ofNullable(request.limit()).map(Object::toString).orElse(null));
        var uri = buildURI("geocode", query);
        var httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(this.gson.toJson(request.qs())))
                .header("Content-Type", "application/json")
                .uri(uri)
                .build();
        return sendAsync(httpRequest, BatchGeocodingResponse.class);
    }

    /**
     * See <a href="https://www.geocod.io/docs/#reverse-geocoding-single-coordinate">this</a> for full documentation
     * @param request The reverse geocoding request
     * @return a future of {@link ReverseGeocodingResponse}
     */
    public CompletableFuture<ReverseGeocodingResponse> reverseGeocodeAsync(ReverseGeocodingRequest request) {
        var query = initializeRequestQuery();
        query.put("q", String.format("%s,%s", request.latitude(), request.longitude()));
        query.put("fields", formatFieldsParam(request.fields()));
        query.put("limit", Optional.ofNullable(request.limit()).map(Object::toString).orElse(null));
        query.put("format", request.format());
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
                .thenApply(resp -> {
                    var body = resp.body();
                    if (resp.statusCode() != 200) {
                        throw new GeocodioStatusCodeException(resp.statusCode(), body);
                    }
                    return body;
                })
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

    private Map<String, String> initializeRequestQuery() {
        var query = new HashMap<String, String>();
        query.put("api_key", this.apiKey);
        return query;
    }
}
