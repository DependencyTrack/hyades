package org.hyades.kstreams.util;

import org.apache.commons.lang3.StringUtils;
import org.hyades.common.KafkaTopic;

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
