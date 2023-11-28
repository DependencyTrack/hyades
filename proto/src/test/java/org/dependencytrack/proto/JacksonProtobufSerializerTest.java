package org.dependencytrack.proto;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.dependencytrack.proto.vulnanalysis.v1.Component;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;

class JacksonProtobufSerializerTest {

    @Test
    void testSerialize() throws Exception {
        final var stringWriter = new StringWriter();
        final var jsonGenerator = new JsonFactory().createGenerator(stringWriter);
        final var serializerProvider = new ObjectMapper().getSerializerProvider();

        final var serializer = new JacksonProtobufSerializer<>(Component.class);
        serializer.serialize(Component.newBuilder()
                        .setUuid("786b9343-9b98-477d-82b5-4b12ac5f5cec")
                        .setCpe("cpe:/a:acme:application:9.1.1")
                        .setPurl("pkg:maven/acme/a@9.1.1")
                        .setInternal(true)
                        .build(),
                jsonGenerator,
                serializerProvider
        );

        jsonGenerator.flush();

        JsonAssertions.assertThatJson(stringWriter.toString())
                .isEqualTo(json("""
                        {
                          "uuid": "786b9343-9b98-477d-82b5-4b12ac5f5cec",
                          "cpe": "cpe:/a:acme:application:9.1.1",
                          "purl": "pkg:maven/acme/a@9.1.1",
                          "internal": true
                        }
                        """));
    }

}