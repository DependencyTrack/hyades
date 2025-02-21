package org.dependencytrack.vulnmirror.datasource.csaf;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.dependencytrack.common.KafkaTopic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@QuarkusTest
    @TestProfile(CsafMirrorTest.TestProfile.class)
    @QuarkusTestResource(KafkaCompanionResource.class)
    class CsafMirrorTest {

        public static final class TestProfile implements QuarkusTestProfile {

            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.ofEntries(
                        Map.entry("dtrack.vuln-source.csaf.advisories.enabled", "true")
                );
            }

        }

        @Inject
        CsafMirror csafMirror;

        @Inject
        @Default
        ObjectMapper objectMapper;

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

        @AfterEach
        void afterEach() {
            // Publish tombstones to the vulnerability digest topic for all vulnerabilities used in this test.
            kafkaCompanion.produce(Serdes.String(), Serdes.ByteArray())
                    .fromRecords(List.of(
                            new ProducerRecord<>(KafkaTopic.VULNERABILITY_DIGEST.getName(), "CSAF/CSAF-12345678-TRACKINGID-VULNERABILITY0", null)
                    ))
                    .awaitCompletion();
        }

    @Test
    void testDoMirrorSuccessNotification() {
        /*final var apiClientMock = mock(GitHubSecurityAdvisoryClient.class);
        when(apiClientMock.hasNext())
                .thenReturn(false);

        assertThatNoException().isThrownBy(() -> csafMirror.doMirror().get());*/
    }
}
