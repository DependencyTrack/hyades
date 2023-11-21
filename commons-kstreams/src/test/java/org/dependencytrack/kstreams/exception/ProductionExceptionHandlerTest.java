package org.dependencytrack.kstreams.exception;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.streams.errors.ProductionExceptionHandler.ProductionExceptionHandlerResponse;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductionExceptionHandlerTest {

    @Test
    void testHandle() {
        final var record = new ProducerRecord<>("topic", 6, "key".getBytes(), "value".getBytes());
        final var handler = new ProductionExceptionHandler(Clock.systemUTC(), Duration.ofMinutes(5), 10);

        for (int i = 0; i < 9; i++) {
            assertThat(handler.handle(record, new RecordTooLargeException())).isEqualTo(ProductionExceptionHandlerResponse.CONTINUE);
        }

        assertThat(handler.handle(record, new RecordTooLargeException())).isEqualTo(ProductionExceptionHandlerResponse.FAIL);
    }

    @Test
    void testHandleWithThresholdReset() {
        final var clockMock = mock(Clock.class);
        when(clockMock.instant())
                .thenReturn(Instant.EPOCH)
                .thenReturn(Instant.EPOCH.plusMillis(250))
                .thenReturn(Instant.EPOCH.plusSeconds(1).plusMillis(251));

        final var record = new ProducerRecord<>("topic", 6, "key".getBytes(), "value".getBytes());
        final var handler = new ProductionExceptionHandler(clockMock, Duration.ofSeconds(1), 2);

        assertThat(handler.handle(record, new RecordTooLargeException())).isEqualTo(ProductionExceptionHandlerResponse.CONTINUE);
        assertThat(handler.handle(record, new RecordTooLargeException())).isEqualTo(ProductionExceptionHandlerResponse.FAIL);

        await()
                .atMost(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(handler.handle(record, new RecordTooLargeException())).isEqualTo(ProductionExceptionHandlerResponse.CONTINUE));
    }

    @Test
    void testHandleWithUnexpectedException() {
        final var record = new ProducerRecord<>("topic", 6, "key".getBytes(), "value".getBytes());
        final var handler = new ProductionExceptionHandler(Clock.systemUTC(), Duration.ofMinutes(5), 10);

        assertThat(handler.handle(record, new IllegalStateException())).isEqualTo(ProductionExceptionHandlerResponse.FAIL);
    }

}