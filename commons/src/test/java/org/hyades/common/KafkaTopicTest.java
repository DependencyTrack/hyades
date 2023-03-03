package org.hyades.common;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class KafkaTopicTest {

    @Test
    void testKafkaTopicConfig() {
        assertEquals("dtrack.vulnerability.mirror.osv", KafkaTopic.MIRROR_OSV.getName());
        System.setProperty("api.topic.prefix", "customPrefix.");
        assertEquals("customPrefix.dtrack.vulnerability.mirror.osv", KafkaTopic.MIRROR_OSV.getName());
    }
}
