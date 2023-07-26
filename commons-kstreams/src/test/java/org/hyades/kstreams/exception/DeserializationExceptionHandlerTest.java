package org.hyades.kstreams.exception;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler.DeserializationHandlerResponse;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

class DeserializationExceptionHandlerTest {

    @Test
    void testHandle() {
        final var record = new ConsumerRecord<>("topic", 6, 3, "key".getBytes(), "value".getBytes());
        final var processorContext = mock(ProcessorContext.class);
        final var handler = new DeserializationExceptionHandler(Duration.ofMinutes(5), 10);

        for (int i = 0; i < 9; i++) {
            assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.CONTINUE);
        }

        assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.FAIL);
    }

    @Test
    void testHandleWithThresholdReset() {
        final var record = new ConsumerRecord<>("topic", 6, 3, "key".getBytes(), "value".getBytes());
        final var processorContext = mock(ProcessorContext.class);
        final var handler = new DeserializationExceptionHandler(Duration.ofMillis(250), 2);

        assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.CONTINUE);
        assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.FAIL);

        await()
                .atMost(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.CONTINUE));
    }

}