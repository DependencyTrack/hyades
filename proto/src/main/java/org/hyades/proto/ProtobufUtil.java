package org.hyades.proto;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public final class ProtobufUtil {

    /**
     * Customized version of {@link DateTimeFormatter#ISO_INSTANT}, which will <em>always</em>
     * include three digits of the fractional second when formatting.
     * <p>
     * Per default {@link DateTimeFormatter#ISO_INSTANT} includes as many fractional digits as necessary,
     * which can result in different formats being produced, depending on how precise the given timestamp is.
     * <p>
     * Because we want output to be reliable, we chose to stay with a fixed format of: {@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}.
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendInstant(3)
            .toFormatter();

    public static String formatTimestamp(final Timestamp timestamp) {
        return DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(Timestamps.toMillis(timestamp)));
    }

}
