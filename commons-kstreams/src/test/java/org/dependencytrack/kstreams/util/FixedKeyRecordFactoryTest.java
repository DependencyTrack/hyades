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
package org.dependencytrack.kstreams.util;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FixedKeyRecordFactoryTest {

    @Test
    void testCreate() {
        final Headers headers = new RecordHeaders().add("foo", "bar".getBytes(StandardCharsets.UTF_8));
        final FixedKeyRecord<String, String> record = FixedKeyRecordFactory.create("foo", "bar", 123, headers);

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("foo");
        assertThat(record.value()).isEqualTo("bar");
        assertThat(record.timestamp()).isEqualTo(123);
        assertThat(record.headers()).isEqualTo(headers);
    }

}