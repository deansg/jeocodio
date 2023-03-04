package com.github.deansg.jeocodio;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GeocodioClientTests {
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
    public void foo() throws ExecutionException, InterruptedException {
        var argumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        var mockFuture = CompletableFuture.completedFuture(mockHttpResponse("{}"));
        when(httpClient.sendAsync(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockFuture);

        var future = geocodioClient.geocodeAsync("");

        Assertions.assertNotNull(future);
        var result = future.get();
        Assertions.assertNotNull(result);
        verify(httpClient).sendAsync(argumentCaptor.capture(), any());
        var actualRequest = argumentCaptor.getValue();
        var actualUri = actualRequest.uri();
        var actualQuery = actualUri.getQuery();
        Assertions.assertTrue(actualQuery.contains("api_key=" + randomApiKey));
    }

    private <T> HttpResponse<T> mockHttpResponse(T content) {
        @SuppressWarnings("unchecked")
        HttpResponse<T> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(content);
        return mockResponse;
    }
}
