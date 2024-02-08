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
package org.dependencytrack.notification.publisher;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;

@QuarkusTest
class WebhookPublisherTest extends AbstractWebhookPublisherTest<WebhookPublisher> {

    @Override
    @TestTransaction
    void testInformWithBomConsumedNotification() throws Exception {
        super.testInformWithBomConsumedNotification();

        wireMockServer.verify(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "notification": {
                            "level": "LEVEL_INFORMATIONAL",
                            "scope": "SCOPE_PORTFOLIO",
                            "group": "GROUP_BOM_CONSUMED",
                            "timestamp": "1970-01-01T18:31:06.000Z",
                            "title": "Bill of Materials Consumed",
                            "content": "A CycloneDX BOM was consumed and will be processed",
                            "subject": {
                              "project": {
                                "uuid": "c9c9539a-e381-4b36-ac52-6a7ab83b2c95",
                                "name": "projectName",
                                "version": "projectVersion",
                                "description": "projectDescription",
                                "purl": "pkg:maven/org.acme/projectName@projectVersion",
                                "tags": [
                                  "tag1",
                                  "tag2"
                                ]
                              },
                              "bom": {
                                "content": "bomContent",
                                "format": "CycloneDX",
                                "specVersion": "1.5"
                              }
                            }
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotification() throws Exception {
        super.testInformWithBomProcessingFailedNotification();

        wireMockServer.verify(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "notification" : {
                            "level": "LEVEL_ERROR",
                            "scope": "SCOPE_PORTFOLIO",
                            "group": "GROUP_BOM_PROCESSING_FAILED",
                            "timestamp": "1970-01-01T18:31:06.000Z",
                            "title": "Bill of Materials Processing Failed",
                            "content": "An error occurred while processing a BOM",
                            "subject": {
                              "project": {
                                "uuid": "c9c9539a-e381-4b36-ac52-6a7ab83b2c95",
                                "name": "projectName",
                                "version": "projectVersion",
                                "description": "projectDescription",
                                "purl": "pkg:maven/org.acme/projectName@projectVersion",
                                "tags": [
                                  "tag1",
                                  "tag2"
                                ]
                              },
                              "bom": {
                                "content": "bomContent",
                                "format": "CycloneDX",
                                "specVersion": "1.5"
                              },
                              "cause": "cause"
                            }
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject() throws Exception {
        super.testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject();

        wireMockServer.verify(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "notification" : {
                            "level": "LEVEL_ERROR",
                            "scope": "SCOPE_PORTFOLIO",
                            "group": "GROUP_BOM_PROCESSING_FAILED",
                            "timestamp": "1970-01-01T18:31:06.000Z",
                            "title": "Bill of Materials Processing Failed",
                            "content": "An error occurred while processing a BOM",
                            "subject": {
                              "project": {
                                "uuid": "c9c9539a-e381-4b36-ac52-6a7ab83b2c95",
                                "name": "projectName",
                                "version": "projectVersion",
                                "description": "projectDescription",
                                "purl": "pkg:maven/org.acme/projectName@projectVersion",
                                "tags": [
                                  "tag1",
                                  "tag2"
                                ]
                              },
                              "bom": {
                                "content": "bomContent",
                                "format": "CycloneDX"
                              },
                              "cause": "cause"
                            }
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithDataSourceMirroringNotification() throws Exception {
        super.testInformWithDataSourceMirroringNotification();

        wireMockServer.verify(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "notification": {
                            "level": "LEVEL_ERROR",
                            "scope": "SCOPE_SYSTEM",
                            "group": "GROUP_DATASOURCE_MIRRORING",
                            "timestamp": "1970-01-01T18:31:06.000Z",
                            "title": "GitHub Advisory Mirroring",
                            "content": "An error occurred mirroring the contents of GitHub Advisories. Check log for details.",
                            "subject": null
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithNewVulnerabilityNotification() throws Exception {
        super.testInformWithNewVulnerabilityNotification();

        wireMockServer.verify(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "notification": {
                            "level": "LEVEL_INFORMATIONAL",
                            "scope": "SCOPE_PORTFOLIO",
                            "group": "GROUP_NEW_VULNERABILITY",
                            "timestamp": "1970-01-01T18:31:06.000Z",
                            "title": "New Vulnerability Identified",
                            "content": "",
                            "subject": {
                              "component": {
                                "uuid": "94f87321-a5d1-4c2f-b2fe-95165debebc6",
                                "name": "componentName",
                                "version": "componentVersion"
                              },
                              "project": {
                                "uuid": "c9c9539a-e381-4b36-ac52-6a7ab83b2c95",
                                "name": "projectName",
                                "version": "projectVersion",
                                "description": "projectDescription",
                                "purl": "pkg:maven/org.acme/projectName@projectVersion",
                                "tags": [ "tag1", "tag2" ]
                              },
                              "vulnerabilityAnalysisLevel": "BOM_UPLOAD_ANALYSIS",
                              "vulnerability": {
                                "uuid": "bccec5d5-ec21-4958-b3e8-22a7a866a05a",
                                "vulnId": "INT-001",
                                "source": "INTERNAL",
                                "aliases": [
                                  {
                                    "source": "OSV",
                                    "vulnId": "OSV-001"
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
                                "severity": "MEDIUM",
                                "cwes": [
                                  {
                                    "cweId": 666,
                                    "name": "Operation on Resource in Wrong Phase of Lifetime"
                                  },
                                  {
                                    "cweId": 777,
                                    "name": "Regular Expression without Anchors"
                                  }
                                ]
                              },
                              "affectedProjects": [
                                {
                                  "uuid": "c9c9539a-e381-4b36-ac52-6a7ab83b2c95",
                                  "name": "projectName",
                                  "version": "projectVersion",
                                  "description": "projectDescription",
                                  "purl": "pkg:maven/org.acme/projectName@projectVersion",
                                  "tags": [
                                    "tag1",
                                    "tag2"
                                  ]
                                }
                              ],
                              "affectedProjectsReference" : {
                                "apiUri" : "/api/v1/foo",
                                "frontendUri" : "/foo"
                              }
                            }
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithProjectAuditChangeNotification() throws Exception {
        super.testInformWithProjectAuditChangeNotification();

        wireMockServer.verify(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "notification": {
                            "level": "LEVEL_INFORMATIONAL",
                            "scope": "SCOPE_PORTFOLIO",
                            "group": "GROUP_PROJECT_AUDIT_CHANGE",
                            "timestamp": "1970-01-01T18:31:06.000Z",
                            "title": "Analysis Decision: Finding Suppressed",
                            "content": "",
                            "subject": {
                              "component": {
                                "uuid": "94f87321-a5d1-4c2f-b2fe-95165debebc6",
                                "name": "componentName",
                                "version": "componentVersion"
                              },
                              "project" : {
                                "uuid" : "c9c9539a-e381-4b36-ac52-6a7ab83b2c95",
                                "name" : "projectName",
                                "version" : "projectVersion",
                                "description" : "projectDescription",
                                "purl" : "pkg:maven/org.acme/projectName@projectVersion",
                                "tags" : [ "tag1", "tag2" ]
                              },
                              "vulnerability": {
                                "uuid": "bccec5d5-ec21-4958-b3e8-22a7a866a05a",
                                "vulnId": "INT-001",
                                "source": "INTERNAL",
                                "aliases": [
                                  {
                                    "source": "OSV",
                                    "vulnId": "OSV-001"
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
                                "severity": "MEDIUM",
                                "cwes": [
                                  {
                                    "cweId": 666,
                                    "name": "Operation on Resource in Wrong Phase of Lifetime"
                                  },
                                  {
                                    "cweId": 777,
                                    "name": "Regular Expression without Anchors"
                                  }
                                ]
                              },
                              "analysis": {
                                "suppressed": true,
                                "state": "FALSE_POSITIVE",
                                "component" : {
                                 "uuid" : "94f87321-a5d1-4c2f-b2fe-95165debebc6",
                                 "name" : "componentName",
                                 "version" : "componentVersion"
                                },
                                "vulnerability" : {
                                  "uuid" : "bccec5d5-ec21-4958-b3e8-22a7a866a05a",
                                  "vulnId" : "INT-001",
                                  "source" : "INTERNAL",
                                  "aliases" : [ {
                                    "vulnId" : "OSV-001",
                                    "source" : "OSV"
                                  } ],
                                  "title" : "vulnerabilityTitle",
                                  "subtitle" : "vulnerabilitySubTitle",
                                  "description" : "vulnerabilityDescription",
                                  "recommendation" : "vulnerabilityRecommendation",
                                  "cvssv2" : 5.5,
                                  "cvssv3" : 6.6,
                                  "owaspRRLikelihood" : 1.1,
                                  "owaspRRTechnicalImpact" : 2.2,
                                  "owaspRRBusinessImpact" : 3.3,
                                  "severity" : "MEDIUM",
                                  "cwes" : [ {
                                    "cweId" : 666,
                                    "name" : "Operation on Resource in Wrong Phase of Lifetime"
                                  }, {
                                    "cweId" : 777,
                                    "name" : "Regular Expression without Anchors"
                                  } ]
                                }
                              }
                            }
                          }
                        }
                        """)));
    }

}
