/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.vulnmirror.datasource.util;

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