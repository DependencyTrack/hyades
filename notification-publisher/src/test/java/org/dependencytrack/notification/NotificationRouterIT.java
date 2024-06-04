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
package org.dependencytrack.notification;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.protobuf.Any;
import com.google.protobuf.util.Timestamps;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkiverse.wiremock.devservice.WireMockConfigKey;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.awaitility.Awaitility;
import org.dependencytrack.common.KafkaTopic;
import org.dependencytrack.notification.publisher.PublisherTestUtil;
import org.dependencytrack.proto.KafkaProtobufSerde;
import org.dependencytrack.proto.notification.v1.Component;
import org.dependencytrack.proto.notification.v1.Group;
import org.dependencytrack.proto.notification.v1.Level;
import org.dependencytrack.proto.notification.v1.NewVulnerabilitySubject;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.proto.notification.v1.Project;
import org.dependencytrack.proto.notification.v1.Scope;
import org.dependencytrack.proto.notification.v1.Vulnerability;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@QuarkusIntegrationTest
@TestProfile(NotificationRouterIT.TestProfile.class)
@QuarkusTestResource(KafkaCompanionResource.class)
@ConnectWireMock
class NotificationRouterIT {

    public static class TestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("client.http.config.proxy-timeout-socket", "2");
        }

    }

    @InjectKafkaCompanion
    KafkaCompanion kafkaCompanion;

    WireMock wireMock;

    DevServicesContext devServicesContext;

    @BeforeEach
    void beforeEach() throws Exception {
        try (final Connection connection = DriverManager.getConnection(
                ConfigProvider.getConfig().getValue("quarkus.datasource.jdbc.url", String.class),
                ConfigProvider.getConfig().getValue("quarkus.datasource.username", String.class),
                ConfigProvider.getConfig().getValue("quarkus.datasource.password", String.class))) {
            final PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO "NOTIFICATIONPUBLISHER"
                        ("ID", "DEFAULT_PUBLISHER", "NAME", "PUBLISHER_CLASS", "TEMPLATE", "TEMPLATE_MIME_TYPE", "UUID")
                    VALUES
                        (1, TRUE, 'foo', 'org.dependencytrack.notification.publisher.WebhookPublisher', ?, 'application/json', '1781db56-51a8-462a-858c-6030a2341dfc');
                    """);
            ps.setString(1, PublisherTestUtil.getTemplateContent("WEBHOOK"));
            ps.execute();

            int port = Integer.parseInt(devServicesContext.devServicesProperties().get(WireMockConfigKey.PORT));
            connection.createStatement().execute("""
                    INSERT INTO "NOTIFICATIONRULE" ("ID", "ENABLED", "NAME", "PUBLISHER", "NOTIFY_ON", "NOTIFY_CHILDREN", "LOG_SUCCESSFUL_PUBLISH", "NOTIFICATION_LEVEL", "SCOPE", "UUID", "PUBLISHER_CONFIG") VALUES
                    (1, true, 'foo', 1, 'NEW_VULNERABILITY', false, false, 'INFORMATIONAL', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62', '{"destination": "http://localhost:%d/foo"}');
                    """.formatted(port));
        }
    }

    @Test
    void test() {
        wireMock.register(post(urlEqualTo("/foo"))
                .inScenario("notification-delivery")
                .willReturn(aResponse()
                        .withStatus(204)
                        .withFixedDelay(5 * 1000))
                .willSetStateTo("first-attempt-timeout"));

        wireMock.register(post(urlEqualTo("/foo"))
                .inScenario("notification-delivery")
                .whenScenarioStateIs("first-attempt-timeout")
                .willReturn(aResponse()
                        .withStatus(204)));

        final var notification = Notification.newBuilder()
                .setScope(Scope.SCOPE_PORTFOLIO)
                .setLevel(Level.LEVEL_INFORMATIONAL)
                .setGroup(Group.GROUP_NEW_VULNERABILITY)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .setTimestamp(Timestamps.fromSeconds(666))
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid("componentUuid")
                                .setGroup("componentGroup")
                                .setName("componentName")
                                .setVersion("componentVersion"))
                        .setProject(Project.newBuilder()
                                .setUuid("projectUuid")
                                .setName("projectName")
                                .setVersion("projectVersion"))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid("vulnUuid")
                                .setVulnId("vulnId")
                                .setSource("vulnSource"))
                        .build()))
                .build();

        kafkaCompanion
                .produce(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                .fromRecords(new ProducerRecord<>(KafkaTopic.NOTIFICATION_NEW_VULNERABILITY.getName(), "", notification));

        Awaitility.await()
                .atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> wireMock.verifyThat(2, postRequestedFor(urlPathEqualTo("/foo"))));

        wireMock.verifyThat(postRequestedFor(urlPathEqualTo("/foo"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "notification" : {
                            "level" : "LEVEL_INFORMATIONAL",
                            "scope" : "SCOPE_PORTFOLIO",
                            "group" : "GROUP_NEW_VULNERABILITY",
                            "timestamp" : "1970-01-01T00:11:06.000Z",
                            "title" : "Test Notification",
                            "content" : "This is only a test",
                            "subject" : {
                              "component" : {
                                "uuid" : "componentUuid",
                                "group" : "componentGroup",
                                "name" : "componentName",
                                "version" : "componentVersion"
                              },
                              "project" : {
                                "uuid" : "projectUuid",
                                "name" : "projectName",
                                "version" : "projectVersion"
                              },
                              "vulnerability" : {
                                "uuid" : "vulnUuid",
                                "vulnId" : "vulnId",
                                "source" : "vulnSource"
                              }
                            }
                          }
                        }
                        """)));
    }

}