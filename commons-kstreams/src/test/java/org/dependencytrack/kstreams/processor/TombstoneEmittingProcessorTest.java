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
package org.dependencytrack.kstreams.processor;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TombstoneEmittingProcessorTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    @BeforeEach
    void beforeEach() {
        final var processorSupplier = new TombstoneEmittingProcessorSupplier<String, String>(
                "emitter-processor-store", Serdes.String(), Duration.ofSeconds(5), Duration.ofSeconds(30), key -> null);

        final var streamsBuilder = new StreamsBuilder();
        streamsBuilder
                .stream("input-topic", Consumed.with(Serdes.String(), Serdes.String()))
                .processValues(processorSupplier)
                .to("output-topic", Produced.with(Serdes.String(), Serdes.String()));

        testDriver = new TopologyTestDriver(streamsBuilder.build());
        inputTopic = testDriver.createInputTopic("input-topic", new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic("output-topic", new StringDeserializer(), new StringDeserializer());
    }

    @AfterEach
    void afterEach() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void test() {
        inputTopic.pipeInput(new TestRecord<>("foo", "bar", Instant.now()));
        inputTopic.pipeInput(new TestRecord<>("foo", "baz", Instant.now().plusSeconds(15)));

        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> assertThat(record.value()).isEqualTo("bar"),
                record -> assertThat(record.value()).isEqualTo("baz")
        );

        inputTopic.pipeInput(new TestRecord<>("qux", "quux", Instant.now().plusSeconds(50)));

        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isEqualTo("qux");
                    assertThat(record.value()).isEqualTo("quux");
                },
                record -> {
                    assertThat(record.key()).isEqualTo("foo");
                    assertThat(record.value()).isNull();
                }
        );
    }

}