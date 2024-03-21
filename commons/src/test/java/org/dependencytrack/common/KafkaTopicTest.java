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
package org.dependencytrack.common;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class KafkaTopicTest {

    @io.quarkus.test.junit.TestProfile(KafkaTopicTest.TestProfile.class)
    static class TestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "kafka.topic.prefix", "customPrefix."
            );
        }

        @Test
        void testKafkaTopicConfigWithPrefix() {
            System.setProperty("kafka.topic.prefix", "customPrefix.");
            assertEquals("customPrefix.dtrack.vulnerability.mirror.command", KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName());
        }
    }

    @Test
    void testKafkaTopicConfig() {
        assertEquals("dtrack.vulnerability.mirror.command", KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName());
    }
}
