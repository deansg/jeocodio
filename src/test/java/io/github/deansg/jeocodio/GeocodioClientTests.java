package io.github.deansg.jeocodio;

import io.github.deansg.jeocodio.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.imageio.IIOException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
        geocodioClient = new GeocodioClient(httpClient, randomApiKey,
                GeocodioClientOptionsBuilder.builder().gzip(false).build());
    }

    @Test
    public void testConstructors() {
        geocodioClient = new GeocodioClient(randomApiKey);
        geocodioClient = new GeocodioClient(randomApiKey, new GeocodioClientOptions(null, null));
    }

    //region testGeocodeAsyncSanity

    @Test
    public void testGeocodeAsyncSanity() throws ExecutionException, InterruptedException, IOException {
        var rawResponse = readSampleGeocodingResponse();
        var inputQ = "1109 N Highland St. Arlington VA";
        var mockFuture = CompletableFuture.completedFuture(mockHttpResponse(rawResponse));
        when(httpClient.sendAsync(any(), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(mockFuture);

        var future = geocodioClient.geocodeAsync(inputQ);

        validateGeocodeResponse(future);
        validateGeocodeRequest(inputQ);
    }

    private void validateGeocodeResponse(CompletableFuture<GeocodingResponse> responseFuture) throws ExecutionException, InterruptedException {
        assertNotNull(responseFuture);
        var response = responseFuture.get();
        assertNotNull(response);
        assertNotNull(response.input());
        assertNotNull(response.results());
        assertEquals(1, response.results().size());
        var result = response.results().get(0);
        assertEquals("1109 N Highland St, Arlington, VA 22201", result.formattedAddress());
        Assertions.assertEquals(new Location(38.886672, -77.094735), result.location());
        assertEquals(1.0, result.accuracy());
        assertEquals("rooftop", result.accuracyType());
        assertEquals("N Highland St", result.addressComponents().formattedStreet());
        assertEquals("22201", result.addressComponents().zip());
        assertNull(result.fields());
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

    private InputStream readSampleGeocodingResponse() throws IOException {
        return new ByteArrayInputStream(TestUtils.readResource("sample_geocoding_response.json"));
    }

    //endregion

    //region testGeocodeAsyncWithFields

    @Test
    public void testGeocodeAsyncWithFields() throws ExecutionException, InterruptedException, IOException {
        var rawResponse = readSampleGeocodingWithFieldsResponse();
        var inputQ = "1109 N Highland St. Arlington VA";
        var inputFields = Arrays.asList("cd", "state");
        var mockFuture = CompletableFuture.completedFuture(mockHttpResponse(rawResponse));
        when(httpClient.sendAsync(any(), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(mockFuture);
        var geocodingRequest = GeocodingRequestBuilder.builder()
                .q(inputQ)
                .fields(inputFields)
                .build();

        var future = geocodioClient.geocodeAsync(geocodingRequest);

        validateGeocodeWithFieldsResponse(future);
        validateGeocodeWithFieldsRequest(inputQ, inputFields);
    }

    private void validateGeocodeWithFieldsResponse(CompletableFuture<GeocodingResponse> responseFuture) throws ExecutionException, InterruptedException {
        assertNotNull(responseFuture);
        var response = responseFuture.get();
        assertNotNull(response);
        assertNotNull(response.input());
        assertNotNull(response.results());
        assertEquals(1, response.results().size());
        var result = response.results().get(0);
        assertEquals("1109 N Highland St, Arlington, VA 22201", result.formattedAddress());
        assertEquals(1.0, result.accuracy());
        assertEquals("rooftop", result.accuracyType());
        assertEquals("N Highland St", result.addressComponents().formattedStreet());
        assertEquals("22201", result.addressComponents().zip());
        assertNotNull(result.fields());
        assertEquals(Set.of("congressional_districts", "state_legislative_districts"), result.fields().keySet());
        var congressionalDistricts = (List<?>) result.fields().get("congressional_districts");
        assertEquals(1, congressionalDistricts.size());
        var firstDistrict = (Map<?, ?>) congressionalDistricts.get(0);
        assertEquals(8.0, firstDistrict.get("district_number"));
    }

    private void validateGeocodeWithFieldsRequest(String inputQ, List<String> fields) {
        var argumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).sendAsync(argumentCaptor.capture(), any());
        var actualRequest = argumentCaptor.getValue();
        assertEquals("GET", actualRequest.method());
        var actualUri = actualRequest.uri();
        assertEquals(EXPECTED_DEFAULT_BASE_URL_SCHEME, actualUri.getScheme());
        assertEquals(EXPECTED_DEFAULT_BASE_URL_HOST, actualUri.getHost());
        assertEquals("/v1.7/geocode", actualUri.getPath());
        assertEquals(String.format("api_key=%s&fields=%s&q=%s", randomApiKey, String.join(",", fields), encodeUrlComponent(inputQ)),
                actualUri.getQuery());
    }

    //endregion

    //region testGeocodeAsyncGZIP

    @Test
    public void testGeocodeAsyncGZIP() throws ExecutionException, InterruptedException, IOException {
        var rawResponse = readSampleGZIPGeocodingResponse();
        var inputQ = "1109 N Highland St. Arlington VA";
        var mockResponse = mockHttpResponse(rawResponse);
        when(mockResponse.headers()).thenReturn(HttpHeaders.of(Map.of("Content-Encoding", List.of("gzip")),
                (s1, s2) -> true));
        var mockFuture = CompletableFuture.completedFuture(mockResponse);
        when(httpClient.sendAsync(any(), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(mockFuture);
        geocodioClient = new GeocodioClient(httpClient, randomApiKey);

        var future = geocodioClient.geocodeAsync(inputQ);

        validateGeocodeResponse(future);
        validateGeocodeRequest(inputQ);
    }

    private InputStream readSampleGZIPGeocodingResponse() throws IOException {
        return new ByteArrayInputStream(TestUtils.readResource("sample_geocoding_response.gzip"));
    }

    //endregion

    @Test
    public void testBadStatusCode() {
        int statusCode = 422;
        String errorMessage = "Invalid request!";
        HttpResponse<InputStream> rawResponse = mockHttpResponse(statusCode, new ByteArrayInputStream(errorMessage.getBytes()));
        var mockFuture = CompletableFuture.completedFuture(rawResponse);
        when(httpClient.sendAsync(any(), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(mockFuture);

        var future = geocodioClient.geocodeAsync("");

        var exception = assertThrows(ExecutionException.class, future::get);
        var innerException = assertInstanceOf(GeocodioStatusCodeException.class, exception.getCause());
        assertEquals(statusCode, innerException.statusCode());
        assertEquals(errorMessage, innerException.responseBody());
    }

    //region testBatchGeocodeAsyncSanity

    @Test
    public void testBatchGeocodeAsyncSanity() throws ExecutionException, InterruptedException, IOException {
        var rawResponse = readSampleBatchGeocodingResponse();
        var request = BatchGeocodingRequestBuilder.builder()
                .qs(List.of("1109 N Highland St, Arlington VA", "525 University Ave, Toronto, ON, Canada"))
                .build();
        var mockFuture = CompletableFuture.completedFuture(mockHttpResponse(rawResponse));
        when(httpClient.sendAsync(any(), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(mockFuture);

        var future = geocodioClient.batchGeocodeAsync(request);

        validateBatchGeocodeResponse(future);
        validateBatchGeocodeRequest();
    }

    private void validateBatchGeocodeResponse(CompletableFuture<BatchGeocodingResponse> responseFuture) throws ExecutionException, InterruptedException {
        assertNotNull(responseFuture);
        var response = responseFuture.get();
        assertNotNull(response);
        assertNotNull(response.results());
        assertEquals(2, response.results().size());
        var result = response.results().get(0);
        assertEquals("1109 N Highland St, Arlington VA", result.query());
        var innerResponse = result.response();
        assertNotNull(innerResponse);
        assertNotNull(innerResponse.input());
        assertNotNull(innerResponse.results());
        assertEquals(2, innerResponse.results().size());
    }

    private void validateBatchGeocodeRequest() {
        var argumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).sendAsync(argumentCaptor.capture(), any());
        var actualRequest = argumentCaptor.getValue();
        assertEquals("POST", actualRequest.method());
        var actualUri = actualRequest.uri();
        assertEquals(EXPECTED_DEFAULT_BASE_URL_SCHEME, actualUri.getScheme());
        assertEquals(EXPECTED_DEFAULT_BASE_URL_HOST, actualUri.getHost());
        assertEquals("/v1.7/geocode", actualUri.getPath());
        assertEquals(String.format("api_key=%s", randomApiKey), actualUri.getQuery());
        assertEquals(Map.of("Content-Type", List.of("application/json")), actualRequest.headers().map());
    }

    private InputStream readSampleBatchGeocodingResponse() throws IOException {
        return new ByteArrayInputStream(TestUtils.readResource("sample_batch_geocoding_response.json"));
    }

    //endregion

    //region testReverseGeocodeAsyncSanity

    @Test
    public void testReverseGeocodeAsyncSanity() throws ExecutionException, InterruptedException, IOException {
        var rawResponse = readSampleReverseGeocodingResponse();
        double reqLatitude = 38.9002898;
        double reqLongitude = -76.9990361;
        var query = ReverseGeocodingRequestBuilder.builder()
                .latitude(reqLatitude)
                .longitude(reqLongitude)
                .build();
        var mockFuture = CompletableFuture.completedFuture(mockHttpResponse(rawResponse));
        when(httpClient.sendAsync(any(), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(mockFuture);

        var future = geocodioClient.reverseGeocodeAsync(query);

        validateReverseGeocodeResponse(future);
        validateReverseGeocodeRequest(reqLatitude, reqLongitude);
    }

    private void validateReverseGeocodeResponse(CompletableFuture<ReverseGeocodingResponse> responseFuture) throws ExecutionException, InterruptedException {
        assertNotNull(responseFuture);
        var response = responseFuture.get();
        assertNotNull(response);
        assertNotNull(response.results());
        assertEquals(2, response.results().size());
        var secondResult = response.results().get(1);
        assertEquals("510 H St NE, Washington, DC 20002", secondResult.formattedAddress());
        assertEquals(0.9, secondResult.accuracy());
        assertEquals("rooftop", secondResult.accuracyType());
        assertEquals("City of Washington", secondResult.source());
        assertEquals(new Location(38.900429, -76.998965), secondResult.location());
        assertEquals("H St NE", secondResult.addressComponents().formattedStreet());
        assertEquals("20002", secondResult.addressComponents().zip());
        assertNull(secondResult.fields());
    }

    private void validateReverseGeocodeRequest(double lat, double lon) {
        var argumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).sendAsync(argumentCaptor.capture(), any());
        var actualRequest = argumentCaptor.getValue();
        assertEquals("GET", actualRequest.method());
        var actualUri = actualRequest.uri();
        assertEquals(EXPECTED_DEFAULT_BASE_URL_SCHEME, actualUri.getScheme());
        assertEquals(EXPECTED_DEFAULT_BASE_URL_HOST, actualUri.getHost());
        assertEquals("/v1.7/reverse", actualUri.getPath());
        assertEquals(String.format("api_key=%s&q=%s,%s", randomApiKey, lat, lon), actualUri.getQuery());
    }

    private InputStream readSampleReverseGeocodingResponse() throws IOException {
        return new ByteArrayInputStream(TestUtils.readResource("sample_reverse_geocoding_response.json"));
    }

    //endregion

    @Test
    public void testIOException() throws IOException {
        var inputQ = "1109 N Highland St. Arlington VA";
        var badStream = mock(InputStream.class);
        when(badStream.readAllBytes()).thenThrow(IIOException.class);
        HttpResponse<InputStream> mockHTTPResponse = mockHttpResponse(badStream);
        var mockFuture = CompletableFuture.completedFuture(mockHTTPResponse);
        when(httpClient.sendAsync(any(), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(mockFuture);

        var future = geocodioClient.geocodeAsync(inputQ);

        var exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(UncheckedIOException.class, exception.getCause());
    }


    //region Utils

    private <T> HttpResponse<T> mockHttpResponse(T content) {
        return mockHttpResponse(200, content);
    }

    private <T> HttpResponse<T> mockHttpResponse(int statusCode, T content) {
        @SuppressWarnings("unchecked")
        HttpResponse<T> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(statusCode);
        when(mockResponse.body()).thenReturn(content);
        when(mockResponse.headers()).thenReturn(HttpHeaders.of(Collections.emptyMap(), (s1, s2) -> true));
        return mockResponse;
    }

    private String encodeUrlComponent(String component) {
        return URLEncoder.encode(component, StandardCharsets.UTF_8);
    }

    private InputStream readSampleGeocodingWithFieldsResponse() throws IOException {
        return new ByteArrayInputStream(TestUtils.readResource("sample_geocoding_with_fields_response.json"));
    }

    //endregion
}
