package org.dependencytrack.vulnmirror.datasource.util;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LoggingRejectedExecutionHandlerTest {

    @Test
    void testRejectedExecution() {
        final Logger loggerMock = mock(Logger.class);
        final var handler = new LoggingRejectedExecutionHandler(loggerMock);

        handler.rejectedExecution(() -> {
        }, null);

        final ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(loggerMock).warn(messageCaptor.capture());

        assertThat(messageCaptor.getValue()).isEqualTo("""
                A task execution was rejected; Likely because the executor's \
                task queue is already saturated""");
    }

}