package org.hyades.common;

import io.quarkus.test.junit.QuarkusTest;
import org.hyades.config.KafkaTopicConfig;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class KafkaTopicTest {

    @Inject
    KafkaTopicConfig kafkaTopicConfig;

    @Test
    void testKafkaTopicConfig() {
        assertEquals("dtrack.vulnerability.mirror.osv", KafkaTopic.MIRROR_OSV.getName());
    }
}
