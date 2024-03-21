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
package org.dependencytrack.repometaanalyzer.serde;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serde;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class KafkaPurlSerdeTest {

    private final Serde<PackageURL> purlSerde = new KafkaPurlSerde();

    @Test
    void testSerialize() throws MalformedPackageURLException {
        final byte[] data = purlSerde.serializer().serialize("topic", new PackageURL("pkg:foo/bar@1.2.3"));
        assertThat(data).asString(StandardCharsets.UTF_8).isEqualTo("pkg:foo/bar@1.2.3");
    }

    @Test
    void testDeserialize() {
        final PackageURL purl = purlSerde.deserializer().deserialize("topic", "pkg:foo/bar/baz@1.2.3".getBytes());
        assertThat(purl.getType()).isEqualTo("foo");
        assertThat(purl.getNamespace()).isEqualTo("bar");
        assertThat(purl.getName()).isEqualTo("baz");
        assertThat(purl.getVersion()).isEqualTo("1.2.3");
    }

    @Test
    void testDeserializeWithInvalidPurl() {
        assertThatExceptionOfType(SerializationException.class)
                .isThrownBy(() -> purlSerde.deserializer().deserialize("topic", "foo".getBytes()));
    }

}