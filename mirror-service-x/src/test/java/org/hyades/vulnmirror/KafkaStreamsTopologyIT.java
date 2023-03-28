package org.hyades.vulnmirror;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.http.HttpHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.hyades.common.KafkaTopic;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.util.WireMockTestResource;
import org.hyades.util.WireMockTestResource.InjectWireMock;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.http.Body.fromJsonBytes;
import static org.apache.commons.io.IOUtils.resourceToByteArray;
import static org.assertj.core.api.Assertions.assertThat;

@Suite
@SelectClasses(value = {
        KafkaStreamsTopologyIT.NvdMirrorIT.class,
        KafkaStreamsTopologyIT.GitHubMirrorIT.class
})
class KafkaStreamsTopologyIT {

    @QuarkusIntegrationTest
    @QuarkusTestResource(KafkaCompanionResource.class)
    @QuarkusTestResource(value = WireMockTestResource.class, initArgs = {
            @ResourceArg(name = "serverUrlProperty", value = "mirror.datasource.nvd.base-url")
    })
    static class NvdMirrorIT {

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

        @InjectWireMock
        WireMockServer wireMock;

        @Test
        void test() throws Exception {
            // Simulate the first page of CVEs, containing 2 CVEs.
            wireMock.stubFor(get(urlPathEqualTo("/"))
                    .withQueryParam("startIndex", equalTo("0"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(fromJsonBytes(resourceToByteArray("/datasource/nvd/cves-page-01.json")))));

            // Simulate the second page of CVEs, containing only one item.
            // NOTE: The nvd-lib library will request pages of 2000 items each,
            // that's why we're expecting a startIndex=2000 parameter here.
            wireMock.stubFor(get(urlPathEqualTo("/"))
                    .withQueryParam("startIndex", equalTo("2000"))
                    .willReturn(aResponse()
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(fromJsonBytes(resourceToByteArray("/datasource/nvd/cves-page-02.json")))));

            // Trigger a NVD mirroring operation.
            kafkaCompanion
                    .produce(Serdes.String(), Serdes.String())
                    .fromRecords(new ProducerRecord<>(KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName(), "NVD", null));

            // Wait for all expected vulnerability records; There should be one for each CVE.
            final List<ConsumerRecord<String, Bom>> results = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                    .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 3, Duration.ofSeconds(15))
                    .awaitCompletion()
                    .getRecords();

            // Ensure the vulnerability details are correct.
            assertThat(results).satisfiesExactlyInAnyOrder(
                    record -> {
                        assertThat(record.key()).isEqualTo("NVD/CVE-2022-40489");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("CVE-2022-40489");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("NVD/CVE-2022-40849");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("CVE-2022-40849");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("NVD/CVE-2022-44262");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("CVE-2022-44262");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                    }
            );

            // Wait for the notification that reports the successful mirroring operation.
            final List<ConsumerRecord<String, Notification>> notifications = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                    .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                    .awaitCompletion()
                    .getRecords();
            assertThat(notifications).hasSize(1);
        }

    }

    @QuarkusIntegrationTest
    @TestProfile(GitHubMirrorIT.TestProfile.class)
    @QuarkusTestResource(KafkaCompanionResource.class)
    @QuarkusTestResource(value = WireMockTestResource.class, initArgs = {
            @ResourceArg(name = "serverUrlProperty", value = "mirror.datasource.github.base-url")
    })
    static class GitHubMirrorIT {

        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of("mirror.datasource.github.api-key", "foobar");
            }
        }

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

        @InjectWireMock
        WireMockServer wireMock;

        @Test
        void test() throws Exception {
            wireMock.stubFor(any(anyUrl())
                    .willReturn(aResponse()
                            .withStatus(418)));

            // Simulate the first page of CVEs, containing 2 CVEs.
            wireMock.stubFor(post(anyUrl())
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(fromJsonBytes(resourceToByteArray("/datasource/github/advisories-page-01.json")))));

            // Simulate the second page of CVEs, containing only one item.
            // NOTE: The nvd-lib library will request pages of 2000 items each,
            // that's why we're expecting a startIndex=2000 parameter here.
            wireMock.stubFor(post(anyUrl())
                    .inScenario("advisories-paging")
                    .whenScenarioStateIs("first-page-fetched")
                    .willReturn(aResponse()
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(fromJsonBytes(resourceToByteArray("/datasource/github/advisories-page-02.json")))));

            // Trigger a NVD mirroring operation.
            kafkaCompanion
                    .produce(Serdes.String(), Serdes.String())
                    .fromRecords(new ProducerRecord<>(KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName(), "GITHUB", null));

            // Wait for all expected vulnerability records; There should be one for each CVE.
            final List<ConsumerRecord<String, Bom>> results = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                    .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 3, Duration.ofSeconds(15))
                    .awaitCompletion()
                    .getRecords();

            // Ensure the vulnerability details are correct.
            assertThat(results).satisfiesExactlyInAnyOrder(
                    record -> {
                        assertThat(record.key()).isEqualTo("NVD/CVE-2022-40489");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("CVE-2022-40489");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("NVD/CVE-2022-40849");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("CVE-2022-40849");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("NVD/CVE-2022-44262");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("CVE-2022-44262");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                    }
            );

            // Wait for the notification that reports the successful mirroring operation.
            final List<ConsumerRecord<String, Notification>> notifications = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                    .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                    .awaitCompletion()
                    .getRecords();
            assertThat(notifications).hasSize(1);
        }

    }

}