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
package org.dependencytrack.kstreams.exception;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler.DeserializationHandlerResponse;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeserializationExceptionHandlerTest {

    @Test
    void testHandle() {
        final var record = new ConsumerRecord<>("topic", 6, 3, "key".getBytes(), "value".getBytes());
        final var processorContext = mock(ProcessorContext.class);
        final var handler = new DeserializationExceptionHandler(Clock.systemUTC(), Duration.ofMinutes(5), 10);

        for (int i = 0; i < 9; i++) {
            assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.CONTINUE);
        }

        assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.FAIL);
    }

    @Test
    void testHandleWithThresholdReset() {
        final var clockMock = mock(Clock.class);
        when(clockMock.instant())
                .thenReturn(Instant.EPOCH)
                .thenReturn(Instant.EPOCH.plusMillis(250))
                .thenReturn(Instant.EPOCH.plusSeconds(1).plusMillis(251));

        final var record = new ConsumerRecord<>("topic", 6, 3, "key".getBytes(), "value".getBytes());
        final var processorContext = mock(ProcessorContext.class);
        final var handler = new DeserializationExceptionHandler(clockMock, Duration.ofSeconds(1), 2);

        assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.CONTINUE);
        assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.FAIL);
        assertThat(handler.handle(processorContext, record, new SerializationException())).isEqualTo(DeserializationHandlerResponse.CONTINUE);
    }

}