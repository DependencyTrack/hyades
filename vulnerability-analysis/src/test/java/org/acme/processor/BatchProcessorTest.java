package org.acme.processor;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class BatchProcessorTest {

    private MockProcessorContext<String, List<String>> context;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        final Serde<List<String>> valuesSerde = Serdes.ListSerde(ArrayList.class, Serdes.String());
        final KeyValueStore<String, List<String>> stateStore = Stores
                .keyValueStoreBuilder(Stores.inMemoryKeyValueStore("stateStore"),
                        Serdes.String(), valuesSerde)
                .withLoggingDisabled()
                .build();

        context = new MockProcessorContext<>();
        context.setCurrentSystemTimeMs(Instant.now().toEpochMilli());
        stateStore.init(context.getStateStoreContext(), stateStore);
        context.addStateStore(stateStore);
    }

    @Test
    void testBatching() {
        final var processor = new BatchProcessor<String, String>("stateStore", Duration.ofHours(1), 2);
        processor.init(context);

        final var recordA = new Record<>("foo", UUID.randomUUID().toString(), Instant.now().toEpochMilli());
        final var recordB = new Record<>("foo", UUID.randomUUID().toString(), Instant.now().toEpochMilli());
        final var recordC = new Record<>("bar", UUID.randomUUID().toString(), Instant.now().toEpochMilli());
        final var recordD = new Record<>("bar", UUID.randomUUID().toString(), Instant.now().toEpochMilli());
        final var recordE = new Record<>("baz", UUID.randomUUID().toString(), Instant.now().toEpochMilli());

        processor.process(recordA);
        processor.process(recordB);
        processor.process(recordC);
        processor.process(recordD);
        processor.process(recordE);

        Assertions.assertEquals(2, context.forwarded().size());

        final List<String> batchFoo = context.forwarded().get(0).record().value();
        Assertions.assertEquals(recordA.value(), batchFoo.get(0));
        Assertions.assertEquals(recordB.value(), batchFoo.get(1));

        final List<String> batchBar = context.forwarded().get(1).record().value();
        Assertions.assertEquals(recordC.value(), batchBar.get(0));
        Assertions.assertEquals(recordD.value(), batchBar.get(1));
    }

    @Test
    void testMultipleBatches() {
        final var processor = new BatchProcessor<String, String>("stateStore", Duration.ofHours(1), 10);
        processor.init(context);

        for (int i = 0; i < 50; i++) {
            processor.process(new Record<>("foo", UUID.randomUUID().toString(), Instant.now().toEpochMilli()));
        }

        Assertions.assertEquals(5, context.forwarded().size());
    }

}