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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dependencytrack.proto.vulnanalysis.v1.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonProtobufDeserializerTest {

    @Test
    void testDeserialize() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var deserializationContext = objectMapper.getDeserializationContext();
        final var jsonParser = objectMapper.createParser("""
                {
                  "uuid": "786b9343-9b98-477d-82b5-4b12ac5f5cec",
                  "cpe": "cpe:/a:acme:application:9.1.1",
                  "purl": "pkg:maven/acme/a@9.1.1",
                  "internal": true,
                  "unknownProperty": []
                }
                """);

        final var deserializer = new JacksonProtobufDeserializer<>(Component.class);
        final Component component = deserializer.deserialize(jsonParser, deserializationContext);
        assertThat(component).isNotNull();
        assertThat(component.getUuid()).isEqualTo("786b9343-9b98-477d-82b5-4b12ac5f5cec");
        assertThat(component.getCpe()).isEqualTo("cpe:/a:acme:application:9.1.1");
        assertThat(component.getPurl()).isEqualTo("pkg:maven/acme/a@9.1.1");
        assertThat(component.hasSwidTagId()).isFalse();
        assertThat(component.getInternal()).isTrue();
    }

}