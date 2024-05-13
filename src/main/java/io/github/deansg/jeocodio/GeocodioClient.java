package io.github.deansg.jeocodio;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.deansg.jeocodio.models.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class GeocodioClient {
    public static final String DEFAULT_BASE_URL = "https://api.geocod.io/v1.7/";
    private static final GeocodioClientOptions DEFAULT_OPTIONS = new GeocodioClientOptions(true, DEFAULT_BASE_URL);
    private final HttpClient httpClient;
    private final String apiKey;
    private final Gson gson;
    private final GeocodioClientOptions options;

    //region Constructors

    /**
     * Creates a new GeocodioClient with a default {@link HttpClient} and the default client options
     *
     * @param apiKey The Geocodio API key
     */
    public GeocodioClient(String apiKey) {
        this(defaultHTTPClient(), apiKey, DEFAULT_OPTIONS);
    }

    /**
     * Creates a new GeocodioClient with a default {@link HttpClient} and provided client options
     *
     * @param apiKey  The Geocodio API key
     * @param options The client options
     */
    public GeocodioClient(String apiKey, GeocodioClientOptions options) {
        this(defaultHTTPClient(), apiKey, options);
    }

    /**
     * Creates a new GeocodioClient with the provided {@link HttpClient} and the default client options
     *
     * @param httpClient The HttpClient
     * @param apiKey     The Geocodio API key
     */
    public GeocodioClient(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_OPTIONS);
    }

    /**
     * Creates a new GeocodioClient with the provided {@link HttpClient} and provided client options
     *
     * @param httpClient The HttpClient
     * @param apiKey     The Geocodio API key
     * @param options    The client options
     */
    public GeocodioClient(HttpClient httpClient, String apiKey, GeocodioClientOptions options) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        this.options = options;
    }

    private static HttpClient defaultHTTPClient() {
        return HttpClient.newHttpClient();
    }

    //endregion

    /**
     * Wrapper for {@link #geocodeAsync(GeocodingRequest)} in case you only want to provide the q parameter.
     * See <a href="https://www.geocod.io/docs/#single-address">this</a> for full documentation
     *
     * @param q The geocoding query
     * @return a future of {@link GeocodingResponse}
     */
    public CompletableFuture<GeocodingResponse> geocodeAsync(String q) {
        return geocodeAsync(GeocodingRequestBuilder.builder().q(q).build());
    }

    /**
     * See <a href="https://www.geocod.io/docs/#single-address">this</a> for full documentation
     *
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
        var httpRequest = buildHTTPRequest(HttpRequest.newBuilder()
                .GET()
                .uri(uri));
        return sendAsync(httpRequest, GeocodingResponse.class);
    }

    /**
     * See <a href="https://www.geocod.io/docs/#batch-geocoding">this</a> for full documentation
     *
     * @param request The full geocoding request
     * @return a future of {@link BatchGeocodingResponse}
     */
    public CompletableFuture<BatchGeocodingResponse> batchGeocodeAsync(BatchGeocodingRequest request) {
        var query = initializeRequestQuery();
        query.put("fields", formatFieldsParam(request.fields()));
        query.put("limit", Optional.ofNullable(request.limit()).map(Object::toString).orElse(null));
        var uri = buildURI("geocode", query);
        var httpRequest = buildHTTPRequest(HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(this.gson.toJson(request.qs())))
                .header("Content-Type", "application/json")
                .uri(uri));
        return sendAsync(httpRequest, BatchGeocodingResponse.class);
    }

    /**
     * See <a href="https://www.geocod.io/docs/#reverse-geocoding-single-coordinate">this</a> for full documentation
     *
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
        var httpRequest = buildHTTPRequest(HttpRequest.newBuilder()
                .GET()
                .uri(uri));
        return sendAsync(httpRequest, ReverseGeocodingResponse.class);
    }

    private String formatFieldsParam(List<String> fields) {
        if (fields == null) {
            return null;
        }
        return String.join(",", fields);
    }

    private <T> CompletableFuture<T> sendAsync(HttpRequest httpRequest, Class<T> clazz) {
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(this::readResponse)
                .thenApply(str -> this.gson.fromJson(str, clazz));
    }

    private String readResponse(HttpResponse<InputStream> resp) {
        try (var inputStream = getResponseInputStream(resp)) {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            if (resp.statusCode() != 200) {
                throw new GeocodioStatusCodeException(resp.statusCode(), json);
            }
            return json;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getResponseInputStream(HttpResponse<InputStream> resp) throws IOException {
        var inputStream = resp.body();
        if (resp.headers().firstValue("Content-Encoding").orElse("").equals("gzip")) {
            try {
                inputStream = new GZIPInputStream(inputStream);
            } catch (IOException e) {
                inputStream.close();
                throw e;
            }
        }
        return inputStream;
    }

    private URI buildURI(String endpoint, Map<String, String> query) {
        var queryString = query.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> entry.getValue() != null)
                .map(entry -> String.format("%s=%s", entry.getKey(), URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining("&"));
        String uriString = String.format("%s%s?%s", baseURL(), endpoint, queryString);
        return URI.create(uriString);
    }

    private HttpRequest buildHTTPRequest(HttpRequest.Builder builder) {
        if (Optional.ofNullable(this.options.gzip()).orElse(true)) {
            builder.header("Accept-Encoding", "gzip");
        }
        return builder.build();
    }

    private String baseURL() {
        var baseUrl = this.options.BaseURL();
        if (baseUrl == null) {
            baseUrl = DEFAULT_BASE_URL;
        }
        return baseUrl;
    }

    private Map<String, String> initializeRequestQuery() {
        var query = new HashMap<String, String>();
        query.put("api_key", this.apiKey);
        return query;
    }
}
