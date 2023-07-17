package org.hyades.kstreams.exception;

import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ProcessingExceptionHandlerTest {

    @Test
    void testHandleWithTransientError() {
        final var handler = new ProcessingExceptionHandler(Duration.ofMinutes(5), 10);
        assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.REPLACE_THREAD);
    }

    @Test
    void testHandleWithNonTransientError() {
        final var handler = new ProcessingExceptionHandler(Duration.ofMinutes(5), 10);
        assertThat(handler.handle(new IllegalStateException())).isEqualTo(StreamThreadExceptionResponse.SHUTDOWN_CLIENT);
    }

    @Test
    void testHandleWithTransientErrorExceedingThreshold() {
        final var handler = new ProcessingExceptionHandler(Duration.ofMinutes(5), 10);

        for (int i = 0; i < 9; i++) {
            assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.REPLACE_THREAD);
        }

        assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.SHUTDOWN_CLIENT);
    }

    @Test
    void testHandleWithTransientErrorThresholdReset() {
        final var handler = new ProcessingExceptionHandler(Duration.ofMillis(250), 2);

        assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.REPLACE_THREAD);
        assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.SHUTDOWN_CLIENT);

        await()
                .atMost(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.REPLACE_THREAD));
    }

}