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
                    "api.topic.prefix", "customPrefix."
            );
        }

        @Test
        void testKafkaTopicConfigWithPrefix() {
            System.setProperty("api.topic.prefix", "customPrefix.");
            assertEquals("customPrefix.dtrack.vulnerability.mirror.command", KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName());
        }
    }

    @Test
    void testKafkaTopicConfig() {
        assertEquals("dtrack.vulnerability.mirror.command", KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName());
    }
}
