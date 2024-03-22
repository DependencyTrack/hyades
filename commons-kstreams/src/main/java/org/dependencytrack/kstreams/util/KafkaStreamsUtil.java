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

import org.apache.commons.lang3.StringUtils;
import org.dependencytrack.common.KafkaTopic;

public final class KafkaStreamsUtil {

    private KafkaStreamsUtil() {
    }

    /**
     * Create a name for a processor that consumes from a {@link KafkaTopic}.
     *
     * @param topic The {@link KafkaTopic} the processor consumes from
     * @return The processor name
     */
    public static String processorNameConsume(final KafkaTopic topic) {
        return "consume_from_%s_topic".formatted(topic);
    }

    /**
     * Create a name for a processor that produces to a {@link KafkaTopic}.
     *
     * @param topic The {@link KafkaTopic} the processor produces to
     * @return The processor name
     */
    public static String processorNameProduce(final KafkaTopic topic) {
        return processorNameProduce(topic, null);
    }

    /**
     * Create a name for a processor that produces a given subject to a {@link KafkaTopic}.
     * <p>
     * Useful when multiple processors within the topology produce to the same topic,
     * as processor names must be unique. Distinguishing them by subject is a convenient
     * way to bypass this restriction.
     *
     * @param topic   The {@link KafkaTopic} the processor produces to
     * @param subject Name of the subject that the processor produces
     * @return The processor name
     */
    public static String processorNameProduce(final KafkaTopic topic, final String subject) {
        if (subject == null || subject.isBlank()) {
            return "produce_to_%s_topic".formatted(topic);
        }

        return "produce_%s_to_%s_topic".formatted(format(subject), topic);
    }

    private static String format(final String str) {
        return StringUtils.trimToEmpty(StringUtils.lowerCase(str)).replaceAll("\\W+", "_");
    }

}
