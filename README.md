# jeocodio

### 🌐 A Java wrapper for the Geocodio API

|                              |                                                                                                                    |
|------------------------------|--------------------------------------------------------------------------------------------------------------------|
| __License__                  | [![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) |
| __Tests status__             | ![Build Status](https://github.com/deansg/jeocodio/actions/workflows/test.yml/badge.svg)                           |
| __Test Coverage__            | ![Coverage](.github/badges/jacoco.svg) ![Branches](.github/badges/branches.svg)                                    |

## How to Use

Basic example:

```java
import com.github.deansg.jeocodio.GeocodioClient;
import com.github.deansg.jeocodio.models.GeocodingRequest;
import com.github.deansg.jeocodio.models.GeocodingRequestBuilder;
import com.github.deansg.jeocodio.models.GeocodingResponse;
import com.github.deansg.jeocodio.models.ReverseGeocodingResponse;

public class JeocodioDemo {
    public static void main(String[] args) {
        GeocodioClient client = new GeocodioClient("YOUR_GEOCODIO_API_KEY");
        GeocodingRequest geocodingRequest = GeocodingRequestBuilder.builder()
                .q("1109 N Highland St. Arlington VA")
                .fields(Arrays.asList("cd", "state"))
                .build();

        // Single geocoding request
        GeocodingResponse response = client.geocodeAsync(geocodingRequest).get();
        System.out.println(response.input().formattedAddress());
        System.out.println(response.results().get(0).formattedAddress());

        // Single reverse geocoding request
        ReverseGeocodingResponse response = client.reverseGeocodeAsync(geocodingRequest).get();
        System.out.println(response.results().get(0).formattedAddress());
    }
}
```