package org.dependencytrack.kstreams.exception;

import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcessingExceptionHandlerTest {

    @Test
    void testHandleWithTransientError() {
        final var handler = new ProcessingExceptionHandler(Clock.systemUTC(), Duration.ofMinutes(5), 10);
        assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.REPLACE_THREAD);
    }

    @Test
    void testHandleWithNonTransientError() {
        final var handler = new ProcessingExceptionHandler(Clock.systemUTC(), Duration.ofMinutes(5), 10);
        assertThat(handler.handle(new IllegalStateException())).isEqualTo(StreamThreadExceptionResponse.SHUTDOWN_CLIENT);
    }

    @Test
    void testHandleWithTransientErrorExceedingThreshold() {
        final var handler = new ProcessingExceptionHandler(Clock.systemUTC(), Duration.ofMinutes(5), 10);

        for (int i = 0; i < 9; i++) {
            assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.REPLACE_THREAD);
        }

        assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.SHUTDOWN_CLIENT);
    }

    @Test
    void testHandleWithTransientErrorThresholdReset() {
        final var clockMock = mock(Clock.class);
        when(clockMock.instant())
                .thenReturn(Instant.EPOCH)
                .thenReturn(Instant.EPOCH.plusMillis(250))
                .thenReturn(Instant.EPOCH.plusSeconds(1).plusMillis(251));

        final var handler = new ProcessingExceptionHandler(clockMock, Duration.ofSeconds(1), 2);

        assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.REPLACE_THREAD);
        assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.SHUTDOWN_CLIENT);
        assertThat(handler.handle(new TimeoutException())).isEqualTo(StreamThreadExceptionResponse.REPLACE_THREAD);
    }

}