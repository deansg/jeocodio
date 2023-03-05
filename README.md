# jeocodio

### 🌐 A Java wrapper for the Geocodio API

| __Tests status__             | ![Build Status](https://github.com/deansg/jeocodio/actions/workflows/test.yml/badge.svg) |
|------------------------------|------------------------------------------------------------------------------------------|
| __Jacoco Test Coverage__     | ![Coverage](.github/badges/jacoco.svg)                                                   |
| __Jacoco Branches Coverage__ | ![Branches](.github/badges/branches.svg)                                                 |

## How to Use
Basic example:

```java
import com.github.deansg.jeocodio.GeocodioClient;
import com.github.deansg.jeocodio.models.GeocodingRequest;
import com.github.deansg.jeocodio.models.GeocodingRequestBuilder;
import com.github.deansg.jeocodio.models.GeocodingResponse;

public class Main {
    public static void main(String[] args) {
        GeocodioClient client = new GeocodioClient("YOUR_GEOCODIO_API_KEY");
        GeocodingRequest geocodingRequest = GeocodingRequestBuilder.builder()
                .q("1109 N Highland St. Arlington VA")
                .fields(Arrays.asList("cd", "state"))
                .build();
        GeocodingResponse response = client.geocodeAsync(geocodingRequest).get();
        System.out.println(response.results().get(0).formattedAddress());
    }
}
```