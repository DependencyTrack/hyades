package org.hyades.vulnmirror.datasource.util;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LoggingUncaughtExceptionHandlerTest {

    @Test
    void testUncaughtException() {
        final Logger loggerMock = mock(Logger.class);
        final var exception = new IllegalArgumentException();

        new LoggingUncaughtExceptionHandler(loggerMock).uncaughtException(null, exception);

        final ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(loggerMock).error(messageCaptor.capture(), exceptionCaptor.capture());

        assertThat(messageCaptor.getValue()).isEqualTo("An uncaught exception occurred while processing a task");
        assertThat(exceptionCaptor.getValue()).isEqualTo(exception);
    }

}