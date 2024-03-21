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
package org.dependencytrack.vulnmirror.datasource.nvd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import io.github.jeremylong.openvulnerability.client.nvd.DefCveItem;
import io.github.jeremylong.openvulnerability.client.nvd.NvdApiException;
import io.github.jeremylong.openvulnerability.client.nvd.NvdCveClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import net.javacrumbs.jsonunit.core.Option;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.dependencytrack.common.KafkaTopic;
import org.dependencytrack.proto.KafkaProtobufSerde;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.vulnmirror.TestConstants;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.apache.commons.io.IOUtils.resourceToByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_DATASOURCE_MIRRORING;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_SYSTEM;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NvdMirrorTest {

    @Inject
    NvdMirror nvdMirror;

    @InjectMock
    NvdApiClientFactory apiClientFactoryMock;

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
                        new ProducerRecord<>(KafkaTopic.VULNERABILITY_DIGEST.getName(), "NVD/CVE-1999-1341", null),
                        new ProducerRecord<>(KafkaTopic.VULNERABILITY_DIGEST.getName(), "NVD/CVE-2022-40489", null)
                ))
                .awaitCompletion();
    }

    @Test
    void testDoMirrorSuccessNotification() {
        final var apiClientMock = mock(NvdCveClient.class);
        when(apiClientMock.hasNext())
                .thenReturn(false);

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> nvdMirror.doMirror(null).get());

        final List<ConsumerRecord<String, Notification>> notificationRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(notificationRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isNull();
                    assertThat(record.value().getScope()).isEqualTo(SCOPE_SYSTEM);
                    assertThat(record.value().getGroup()).isEqualTo(GROUP_DATASOURCE_MIRRORING);
                    assertThat(record.value().getLevel()).isEqualTo(LEVEL_INFORMATIONAL);
                    assertThat(record.value().getTitle()).isEqualTo("NVD Mirroring");
                    assertThat(record.value().getContent()).isEqualTo("Mirroring of the National Vulnerability Database completed successfully.");
                }
        );
    }

    @Test
    void testDoMirrorFailureNotification() {
        final var apiClientMock = mock(NvdCveClient.class);
        when(apiClientMock.hasNext())
                .thenReturn(true);
        when(apiClientMock.next())
                .thenThrow(new IllegalStateException());

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> nvdMirror.doMirror(null).get());

        final List<ConsumerRecord<String, Notification>> notificationRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(notificationRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isNull();
                    assertThat(record.value().getScope()).isEqualTo(SCOPE_SYSTEM);
                    assertThat(record.value().getGroup()).isEqualTo(GROUP_DATASOURCE_MIRRORING);
                    assertThat(record.value().getLevel()).isEqualTo(LEVEL_ERROR);
                    assertThat(record.value().getTitle()).isEqualTo("NVD Mirroring");
                    assertThat(record.value().getContent()).isEqualTo("An error occurred mirroring the contents of the National Vulnerability Database, cause being: java.lang.IllegalStateException. Check log for details.");
                }
        );
    }

    @Test
    @SuppressWarnings("resource")
    void testMirrorInternal() throws Exception {
        final var item = objectMapper.readValue(resourceToByteArray("/datasource/nvd/cve.json"), DefCveItem.class);

        final var apiClientMock = mock(NvdCveClient.class);
        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenReturn(List.of(item));
        when(apiClientMock.getLastUpdated())
                .thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1679922240L), ZoneOffset.UTC));

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> nvdMirror.mirrorInternal());
        verify(apiClientFactoryMock).createApiClient(eq(0L));

        final List<ConsumerRecord<String, Bom>> bovRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(bovRecords).satisfiesExactly(record -> {
            assertThat(record.key()).isEqualTo("NVD/CVE-2022-40489");
            assertThat(record.value()).isNotNull();
        });

        assertThatJson(JsonFormat.printer().print(bovRecords.get(0).value()))
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .withMatcher("vuln-description", Matchers.allOf(
                        Matchers.startsWith("ThinkCMF version 6.0.7 is affected by a"),
                        Matchers.hasLength(168)))
                .isEqualTo("""
                        {
                          "components": [
                            {
                              "bomRef": "02cd44fb-2f0a-569b-a508-1e179e123e38",
                              "type": "CLASSIFICATION_APPLICATION",
                              "publisher": "thinkcmf",
                              "name": "thinkcmf",
                              "cpe": "cpe:2.3:a:thinkcmf:thinkcmf:6.0.7:*:*:*:*:*:*:*"
                            }
                          ],
                          "vulnerabilities": [
                            {
                              "id": "CVE-2022-40489",
                              "source": { "name": "NVD" },
                              "description": "${json-unit.matches:vuln-description}",
                              "cwes": [ 352 ],
                              "published": "2022-12-01T05:15:11Z",
                              "updated": "2022-12-02T17:17:02Z",
                              "ratings": [
                                {
                                  "method": "SCORE_METHOD_CVSSV31",
                                  "score": 8.8,
                                  "severity": "SEVERITY_HIGH",
                                  "vector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:H/I:H/A:H",
                                  "source": { "name": "NVD" }
                                }
                              ],
                              "affects": [
                                {
                                  "ref": "02cd44fb-2f0a-569b-a508-1e179e123e38",
                                  "versions": [
                                    { "version": "6.0.7" }
                                  ]
                                }
                              ]
                            }
                          ],
                          "externalReferences": [
                            { "url": "https://github.com/thinkcmf/thinkcmf/issues/736" }
                          ]
                        }
                        """);

        // Trigger a mirror operation one more time.
        // Verify that this time the previously stored "last updated" timestamp is used.
        assertThatNoException().isThrownBy(() -> nvdMirror.mirrorInternal());
        verify(apiClientFactoryMock).createApiClient(eq(1679922240L));
    }

    @Test
    void testRetryInCaseOfTwoConsecutiveExceptions() throws IOException {
        final var apiClientMock = mock(NvdCveClient.class);
        final var item = objectMapper.readValue(resourceToByteArray("/datasource/nvd/cve.json"), DefCveItem.class);

        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenReturn(List.of(item));
        when(apiClientMock.getLastUpdated())
                .thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1679922240L), ZoneOffset.UTC));

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> nvdMirror.doMirror(null).get());
        final List<ConsumerRecord<String, Bom>> vulnRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(vulnRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isEqualTo("NVD/CVE-2022-40489");
                    assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                    final Vulnerability vuln = record.value().getVulnerabilities(0);
                    assertThat(vuln.getId()).isEqualTo("CVE-2022-40489");
                    assertThat(vuln.hasSource()).isTrue();
                    assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                }
        );

        // Trigger a mirror operation one more time.
        // Verify that this time the previously stored "last updated" timestamp is used.
        assertThatNoException().isThrownBy(() -> nvdMirror.mirrorInternal());
    }

    @Test
    void testRetryWithDoMirrorInCaseOfThreeConsecutiveExceptions() throws IOException {
        final var apiClientMock = mock(NvdCveClient.class);
        final var item = objectMapper.readValue(resourceToByteArray("/datasource/nvd/cve.json"), DefCveItem.class);

        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenReturn(List.of(item));
        when(apiClientMock.getLastUpdated())
                .thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1679922240L), ZoneOffset.UTC));

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);
        assertThatNoException().isThrownBy(() -> nvdMirror.doMirror(null).get());

        final List<ConsumerRecord<String, Notification>> notificationRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(notificationRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isNull();
                    assertThat(record.value().getScope()).isEqualTo(SCOPE_SYSTEM);
                    assertThat(record.value().getGroup()).isEqualTo(GROUP_DATASOURCE_MIRRORING);
                    assertThat(record.value().getLevel()).isEqualTo(LEVEL_ERROR);
                    assertThat(record.value().getTitle()).isEqualTo("NVD Mirroring");
                    assertThat(record.value().getContent()).contains("io.github.jeremylong.openvulnerability.client.nvd.NvdApiException");
                }
        );

    }

    @Test
    void testRetryWithMirrorInternalInCaseOfThreeConsecutiveExceptions() throws IOException {
        final var apiClientMock = mock(NvdCveClient.class);
        final var item = objectMapper.readValue(resourceToByteArray("/datasource/nvd/cve.json"), DefCveItem.class);

        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenReturn(List.of(item));

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> nvdMirror.mirrorInternal());

    }
}