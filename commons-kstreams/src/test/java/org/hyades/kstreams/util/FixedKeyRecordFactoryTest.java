package org.hyades.kstreams.util;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FixedKeyRecordFactoryTest {

    @Test
    void testCreate() {
        final Headers headers = new RecordHeaders().add("foo", "bar".getBytes(StandardCharsets.UTF_8));
        final FixedKeyRecord<String, String> record = FixedKeyRecordFactory.create("foo", "bar", 123, headers);

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("foo");
        assertThat(record.value()).isEqualTo("bar");
        assertThat(record.timestamp()).isEqualTo(123);
        assertThat(record.headers()).isEqualTo(headers);
    }

}