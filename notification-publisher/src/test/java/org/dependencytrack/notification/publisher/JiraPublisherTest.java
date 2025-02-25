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
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.json.JsonObjectBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@QuarkusTest
@TestProfile(JiraPublisherTest.TestProfile.class)
public class JiraPublisherTest extends AbstractWebhookPublisherTest<JiraPublisher> {

    public static class TestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {

            return Map.ofEntries(
                    Map.entry("dtrack.general.base.url", "https://example.com"),
                    Map.entry("dtrack.integrations.jira.username", "jiraUser"),
                    Map.entry("dtrack.integrations.jira.url", "http://localhost:${quarkus.wiremock.devservices.port}"),
                    Map.entry("dtrack.integrations.jira.password", "7h5IR+TUX22lXLHCv8wJqxKud8NdPrujF4Lnbx+GHgI=")
            );
        }
    }

    @Override
    JsonObjectBuilder extraConfig() {
        return super.extraConfig()
                .add(Publisher.CONFIG_DESTINATION, "PROJECT")
                .add("jiraTicketType", "Task");
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomConsumedNotification() throws Exception {
        super.testInformWithBomConsumedNotification();

        wireMock.verifyThat(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_BOM_CONSUMED] Bill of Materials Consumed",
                            "description" : "A CycloneDX BOM was consumed and will be processed\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_INFORMATIONAL\\n\\n"
                          }
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotification() throws Exception {
        super.testInformWithBomProcessingFailedNotification();

        wireMock.verifyThat(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_BOM_PROCESSING_FAILED] Bill of Materials Processing Failed",
                            "description" : "An error occurred while processing a BOM\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_ERROR\\n\\n"
                          }
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject() throws Exception {
        super.testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject();

        wireMock.verifyThat(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_BOM_PROCESSING_FAILED] Bill of Materials Processing Failed",
                            "description" : "An error occurred while processing a BOM\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_ERROR\\n\\n"
                          }
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomValidationFailedNotificationSubject() throws Exception {
        super.testInformWithBomValidationFailedNotificationSubject();

        wireMock.verifyThat(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_BOM_VALIDATION_FAILED] Bill of Materials Validation Failed",
                            "description" : "An error occurred while validating a BOM\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_ERROR\\n\\n"
                          }
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithDataSourceMirroringNotification() throws Exception {
        super.testInformWithDataSourceMirroringNotification();

        wireMock.verifyThat(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_DATASOURCE_MIRRORING] GitHub Advisory Mirroring",
                            "description" : "An error occurred mirroring the contents of GitHub Advisories. Check log for details.\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_ERROR\\n\\n"
                          }
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithNewVulnerabilityNotification() throws Exception {
        super.testInformWithNewVulnerabilityNotification();

        wireMock.verifyThat(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_NEW_VULNERABILITY] [MEDIUM] New medium vulnerability identified: INT-001",
                            "description" : "A new vulnerability has been identified on your project(s).\\n\\\\\\\\\\n\\\\\\\\\\n*Vulnerability description*\\n{code:none|bgColor=white|borderStyle=none}vulnerabilityDescription{code}\\n\\n*VulnID*\\nINT-001\\n\\n*Severity*\\nMedium\\n\\n*Component*\\n[componentName : componentVersion|https://example.com/components/94f87321-a5d1-4c2f-b2fe-95165debebc6]\\n\\n*Affected project(s)*\\n- [projectName (projectVersion)|https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95]\\n"
                          }
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithProjectAuditChangeNotification() throws Exception {
        super.testInformWithProjectAuditChangeNotification();

        wireMock.verifyThat(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_PROJECT_AUDIT_CHANGE] Analysis Decision: Finding Suppressed",
                            "description" : "\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_INFORMATIONAL\\n\\n"
                          }
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    public void testInformWithNewVulnerableDependencyNotification() throws Exception {
        super.testInformWithNewVulnerableDependencyNotification();

        wireMock.verifyThat(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields": {
                            "project": {
                              "key": "PROJECT"
                            },
                            "issuetype": {
                              "name": "Task"
                            },
                            "summary": "[Dependency-Track] [GROUP_NEW_VULNERABLE_DEPENDENCY] Vulnerable dependency introduced on project projectName",
                            "description": "A component which contains one or more vulnerabilities has been added to your project.\\n\\\\\\\\\\n\\\\\\\\\\n*Project*\\n[pkg:maven/org.acme/projectName@projectVersion|https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95]\\n\\n*Component*\\n[componentName : componentVersion|https://example.com/components/94f87321-a5d1-4c2f-b2fe-95165debebc6]\\n\\n*Vulnerabilities*\\n- INT-001 (Medium)\\n"
                          }
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithTemplateInclude() throws Exception {
        super.testInformWithTemplateInclude();
    }

}
