package org.hyades.proto;

import com.google.protobuf.AbstractMessageLite;
import org.apache.kafka.common.errors.SerializationException;
import org.hyades.proto.vulnanalysis.v1.Component;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

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
    void testSerializationException() throws IOException {
        final var serializer = new KafkaProtobufSerializer<AbstractMessageLite>();

        final var mockMessage = mock(AbstractMessageLite.class);
        doThrow(IllegalStateException.class).when(mockMessage).writeTo(any(OutputStream.class));

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