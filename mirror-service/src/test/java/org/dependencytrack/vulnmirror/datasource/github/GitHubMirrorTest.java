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
package org.dependencytrack.vulnmirror.datasource.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClient;
import io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisory;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import net.javacrumbs.jsonunit.core.Option;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.cyclonedx.proto.v1_6.Bom;
import org.dependencytrack.common.KafkaTopic;
import org.dependencytrack.proto.KafkaProtobufSerde;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.vulnmirror.TestConstants;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.apache.commons.io.IOUtils.resourceToByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_DATASOURCE_MIRRORING;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_SYSTEM;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(GitHubMirrorTest.TestProfile.class)
@QuarkusTestResource(KafkaCompanionResource.class)
class GitHubMirrorTest {

    public static final class TestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.ofEntries(
                    Map.entry("dtrack.vuln-source.github.advisories.enabled", "true")
            );
        }

    }

    @Inject
    GitHubMirror githubMirror;

    @InjectMock
    GitHubApiClientFactory apiClientFactoryMock;

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
                        new ProducerRecord<>(KafkaTopic.VULNERABILITY_DIGEST.getName(), "GITHUB/GHSA-fxwm-579q-49qq", null)
                ))
                .awaitCompletion();
    }

    @Test
    void testDoMirrorSuccessNotification() {
        final var apiClientMock = mock(GitHubSecurityAdvisoryClient.class);
        when(apiClientMock.hasNext())
                .thenReturn(false);

        when(apiClientFactoryMock.create(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> githubMirror.doMirror().get());

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
                    assertThat(record.value().getTitle()).isEqualTo("GitHub Advisory Mirroring");
                    assertThat(record.value().getContent()).isEqualTo("Mirroring of GitHub Advisories completed successfully.");
                }
        );
    }

    @Test
    void testDoMirrorFailureNotification() {
        final var apiClientMock = mock(GitHubSecurityAdvisoryClient.class);
        when(apiClientMock.hasNext())
                .thenReturn(true);
        when(apiClientMock.next())
                .thenThrow(new IllegalStateException());

        when(apiClientFactoryMock.create(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> githubMirror.doMirror().get());

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
                    assertThat(record.value().getTitle()).isEqualTo("GitHub Advisory Mirroring");
                    assertThat(record.value().getContent()).isEqualTo("An error occurred mirroring the contents of GitHub Advisories. Check log for details.");
                }
        );
    }

    @Test
    void testMirrorInternal() throws Exception {
        final var advisory = objectMapper.readValue(resourceToByteArray("/datasource/github/advisory.json"), SecurityAdvisory.class);

        final var apiClientMock = mock(GitHubSecurityAdvisoryClient.class);
        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenReturn(List.of(advisory));
        when(apiClientMock.getLastUpdated())
                .thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1679922240L), ZoneOffset.UTC));

        when(apiClientFactoryMock.create(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> githubMirror.mirrorInternal());
        verify(apiClientFactoryMock).create(eq(0L));

        final List<ConsumerRecord<String, Bom>> vulnRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(vulnRecords).satisfiesExactly(record -> {
            assertThat(record.key()).isEqualTo("GITHUB/GHSA-fxwm-579q-49qq");
            assertThat(record.value()).isNotNull();
        });

        assertThatJson(JsonFormat.printer().print(vulnRecords.get(0).value()))
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .withMatcher("vuln-description", Matchers.allOf(
                        Matchers.startsWith("In Bootstrap 4 before 4.3.1 and Bootstrap 3 before 3.4.1,"),
                        Matchers.hasLength(219)))
                .isEqualTo("""
                        {
                          "components": [
                            {
                              "bomRef": "3c41e06b-5923-5392-a1e3-64a630c97591",
                              "purl": "pkg:nuget/bootstrap"
                            },
                            {
                              "bomRef": "e5dc290a-c649-5f73-b814-c9a47690a48a",
                              "purl": "pkg:nuget/bootstrap.sass"
                            },
                            {
                              "bomRef": "c8e5d671-0b0d-5fda-a404-730615325a7f",
                              "purl": "pkg:nuget/Bootstrap.Less"
                            }
                          ],
                          "vulnerabilities": [
                            {
                              "id": "GHSA-fxwm-579q-49qq",
                              "source": { "name": "GITHUB" },
                              "description": "${json-unit.matches:vuln-description}",
                              "properties": [
                                  {
                                    "name": "dependency-track:vuln:title",
                                    "value": "Moderate severity vulnerability that affects Bootstrap.Less, bootstrap, and bootstrap.sass"
                                  }
                              ],
                              "published": "2019-02-22T20:54:40Z",
                              "updated": "2021-12-03T14:54:43Z",
                              "ratings": [
                                {
                                  "method": "SCORE_METHOD_OTHER",
                                  "severity": "SEVERITY_MEDIUM",
                                  "source": { "name": "GITHUB" }
                                }
                              ],
                              "affects": [
                                {
                                  "ref": "3c41e06b-5923-5392-a1e3-64a630c97591",
                                  "versions": [
                                    { "range": "vers:nuget/>=3.0.0|<3.4.1" },
                                    { "range": "vers:nuget/>=4.0.0|<4.3.1" }
                                  ]
                                },
                                {
                                  "ref": "e5dc290a-c649-5f73-b814-c9a47690a48a",
                                  "versions": [
                                    { "range": "vers:nuget/<4.3.1" }
                                  ]
                                },
                                {
                                  "ref": "c8e5d671-0b0d-5fda-a404-730615325a7f",
                                  "versions": [
                                    { "range": "vers:nuget/>=3.0.0|<3.4.1" }
                                  ]
                                }
                              ]
                            }
                          ],
                          "externalReferences": [
                            { "url": "https://github.com/advisories/GHSA-fxwm-579q-49qq" }
                          ]
                        }
                        """);

        // Trigger a mirror operation one more time.
        // Verify that this time the previously stored "last updated" timestamp is used.
        assertThatNoException().isThrownBy(() -> githubMirror.mirrorInternal());
        verify(apiClientFactoryMock).create(eq(1679922240L));
    }

    @Test
    void testMirrorWithdrawnAdvisory() throws Exception {
        final var advisory = objectMapper.readValue(resourceToByteArray("/datasource/github/advisory-withdrawn.json"), SecurityAdvisory.class);
        final var apiClientMock = mock(GitHubSecurityAdvisoryClient.class);
        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenReturn(List.of(advisory));
        when(apiClientMock.getLastUpdated())
                .thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1679922240L), ZoneOffset.UTC));
        when(apiClientFactoryMock.create(anyLong()))
                .thenReturn(apiClientMock);
        githubMirror.mirrorInternal();
        final List<ConsumerRecord<String, Bom>> vulnRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();
        assertThat(vulnRecords.size()).isEqualTo(0);
    }
}