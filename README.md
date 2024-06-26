# jeocodio

### 🌐 A Java wrapper for the Geocodio API

|                    |                                                                                                                                                                                      |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| __License__        | [![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)                                                                   |
| __Tests status__   | ![Build Status](https://github.com/deansg/jeocodio/actions/workflows/test.yml/badge.svg)                                                                                             |
| __Test Coverage__  | ![Coverage](.github/badges/jacoco.svg) ![Branches](.github/badges/branches.svg)                                                                                                      |
| __Latest Release__ | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.deansg/jeocodio/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.deansg/jeocodio) |

## How to Use

Jeocodio requires Java 17 and above. It is aimed to be a lightweight library and only includes
[Gson](https://github.com/google/gson) and [RecordBuilder](https://github.com/Randgalt/record-builder) as dependencies.

### Download

Can be downloaded through Maven, by adding the following dependency:

```xml

<dependency>
    <groupId>io.github.deansg</groupId>
    <artifactId>jeocodio</artifactId>
    <version>0.3.0</version>
</dependency>
```

### Code examples

```java
import io.github.deansg.jeocodio.GeocodioClient;
import io.github.deansg.jeocodio.GeocodioStatusCodeException;
import io.github.deansg.jeocodio.models.*;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class JeocodioDemo {
    public static void main(String[] args) throws Exception {
        // Basic client creation
        GeocodioClient client = new GeocodioClient("YOUR_GEOCODIO_API_KEY");

        // Single geocoding request
        GeocodingRequest geocodingRequest = GeocodingRequestBuilder.builder()
                .q("1109 N Highland St. Arlington VA")
                .fields(Arrays.asList("cd", "state"))
                .build();
        GeocodingResponse response = client.geocodeAsync(geocodingRequest).get();
        System.out.println(response.input().formattedAddress());
        System.out.println(response.results().get(0).formattedAddress());

        // Batch geocoding request
        BatchGeocodingRequest batchGeocodingRequest = BatchGeocodingRequestBuilder.builder()
                .qs(List.of("1109 N Highland St, Arlington VA", "525 University Ave, Toronto, ON, Canada"))
                .build();
        BatchGeocodingResponse batchGeocodingResponse = client.batchGeocodeAsync(batchGeocodingRequest).get();
        System.out.println(batchGeocodingResponse.results().get(0).query());

        // Single reverse geocoding request
        ReverseGeocodingRequest reverseGeocodingRequest = ReverseGeocodingRequestBuilder.builder()
                .latitude(38.9002898)
                .longitude(-76.9990361)
                .build();
        ReverseGeocodingResponse reverseGeocodingResponse = client.reverseGeocodeAsync(geocodingRequest).get();
        System.out.println(reverseGeocodingResponse.results().get(0).formattedAddress());

        // Using a custom java.net.http.HttpClient instance
        client = new GeocodioClient(HttpClient.newBuilder().build(), "YOUR_GEOCODIO_API_KEY");

        // Error handling
        try {
            client.geocodeAsync("").get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof GeocodioStatusCodeException statusCodeException) {
                System.out.println(statusCodeException.statusCode());
                System.out.println(statusCodeException.responseBody());
            }
        }
    }
}
```

## TODOs

* Add support for more forms of batch geocoding
* Add batch reverse geocoding
* Add non-async variants for all methods

## Other notes

* Since this library still didn't reach version 1.0.0, the rules of [Semantic Versioning](https://semver.org/) might not
  yet be followed in full