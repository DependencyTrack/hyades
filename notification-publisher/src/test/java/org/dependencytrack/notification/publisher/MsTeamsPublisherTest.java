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
package org.dependencytrack.notification.publisher;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.json.JsonObjectBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;

@QuarkusTest
class MsTeamsPublisherTest extends AbstractWebhookPublisherTest<MsTeamsPublisher> {

    @Override
    JsonObjectBuilder extraConfig() {
        return super.extraConfig()
                .add(Publisher.CONFIG_DESTINATION, "http://localhost:" + wireMockPort);
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomConsumedNotification() throws Exception {
        super.testInformWithBomConsumedNotification();

        wireMock.verifyThat(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "@type": "MessageCard",
                          "@context": "http://schema.org/extensions",
                          "summary": "Bill of Materials Consumed",
                          "title": "Bill of Materials Consumed",
                          "sections": [
                            {
                              "activityTitle": "Dependency-Track",
                              "activitySubtitle": "1970-01-01T18:31:06.000Z",
                              "activityImage": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                              "facts": [
                                {
                                  "name": "Level",
                                  "value": "LEVEL_INFORMATIONAL"
                                },
                                {
                                  "name": "Scope",
                                  "value": "SCOPE_PORTFOLIO"
                                },
                                {
                                  "name": "Group",
                                  "value": "GROUP_BOM_CONSUMED"
                                }
                              ],
                              "text": "A CycloneDX BOM was consumed and will be processed"
                            }
                          ]
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotification() throws Exception {
        super.testInformWithBomProcessingFailedNotification();

        wireMock.verifyThat(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "@type": "MessageCard",
                          "@context": "http://schema.org/extensions",
                          "summary": "Bill of Materials Processing Failed",
                          "title": "Bill of Materials Processing Failed",
                          "sections": [
                            {
                              "activityTitle": "Dependency-Track",
                              "activitySubtitle": "1970-01-01T18:31:06.000Z",
                              "activityImage": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                              "facts": [
                                {
                                  "name": "Level",
                                  "value": "LEVEL_ERROR"
                                },
                                {
                                  "name": "Scope",
                                  "value": "SCOPE_PORTFOLIO"
                                },
                                {
                                  "name": "Group",
                                  "value": "GROUP_BOM_PROCESSING_FAILED"
                                }
                              ],
                              "text": "An error occurred while processing a BOM"
                            }
                          ]
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject() throws Exception {
        super.testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject();

        wireMock.verifyThat(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "@type": "MessageCard",
                          "@context": "http://schema.org/extensions",
                          "summary": "Bill of Materials Processing Failed",
                          "title": "Bill of Materials Processing Failed",
                          "sections": [
                            {
                              "activityTitle": "Dependency-Track",
                              "activitySubtitle": "1970-01-01T18:31:06.000Z",
                              "activityImage": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                              "facts": [
                                {
                                  "name": "Level",
                                  "value": "LEVEL_ERROR"
                                },
                                {
                                  "name": "Scope",
                                  "value": "SCOPE_PORTFOLIO"
                                },
                                {
                                  "name": "Group",
                                  "value": "GROUP_BOM_PROCESSING_FAILED"
                                }
                              ],
                              "text": "An error occurred while processing a BOM"
                            }
                          ]
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithDataSourceMirroringNotification() throws Exception {
        super.testInformWithDataSourceMirroringNotification();

        wireMock.verifyThat(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "@type": "MessageCard",
                          "@context": "http://schema.org/extensions",
                          "summary": "GitHub Advisory Mirroring",
                          "title": "GitHub Advisory Mirroring",
                          "sections": [
                            {
                              "activityTitle": "Dependency-Track",
                              "activitySubtitle": "1970-01-01T18:31:06.000Z",
                              "activityImage": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                              "facts": [
                                {
                                  "name": "Level",
                                  "value": "LEVEL_ERROR"
                                },
                                {
                                  "name": "Scope",
                                  "value": "SCOPE_SYSTEM"
                                },
                                {
                                  "name": "Group",
                                  "value": "GROUP_DATASOURCE_MIRRORING"
                                }
                              ],
                              "text": "An error occurred mirroring the contents of GitHub Advisories. Check log for details."
                            }
                          ]
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithNewVulnerabilityNotification() throws Exception {
        super.testInformWithNewVulnerabilityNotification();

        wireMock.verifyThat(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "@type": "MessageCard",
                          "@context": "http://schema.org/extensions",
                          "summary": "New Vulnerability Identified",
                          "title": "New Vulnerability Identified",
                          "sections": [
                            {
                              "activityTitle": "Dependency-Track",
                              "activitySubtitle": "1970-01-01T18:31:06.000Z",
                              "activityImage": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                              "facts": [
                                {
                                  "name": "VulnID",
                                  "value": "INT-001"
                                },
                                {
                                  "name": "Severity",
                                  "value": "MEDIUM"
                                },
                                {
                                  "name": "Source",
                                  "value": "INTERNAL"
                                },
                                {
                                  "name": "Component",
                                  "value": "componentName : componentVersion"
                                }
                              ],
                              "text": ""
                            }
                          ]
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithProjectAuditChangeNotification() throws Exception {
        super.testInformWithProjectAuditChangeNotification();

        wireMock.verifyThat(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "@type": "MessageCard",
                          "@context": "http://schema.org/extensions",
                          "summary": "Analysis Decision: Finding Suppressed",
                          "title": "Analysis Decision: Finding Suppressed",
                          "sections": [
                            {
                              "activityTitle": "Dependency-Track",
                              "activitySubtitle": "1970-01-01T18:31:06.000Z",
                              "activityImage": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                              "facts": [
                                {
                                  "name": "Analysis Type",
                                  "value": "Project Analysis"
                                },
                                {
                                  "name": "Analysis State",
                                  "value": "FALSE_POSITIVE"
                                },
                                {
                                  "name": "Suppressed",
                                  "value": "true"
                                },
                                {
                                  "name": "VulnID",
                                  "value": "INT-001"
                                },
                                {
                                  "name": "Severity",
                                  "value": "MEDIUM"
                                },
                                {
                                  "name": "Source",
                                  "value": "INTERNAL"
                                },
                                {
                                  "name": "Component",
                                  "value": "componentName : componentVersion"
                                },
                                {
                                  "name": "Project",
                                  "value": "pkg:maven/org.acme/projectName@projectVersion"
                                }
                              ],
                              "text": ""
                            }
                          ]
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    public void testInformWithNewVulnerableDependencyNotification() throws Exception {
        super.testInformWithNewVulnerableDependencyNotification();

        wireMock.verifyThat(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "@type": "MessageCard",
                          "@context": "http://schema.org/extensions",
                          "summary": "Vulnerable Dependency Introduced",
                          "title": "Vulnerable Dependency Introduced",
                          "sections": [
                            {
                              "activityTitle": "Dependency-Track",
                              "activitySubtitle": "1970-01-01T18:31:06.000Z",
                              "activityImage": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                              "facts": [
                                {
                                  "name": "Project",
                                  "value": "pkg:maven/org.acme/projectName@projectVersion"
                                },
                                {
                                  "name": "Component",
                                  "value": "componentName : componentVersion"
                                }
                              ],
                              "text": ""
                            }
                          ]
                        }
                        """)));
    }
}
