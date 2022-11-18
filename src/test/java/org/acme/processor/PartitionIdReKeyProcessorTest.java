package org.acme.processor;

import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class PartitionIdReKeyProcessorTest {

    private MockProcessorContext<Integer, String> context;

    @BeforeEach
    void setUp() {
        context = new MockProcessorContext<>();
    }

    @Test
    void test() {
        final var processor = new PartitionIdReKeyProcessor<String, String>();
        context.setRecordMetadata("topic", 1, 0);
        processor.init(context);

        processor.process(new Record<>("foo", "bar", Instant.now().toEpochMilli()));

        Assertions.assertEquals(1, context.forwarded().size());
        Assertions.assertEquals(1, context.forwarded().get(0).record().key());
        Assertions.assertEquals("bar", context.forwarded().get(0).record().value());
    }

}