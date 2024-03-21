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