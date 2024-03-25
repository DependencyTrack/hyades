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
package org.dependencytrack.vulnmirror;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.dependencytrack.common.KafkaTopic;
import org.dependencytrack.proto.KafkaProtobufSerde;
import org.dependencytrack.proto.mirror.v1.EpssItem;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.repometaanalyzer.util.WireMockTestResource;
import org.dependencytrack.repometaanalyzer.util.WireMockTestResource.InjectWireMock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.http.Body.fromJsonBytes;
import static org.apache.commons.io.IOUtils.resourceToByteArray;
import static org.assertj.core.api.Assertions.assertThat;

@Suite
@SelectClasses(value = {
        KafkaStreamsTopologyIT.NvdMirrorIT.class,
        KafkaStreamsTopologyIT.GitHubMirrorIT.class,
        KafkaStreamsTopologyIT.OsvMirrorIT.class,
        KafkaStreamsTopologyIT.EpssMirrorIT.class
})
class KafkaStreamsTopologyIT {

    @Nested
    @QuarkusIntegrationTest
    @TestProfile(NvdMirrorIT.TestProfile.class)
    class NvdMirrorIT {

        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public List<TestResourceEntry> testResources() {
                return List.of(
                        new TestResourceEntry(KafkaCompanionResource.class),
                        new TestResourceEntry(
                                WireMockTestResource.class,
                                Map.of("serverUrlProperty", "mirror.datasource.nvd.base-url")
                        ));
            }
        }

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
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
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
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
                    .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                    .awaitCompletion()
                    .getRecords();
            assertThat(notifications).hasSize(1);
        }

    }

    @Nested
    @QuarkusIntegrationTest
    @TestProfile(OsvMirrorIT.TestProfile.class)
    class OsvMirrorIT {

        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public List<TestResourceEntry> testResources() {
                return List.of(
                        new TestResourceEntry(KafkaCompanionResource.class),
                        new TestResourceEntry(
                                WireMockTestResource.class,
                                Map.of("serverUrlProperty", "mirror.datasource.osv.base-url")
                        ));
            }
        }

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

        @InjectWireMock
        WireMockServer wireMock;

        @Test
        void test() throws Exception {
            // Simulate the first page of CVEs, containing 2 CVEs.
            wireMock.stubFor(get(urlPathMatching("/.*"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(Body.ofBinaryOrText(resourceToByteArray("/datasource/osv/maven.zip"), new ContentTypeHeader(MediaType.APPLICATION_OCTET_STREAM)))));
            // Trigger a OSV mirroring operation.
            kafkaCompanion
                    .produce(Serdes.String(), Serdes.String())
                    .fromRecords(new ProducerRecord<>(KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName(), "OSV", "Maven"));

            // Wait for all expected vulnerability records; There should be one for each CVE.
            final List<ConsumerRecord<String, Bom>> results = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
                    .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 2, Duration.ofSeconds(15))
                    .awaitCompletion()
                    .getRecords();

            // Ensure the vulnerability details are correct.
            assertThat(results).satisfiesExactlyInAnyOrder(
                    record -> {
                        assertThat(record.key()).isEqualTo("OSV/GHSA-2cc5-23r7-vc4v");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("GHSA-2cc5-23r7-vc4v");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("OSV/GHSA-2cfc-865j-gm4w");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("GHSA-2cfc-865j-gm4w");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                    }
            );

            // Wait for the notification that reports the successful mirroring operation.
            final List<ConsumerRecord<String, Notification>> notifications = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
                    .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                    .awaitCompletion()
                    .getRecords();
            assertThat(notifications).hasSize(1);
            assertThat(notifications.get(0).value().getContent()).isEqualToIgnoringCase("OSV mirroring completed for ecosystem: Maven");
        }

    }

    @Nested
    @QuarkusIntegrationTest
    @TestProfile(GitHubMirrorIT.TestProfile.class)
    class GitHubMirrorIT {

        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of("mirror.datasource.github.api-key", "foobar");
            }

            @Override
            public List<TestResourceEntry> testResources() {
                return List.of(
                        new TestResourceEntry(KafkaCompanionResource.class),
                        new TestResourceEntry(
                                WireMockTestResource.class,
                                Map.of("serverUrlProperty", "mirror.datasource.github.base-url")
                        ));
            }
        }

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

        @InjectWireMock
        WireMockServer wireMock;

        @Test
        void test() throws Exception {
            // Simulate the first page of advisories, containing 2 GHSAs.
            wireMock.stubFor(post(urlPathEqualTo("/"))
                    .inScenario("advisories-paging")
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(fromJsonBytes(resourceToByteArray("/datasource/github/advisories-page-01.json"))))
                    .willSetStateTo("first-page-fetched"));

            // Simulate the second page of advisories, containing only one advisory.
            wireMock.stubFor(post(urlPathEqualTo("/"))
                    .inScenario("advisories-paging")
                    .whenScenarioStateIs("first-page-fetched")
                    .willReturn(aResponse()
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(fromJsonBytes(resourceToByteArray("/datasource/github/advisories-page-02.json")))));

            // Trigger a GitHub mirroring operation.
            kafkaCompanion
                    .produce(Serdes.String(), Serdes.String())
                    .fromRecords(new ProducerRecord<>(KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName(), "GITHUB", null));

            // Wait for all expected vulnerability records; There should be one for each advisory.
            final List<ConsumerRecord<String, Bom>> results = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
                    .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 3, Duration.ofSeconds(15))
                    .awaitCompletion()
                    .getRecords();

            // Ensure the vulnerability details are correct.
            assertThat(results).satisfiesExactlyInAnyOrder(
                    record -> {
                        assertThat(record.key()).isEqualTo("GITHUB/GHSA-fxwm-579q-49qq");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("GHSA-fxwm-579q-49qq");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("GITHUB/GHSA-wh77-3x4m-4q9g");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("GHSA-wh77-3x4m-4q9g");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("GITHUB/GHSA-p82g-2xpp-m5r3");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("GHSA-p82g-2xpp-m5r3");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                    }
            );

            // Verify that the API key was used.
            wireMock.verify(postRequestedFor(urlPathEqualTo("/"))
                    .withHeader(HttpHeaders.AUTHORIZATION, equalToIgnoreCase("bearer foobar")));

            // Wait for the notification that reports the successful mirroring operation.
            final List<ConsumerRecord<String, Notification>> notifications = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
                    .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                    .awaitCompletion()
                    .getRecords();
            assertThat(notifications).hasSize(1);
        }

    }

    @Nested
    @QuarkusIntegrationTest
    @TestProfile(OsvMirrorIT.TestProfile.class)
    class OsvMirrorCommaSeparatedListOfEcoSystemsIT {

        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public List<TestResourceEntry> testResources() {
                return List.of(
                        new TestResourceEntry(KafkaCompanionResource.class),
                        new TestResourceEntry(
                                WireMockTestResource.class,
                                Map.of("serverUrlProperty", "mirror.datasource.osv.base-url")
                        ));
            }
        }

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

        @InjectWireMock
        WireMockServer wireMock;

        @Test
        void test() throws Exception {
            // Simulate the first page of CVEs, containing 2 CVEs.
            wireMock.stubFor(get(urlPathEqualTo("/Maven/all.zip"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(Body.ofBinaryOrText(resourceToByteArray("/datasource/osv/maven.zip"), new ContentTypeHeader(MediaType.APPLICATION_OCTET_STREAM)))));
            wireMock.stubFor(get(urlPathEqualTo("/Go/all.zip"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(Body.ofBinaryOrText(resourceToByteArray("/datasource/osv/go.zip"), new ContentTypeHeader(MediaType.APPLICATION_OCTET_STREAM)))));
            // Trigger a OSV mirroring operation.
            kafkaCompanion
                    .produce(Serdes.String(), Serdes.String())
                    .fromRecords(new ProducerRecord<>(KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName(), "OSV", "Maven,Go"));

            // Wait for all expected vulnerability records; There should be one for each CVE.
            final List<ConsumerRecord<String, Bom>> results = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
                    .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 4, Duration.ofSeconds(15))
                    .awaitCompletion()
                    .getRecords();

            // Ensure the vulnerability details are correct.
            assertThat(results).satisfiesExactlyInAnyOrder(
                    record -> {
                        assertThat(record.key()).isEqualTo("OSV/GHSA-2cc5-23r7-vc4v");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("GHSA-2cc5-23r7-vc4v");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("OSV/GHSA-2cfc-865j-gm4w");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("GHSA-2cfc-865j-gm4w");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("OSV/GHSA-2jx2-76rc-2v7v");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("GHSA-2jx2-76rc-2v7v");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("OSV/GHSA-2jhh-5xm2-j4gf");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("GHSA-2jhh-5xm2-j4gf");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                    }
            );

            // Wait for the notification that reports the successful mirroring operation.
            final List<ConsumerRecord<String, Notification>> notifications = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
                    .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 2, Duration.ofSeconds(5))
                    .awaitCompletion()
                    .getRecords();
            assertThat(notifications).hasSize(2);
            assertThat(notifications.get(0).value().getContent()).isEqualToIgnoringCase("OSV mirroring completed for ecosystem: Maven");
            assertThat(notifications.get(1).value().getContent()).isEqualToIgnoringCase("OSV mirroring completed for ecosystem: Go");
        }

    }

    @Nested
    @QuarkusIntegrationTest
    @TestProfile(EpssMirrorIT.TestProfile.class)
    class EpssMirrorIT {

        public static class TestProfile implements QuarkusTestProfile {

            @Override
            public List<TestResourceEntry> testResources() {
                return List.of(
                        new TestResourceEntry(KafkaCompanionResource.class),
                        new TestResourceEntry(WireMockTestResource.class,
                                Map.of("serverUrlProperty", "mirror.datasource.epss.download-url")));
            }
        }

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

        @InjectWireMock
        WireMockServer wireMock;

        @Test
        void test() throws IOException {
            // Simulate list of eppsItems containing 2 records.
            wireMock.stubFor(get(anyUrl())
                    .inScenario("epss-download")
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(Body.ofBinaryOrText(resourceToByteArray("/datasource/epss/epss-items.tar.gz"), new ContentTypeHeader(MediaType.APPLICATION_OCTET_STREAM))))
                    .willSetStateTo("epss-fetched"));


            // Trigger a EPSS mirroring operation.
            kafkaCompanion
                    .produce(Serdes.String(), Serdes.String())
                    .fromRecords(new ProducerRecord<>(KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName(), "EPSS", null));

            // Wait for all expected vulnerability records; There should be one for each advisory.
            final List<ConsumerRecord<String, EpssItem>> results = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(EpssItem.parser()))
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
                    .fromTopics(KafkaTopic.VULNERABILITY_MIRROR_EPSS.getName(), 2, Duration.ofSeconds(15))
                    .awaitCompletion()
                    .getRecords();

            // Ensure the EPSS details are correct.
            assertThat(results).satisfiesExactlyInAnyOrder(
                    record -> {
                        assertThat(record.key()).isEqualTo("CVE-123");
                        final EpssItem epssItem = record.value();
                        assertThat(epssItem.getEpss()).isEqualTo(1.2);
                        assertThat(epssItem.getPercentile()).isEqualTo(3.4);
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("CVE-456");
                        final EpssItem epssItem = record.value();
                        assertThat(epssItem.getEpss()).isEqualTo(6.7);
                        assertThat(epssItem.getPercentile()).isEqualTo(8.9);
                    }
            );

            // Wait for the notification that reports the successful mirroring operation.
            final List<ConsumerRecord<String, Notification>> notifications = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                    .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                    .withAutoCommit()
                    .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                    .awaitCompletion()
                    .getRecords();
            assertThat(notifications).hasSize(1);
        }
    }
}