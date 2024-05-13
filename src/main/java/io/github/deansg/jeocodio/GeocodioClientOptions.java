package io.github.deansg.jeocodio;

import io.soabase.recordbuilder.core.RecordBuilder;

/**
 *
 * @param gzip whether to request the API to gzip its responses. Default is true.
 * @param BaseURL The Geocodio API base URL. Default is {@link GeocodioClient#DEFAULT_BASE_URL}
 */
@RecordBuilder
public record GeocodioClientOptions(Boolean gzip, String BaseURL) {
}
