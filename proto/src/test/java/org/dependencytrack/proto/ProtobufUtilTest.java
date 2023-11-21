package org.dependencytrack.proto;

import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ProtobufUtilTest {

    @Test
    void testFormatTimestamp() {
        assertThat(ProtobufUtil.formatTimestamp(Timestamps.fromDate(new Date())))
                .matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$");

        // Just to be super-sure: When the timestamp has no fractional part,
        // we still include the fraction for consistency's sake.
        assertThat(ProtobufUtil.formatTimestamp(Timestamps.fromDate(Date.from(Instant.ofEpochSecond(666)))))
                .matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.000Z$");
    }

}