package io.github.deansg.jeocodio;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.deansg.jeocodio.models.GeocodingRequestBuilder;

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

    @Disabled("Should be used for local testing with a real API key as an environment variable")
    @Test
    public void testGeocodingE2EWarnings() throws ExecutionException, InterruptedException {
        var geocodingRequest = GeocodingRequestBuilder.builder()
                .q("Road X, Hot Springs, AR")
                .fields(List.of("acs-social"))
                .limit(1)
                .build();

        var result = geocodioClient.geocodeAsync(geocodingRequest).get();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.results().size());
        Assertions.assertEquals(List.of("acs-social was skipped since result is not street-level"), result.results().get(0).warnings());
    }

}
