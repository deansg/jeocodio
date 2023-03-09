package io.github.deansg.jeocodio;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class IntegrationTest {
    private static final String API_KEY = System.getenv("API_KEY");
    private GeocodioClient geocodioClient;

    @BeforeEach
    public void setUp() {
        geocodioClient = new GeocodioClient(API_KEY);
    }

    @Disabled("Should be used for local testing with a real API key as an environment variable")
    @Test
    public void testGeocodingE2E() throws ExecutionException, InterruptedException {
         var q = "1109 N Highland St, Arlington VA";

        var future = geocodioClient.geocodeAsync(q);

        var result = future.get();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.results().size());
    }
}
