package com.github.deansg.jeocodio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GeocodioClientTests {
    private HttpClient httpClient;
    private GeocodioClient geocodioClient;

    @BeforeEach
    public void setUp() {
        httpClient = mock(HttpClient.class);
        geocodioClient = new GeocodioClient(httpClient);
    }

    @Test
    public void foo() {
        geocodioClient.tempMethod();

        verify(httpClient).sendAsync(any(), any());
    }
}
