package org.hyades.kstreams.util;

import org.hyades.common.KafkaTopic;
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