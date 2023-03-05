package com.github.deansg.jeocodio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class GeocodioClientTests {
    private static final String EXPECTED_DEFAULT_BASE_URL_SCHEME = "https";
    private static final String EXPECTED_DEFAULT_BASE_URL_HOST = "api.geocod.io";
    private String randomApiKey;
    private HttpClient httpClient;
    private GeocodioClient geocodioClient;

    @BeforeEach
    public void setUp() {
        randomApiKey = UUID.randomUUID().toString();
        httpClient = mock(HttpClient.class);
        geocodioClient = new GeocodioClient(httpClient, randomApiKey);
    }

    @Test
    public void testGeocodeAsyncSanity() throws ExecutionException, InterruptedException, IOException {
        var rawResponse = readSampleGeocodingResponse();
        var inputQ = "1109 N Highland St. Arlington VA";
        var mockFuture = CompletableFuture.completedFuture(mockHttpResponse(rawResponse));
        when(httpClient.sendAsync(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockFuture);

        var future = geocodioClient.geocodeAsync(inputQ);

        assertNotNull(future);
        var result = future.get();
        assertNotNull(result);
        validateGeocodeRequest(inputQ);
    }

    private void validateGeocodeRequest(String inputQ) {
        var argumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).sendAsync(argumentCaptor.capture(), any());
        var actualRequest = argumentCaptor.getValue();
        assertEquals("GET", actualRequest.method());
        var actualUri = actualRequest.uri();
        assertEquals(EXPECTED_DEFAULT_BASE_URL_SCHEME, actualUri.getScheme());
        assertEquals(EXPECTED_DEFAULT_BASE_URL_HOST, actualUri.getHost());
        assertEquals("/v1.7/geocode", actualUri.getPath());
        assertEquals(String.format("api_key=%s&q=%s", randomApiKey, encodeUrlComponent(inputQ)), actualUri.getQuery());
    }

    private <T> HttpResponse<T> mockHttpResponse(T content) {
        @SuppressWarnings("unchecked")
        HttpResponse<T> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(content);
        return mockResponse;
    }

    private String readSampleGeocodingResponse() throws IOException {
        return TestUtils.readResource("sample_geocoding_response.json");
    }

    private String encodeUrlComponent(String component) {
        return URLEncoder.encode(component, StandardCharsets.UTF_8);
    }
}
