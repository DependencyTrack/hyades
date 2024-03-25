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
package org.dependencytrack.proto;

import com.google.protobuf.AbstractMessageLite;
import org.apache.kafka.common.errors.SerializationException;
import org.dependencytrack.proto.vulnanalysis.v1.Component;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KafkaProtobufSerdeTest {

    @Test
    @SuppressWarnings("resource")
    void testRoundTrip() {
        final var serde = new KafkaProtobufSerde<>(Component.parser());

        final byte[] componentBytes = serde.serializer().serialize("topic", Component.newBuilder()
                .setUuid("786b9343-9b98-477d-82b5-4b12ac5f5cec")
                .setCpe("cpe:/a:acme:application:9.1.1")
                .setPurl("pkg:maven/acme/a@9.1.1")
                .setInternal(true)
                .build());
        assertThat(componentBytes).isNotNull();

        final Component component = serde.deserializer().deserialize("topic", componentBytes);
        assertThat(component).isNotNull();
        assertThat(component.getUuid()).isEqualTo("786b9343-9b98-477d-82b5-4b12ac5f5cec");
        assertThat(component.getCpe()).isEqualTo("cpe:/a:acme:application:9.1.1");
        assertThat(component.getPurl()).isEqualTo("pkg:maven/acme/a@9.1.1");
        assertThat(component.hasSwidTagId()).isFalse();
        assertThat(component.getInternal()).isTrue();
    }

    @Test
    @SuppressWarnings({"resource", "rawtypes"})
    void testSerializationException() {
        final var serializer = new KafkaProtobufSerializer<AbstractMessageLite>();

        final var mockMessage = mock(AbstractMessageLite.class);
        when(mockMessage.toByteArray())
                .thenThrow(IllegalStateException.class);

        assertThatExceptionOfType(SerializationException.class)
                .isThrownBy(() -> serializer.serialize("topic", mockMessage))
                .withCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    @SuppressWarnings("resource")
    void testDeserializationException() {
        final var serde = new KafkaProtobufSerde<>(Component.parser());

        assertThatExceptionOfType(SerializationException.class)
                .isThrownBy(() -> serde.deserializer().deserialize("topic", "[]".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @SuppressWarnings("resource")
    void testSerializeNull() {
        final var serde = new KafkaProtobufSerde<>(Component.parser());

        assertThat(serde.serializer().serialize("topic", null)).isNull();
    }

    @Test
    @SuppressWarnings("resource")
    void testDeserializeNull() {
        final var serde = new KafkaProtobufSerde<>(Component.parser());

        assertThat(serde.deserializer().deserialize("topic", null)).isEqualTo(null);
    }

}