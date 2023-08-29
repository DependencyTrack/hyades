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
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.hyades.notification.publisher;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.protobuf.Any;
import com.google.protobuf.util.Timestamps;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.hyades.proto.notification.v1.BackReference;
import org.hyades.proto.notification.v1.Bom;
import org.hyades.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.hyades.proto.notification.v1.Component;
import org.hyades.proto.notification.v1.NewVulnerabilitySubject;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.proto.notification.v1.Project;
import org.hyades.proto.notification.v1.Vulnerability;
import org.hyades.util.WireMockTestResource;
import org.hyades.util.WireMockTestResource.InjectWireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static org.hyades.notification.publisher.PublisherTestUtil.createPublisherContext;
import static org.hyades.notification.publisher.PublisherTestUtil.getConfig;
import static org.hyades.proto.notification.v1.Group.GROUP_BOM_PROCESSED;
import static org.hyades.proto.notification.v1.Group.GROUP_NEW_VULNERABILITY;
import static org.hyades.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.hyades.proto.notification.v1.Scope.SCOPE_PORTFOLIO;

@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
class WebhookPublisherTest {

    @Inject
    WebhookPublisher publisher;

    @InjectWireMock
    WireMockServer wireMockServer;

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    @Test
    @TestTransaction
    void testPublishNewVulnerabilityNotification() throws Exception {
        wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(202)));

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setLevel(LEVEL_INFORMATIONAL)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .setTimestamp(Timestamps.fromSeconds(666))
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(createComponent())
                        .setProject(createProject())
                        .setVulnerability(createVulnerability())
                        .setAffectedProjectsReference(BackReference.newBuilder()
                                .setApiUri("apiUriValue")
                                .setFrontendUri("frontendUriValue"))
                        .setVulnerabilityAnalysisLevel("BOM_UPLOAD")
                        .addAffectedProjects(createProject())
                        .build()))
                .build();

        publisher.inform(createPublisherContext(notification), notification, getPublisherConfig());

        wireMockServer.verify(postRequestedFor(anyUrl())
                .withRequestBody(equalToJson("""
                        {
                          "notification": {
                            "level": "LEVEL_INFORMATIONAL",
                            "scope": "SCOPE_PORTFOLIO",
                            "group": "GROUP_NEW_VULNERABILITY",
                            "timestamp": "1970-01-01T00:11:06Z",
                            "title": "Test Notification",
                            "content": "This is only a test",
                            "subject": {
                              "component": {
                                "uuid": "3c87b90d-d08a-492a-855e-d6e9d8b63a18",
                                "group": "componentGroup",
                                "name": "componentName",
                                "version": "componentVersion",
                                "purl": "componentPurl",
                                "md5": "componentMd5",
                                "sha1": "componentSha1",
                                "sha256": "componentSha256",
                                "sha512": "componentSha512"
                              },
                              "project": {
                                "uuid": "0957687b-3482-4891-a836-dad37e9b804a",
                                "name": "projectName",
                                "version": "projectVersion",
                                "description": "projectDescription",
                                "purl": "projectPurl",
                                "tags": [
                                  "tag-a",
                                  "tag-b"
                                ]
                              },
                              "vulnerability": {
                                "uuid": "418d9be1-f888-446a-8f03-f3253e5b5361",
                                "vulnId": "INT-001",
                                "source": "INTERNAL",
                                "aliases": [
                                  {
                                    "vulnId": "OSV-001",
                                    "source": "OSV"
                                  }
                                ],
                                "title": "vulnerabilityTitle",
                                "subtitle": "vulnerabilitySubTitle",
                                "description": "vulnerabilityDescription",
                                "recommendation": "vulnerabilityRecommendation",
                                "cvssv2": 5.5,
                                "cvssv3": 6.6,
                                "owaspRRLikelihood": 1.1,
                                "owaspRRTechnicalImpact": 2.2,
                                "owaspRRBusinessImpact": 3.3,
                                "cwes": [
                                  {
                                    "cweId": 666,
                                    "name": "Operation on Resource in Wrong Phase of Lifetime"
                                  }
                                ]
                              },
                              "affectedProjectsReference": {
                                "apiUri": "apiUriValue",
                                "frontendUri": "frontendUriValue"
                              },
                              "vulnerabilityAnalysisLevel": "BOM_UPLOAD",
                              "affectedProjects": [
                                {
                                  "uuid": "0957687b-3482-4891-a836-dad37e9b804a",
                                  "name": "projectName",
                                  "version": "projectVersion",
                                  "description": "projectDescription",
                                  "purl": "projectPurl",
                                  "tags": [
                                    "tag-a",
                                    "tag-b"
                                  ]
                                }
                              ]
                            }
                          }
                        }
                        """)));
    }

    @Test
    @TestTransaction
    void testPublishBomProcessedNotification() throws Exception {
        wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(202)));

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setLevel(LEVEL_INFORMATIONAL)
                .setGroup(GROUP_BOM_PROCESSED)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .setTimestamp(Timestamps.fromSeconds(666))
                .setSubject(Any.pack(BomConsumedOrProcessedSubject.newBuilder()
                        .setProject(createProject())
                        .setBom(Bom.newBuilder()
                                .setContent("bomContent")
                                .setFormat("bomFormat")
                                .setSpecVersion("bomSpecVersion"))
                        .build()))
                .build();

        publisher.inform(createPublisherContext(notification), notification, getPublisherConfig());

        wireMockServer.verify(postRequestedFor(anyUrl())
                .withRequestBody(equalToJson("""
                        {
                          "notification": {
                            "level": "LEVEL_INFORMATIONAL",
                            "scope": "SCOPE_PORTFOLIO",
                            "group": "GROUP_BOM_PROCESSED",
                            "timestamp": "1970-01-01T00:11:06Z",
                            "title": "Test Notification",
                            "content": "This is only a test",
                            "subject": {
                              "project": {
                                "uuid": "0957687b-3482-4891-a836-dad37e9b804a",
                                "name": "projectName",
                                "version": "projectVersion",
                                "description": "projectDescription",
                                "purl": "projectPurl",
                                "tags": [
                                  "tag-a",
                                  "tag-b"
                                ]
                              },
                              "bom": {
                                "content": "bomContent",
                                "format": "bomFormat",
                                "specVersion": "bomSpecVersion"
                              }
                            }
                          }
                        }
                        """)));
    }

    private JsonObject getPublisherConfig() {
        return getConfig("WEBHOOK", wireMockServer.baseUrl());
    }

    private Component createComponent() {
        return Component.newBuilder()
                .setUuid("3c87b90d-d08a-492a-855e-d6e9d8b63a18")
                .setGroup("componentGroup")
                .setName("componentName")
                .setVersion("componentVersion")
                .setPurl("componentPurl")
                .setMd5("componentMd5")
                .setSha1("componentSha1")
                .setSha256("componentSha256")
                .setSha512("componentSha512")
                .build();
    }

    private Project createProject() {
        return Project.newBuilder()
                .setUuid("0957687b-3482-4891-a836-dad37e9b804a")
                .setName("projectName")
                .setVersion("projectVersion")
                .setDescription("projectDescription")
                .setPurl("projectPurl")
                .addAllTags(List.of("tag-a", "tag-b"))
                .build();
    }

    private Vulnerability createVulnerability() {
        return Vulnerability.newBuilder()
                .setUuid("418d9be1-f888-446a-8f03-f3253e5b5361")
                .setVulnId("INT-001")
                .setSource("INTERNAL")
                .addCwes(Vulnerability.Cwe.newBuilder()
                        .setName("Operation on Resource in Wrong Phase of Lifetime")
                        .setCweId(666))
                .addAliases(Vulnerability.Alias.newBuilder()
                        .setId("OSV-001")
                        .setSource("OSV"))
                .setTitle("vulnerabilityTitle")
                .setSubTitle("vulnerabilitySubTitle")
                .setDescription("vulnerabilityDescription")
                .setRecommendation("vulnerabilityRecommendation")
                .setCvssV2(5.5)
                .setCvssV3(6.6)
                .setOwaspRrLikelihood(1.1)
                .setOwaspRrTechnicalImpact(2.2)
                .setOwaspRrBusinessImpact(3.3)
                .build();
    }

}
