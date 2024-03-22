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

import org.dependencytrack.common.KafkaTopic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KafkaStreamsUtilTest {

    @Test
    void testProcessorNameConsume() {
        Assertions.assertEquals("consume_from_dtrack.vuln-analysis.result_topic",
                KafkaStreamsUtil.processorNameConsume(KafkaTopic.VULN_ANALYSIS_RESULT));
    }

    @Test
    void testProcessorNameProduce() {
        Assertions.assertEquals("produce_to_dtrack.vuln-analysis.result_topic",
                KafkaStreamsUtil.processorNameProduce(KafkaTopic.VULN_ANALYSIS_RESULT));
    }

    @ParameterizedTest
    @CsvSource(value = {
            ", produce_to_dtrack.vuln-analysis.result_topic",
            "'', produce_to_dtrack.vuln-analysis.result_topic",
            "foo, produce_foo_to_dtrack.vuln-analysis.result_topic",
            "foo bar, produce_foo_bar_to_dtrack.vuln-analysis.result_topic",
            "foo  bar, produce_foo_bar_to_dtrack.vuln-analysis.result_topic",
            "foo-bar, produce_foo_bar_to_dtrack.vuln-analysis.result_topic",
    })
    void testProcessorNameProduceWithSubject(final String inputSubject, final String expectedName) {
        Assertions.assertEquals(expectedName,
                KafkaStreamsUtil.processorNameProduce(KafkaTopic.VULN_ANALYSIS_RESULT, inputSubject));
    }

}