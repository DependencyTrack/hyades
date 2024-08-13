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

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;

@QuarkusTest
@TestProfile(SlackPublisherTest.TestProfile.class)
public class SlackPublisherTest extends AbstractWebhookPublisherTest<SlackPublisher> {

    public static class TestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.ofEntries(
                    Map.entry("dtrack.general.base.url", "https://example.com")
            );
        }
    }

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
                          "blocks": [
                            {
                              "type": "header",
                              "text": {
                                "type": "plain_text",
                                "text": "GROUP_BOM_CONSUMED"
                              }
                            },
                            {
                              "type": "context",
                              "elements": [
                                {
                                  "text": "*LEVEL_INFORMATIONAL*  |  *SCOPE_PORTFOLIO*",
                                  "type": "mrkdwn"
                                }
                              ]
                            },
                            {
                              "type": "divider"
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "Bill of Materials Consumed",
                                "type": "plain_text"
                              }
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "A CycloneDX BOM was consumed and will be processed",
                                "type": "plain_text"
                              }
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
                          "blocks": [
                            {
                              "type": "header",
                              "text": {
                                "type": "plain_text",
                                "text": "GROUP_BOM_PROCESSING_FAILED"
                              }
                            },
                            {
                              "type": "context",
                              "elements": [
                                {
                                  "text": "*LEVEL_ERROR*  |  *SCOPE_PORTFOLIO*",
                                  "type": "mrkdwn"
                                }
                              ]
                            },
                            {
                              "type": "divider"
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "Bill of Materials Processing Failed",
                                "type": "plain_text"
                              }
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "An error occurred while processing a BOM",
                                "type": "plain_text"
                              }
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
                          "blocks": [
                            {
                              "type": "header",
                              "text": {
                                "type": "plain_text",
                                "text": "GROUP_BOM_PROCESSING_FAILED"
                              }
                            },
                            {
                              "type": "context",
                              "elements": [
                                {
                                  "text": "*LEVEL_ERROR*  |  *SCOPE_PORTFOLIO*",
                                  "type": "mrkdwn"
                                }
                              ]
                            },
                            {
                              "type": "divider"
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "Bill of Materials Processing Failed",
                                "type": "plain_text"
                              }
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "An error occurred while processing a BOM",
                                "type": "plain_text"
                              }
                            }
                          ]
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomValidationFailedNotificationSubject() throws Exception {
        super.testInformWithBomValidationFailedNotificationSubject();

        wireMock.verifyThat(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "blocks": [
                            {
                              "type": "header",
                              "text": {
                                "type": "plain_text",
                                "text": "GROUP_BOM_VALIDATION_FAILED"
                              }
                            },
                            {
                              "type": "context",
                              "elements": [
                                {
                                  "text": "*LEVEL_ERROR*  |  *SCOPE_PORTFOLIO*",
                                  "type": "mrkdwn"
                                }
                              ]
                            },
                            {
                              "type": "divider"
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "Bill of Materials Validation Failed",
                                "type": "plain_text"
                              }
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "An error occurred while validating a BOM",
                                "type": "plain_text"
                              }
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
                          "blocks": [
                            {
                              "type": "header",
                              "text": {
                                "type": "plain_text",
                                "text": "GROUP_DATASOURCE_MIRRORING"
                              }
                            },
                            {
                              "type": "context",
                              "elements": [
                                {
                                  "text": "*LEVEL_ERROR*  |  *SCOPE_SYSTEM*",
                                  "type": "mrkdwn"
                                }
                              ]
                            },
                            {
                              "type": "divider"
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "GitHub Advisory Mirroring",
                                "type": "plain_text"
                              }
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "An error occurred mirroring the contents of GitHub Advisories. Check log for details.",
                                "type": "plain_text"
                              }
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
                          "blocks": [
                            {
                              "type": "header",
                              "text": {
                                "type": "plain_text",
                                "text": "New Vulnerability"
                              }
                            },
                            {
                              "type": "context",
                              "elements": [
                                {
                                  "text": "*LEVEL_INFORMATIONAL*  |  *SCOPE_PORTFOLIO*",
                                  "type": "mrkdwn"
                                }
                              ]
                            },
                            {
                              "type": "divider"
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "New Vulnerability Identified",
                                "type": "mrkdwn"
                              },
                              "fields": [
                                {
                                  "type": "mrkdwn",
                                  "text": "*VulnID*"
                                },
                                {
                                  "type": "plain_text",
                                  "text": "INT-001"
                                },
                                {
                                  "type": "mrkdwn",
                                  "text": "*Severity*"
                                },
                                {
                                  "type": "plain_text",
                                  "text": "MEDIUM"
                                },
                                {
                                  "type": "mrkdwn",
                                  "text": "*Source*"
                                },
                                {
                                  "type": "plain_text",
                                  "text": "INTERNAL"
                                },
                                {
                                  "type": "mrkdwn",
                                  "text": "*Component*"
                                },
                                {
                                  "type": "plain_text",
                                  "text": "componentName : componentVersion"
                                }
                              ]
                            },
                            {
                              "type": "actions",
                              "elements": [
                                {
                                  "type": "button",
                                  "text": {
                                    "type": "plain_text",
                                    "text": "View Vulnerability"
                                  },
                                  "action_id": "actionId-1",
                                  "url": "https://example.com/vulnerabilities/INTERNAL/INT-001"
                                },
                                {
                                  "type": "button",
                                  "text": {
                                    "type": "plain_text",
                                    "text": "View Component"
                                  },
                                  "action_id": "actionId-2",
                                  "url": "https://example.com/components/94f87321-a5d1-4c2f-b2fe-95165debebc6"
                                }
                              ]
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
                          "blocks": [
                            {
                              "type": "header",
                              "text": {
                                "type": "plain_text",
                                "text": "New Vulnerable Dependency"
                              }
                            },
                            {
                              "type": "context",
                              "elements": [
                                {
                                  "text": "*LEVEL_INFORMATIONAL*  |  *SCOPE_PORTFOLIO*",
                                  "type": "mrkdwn"
                                }
                              ]
                            },
                            {
                              "type": "divider"
                            },
                            {
                              "type": "section",
                              "text": {
                                "text": "Vulnerable Dependency Introduced",
                                "type": "mrkdwn"
                              },
                              "fields": [
                                {
                                  "type": "mrkdwn",
                                  "text": "*Component*"
                                },
                                {
                                  "type": "plain_text",
                                  "text": "componentName : componentVersion"
                                },
                                {
                                  "type": "mrkdwn",
                                  "text": "*Project*"
                                },
                                {
                                  "type": "plain_text",
                                  "text": "pkg:maven/org.acme/projectName@projectVersion"
                                }
                              ]
                            },
                            {
                              "type": "actions",
                              "elements": [
                                {
                                  "type": "button",
                                  "text": {
                                    "type": "plain_text",
                                    "text": "View Project"
                                  },
                                  "action_id": "actionId-1",
                                  "url": "https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95"
                                },
                                {
                                  "type": "button",
                                  "text": {
                                    "type": "plain_text",
                                    "text": "View Component"
                                  },
                                  "action_id": "actionId-2",
                                  "url": "https://example.com/components/94f87321-a5d1-4c2f-b2fe-95165debebc6"
                                }
                              ]
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
                          "blocks": [
                            {
                        	  "type": "header",
                        	  "text": {
                        	    "type": "plain_text",
                        		"text": "Project Audit Change"
                        	  }
                        	},
                        	{
                        	  "type": "context",
                        	  "elements": [
                        	    {
                        		  "text": "*LEVEL_INFORMATIONAL*  |  *SCOPE_PORTFOLIO*",
                        		  "type": "mrkdwn"
                        		}
                        	  ]
                        	},
                        	{
                        	  "type": "divider"
                        	},
                        	{
                        	  "type": "section",
                        	  "text": {
                        	    "text": "Analysis Decision: Finding Suppressed",
                        		"type": "plain_text"
                        	  },
                        	  "fields": [
                        	    {
                        		  "type": "mrkdwn",
                        		  "text": "*Analysis State*"
                        		},
                        		{
                        		  "type": "plain_text",
                        		  "emoji": true,
                        		  "text": "FALSE_POSITIVE"
                        		},
                        		{
                        		  "type": "mrkdwn",
                        		  "text": "*Suppressed*"
                        		},
                        		{
                        		  "type": "plain_text",
                        		  "text": "true"
                        		},
                        		{
                        		  "type": "mrkdwn",
                        		  "text": "*VulnID*"
                        		},
                        		{
                        		  "type": "plain_text",
                        		  "text": "INT-001"
                        		},
                        		{
                        		  "type": "mrkdwn",
                        		  "text": "*Severity*"
                        		},
                        		{
                        		  "type": "plain_text",
                        		  "text": "MEDIUM"
                        		},
                        		{
                        		  "type": "mrkdwn",
                        		  "text": "*Source*"
                        		},
                        		{
                        		  "type": "plain_text",
                        		  "text": "INTERNAL"
                        		}
                        	  ]
                        	},
                            {
                        	  "type": "section",
                        	  "fields": [
                        		{
                        		  "type": "mrkdwn",
                        		  "text": "*Component*"
                        		},
                        		{
                        		  "type": "plain_text",
                        		  "text": "componentName : componentVersion"
                        		},
                        		{
                        		  "type": "mrkdwn",
                        		  "text": "*Project*"
                        		},
                        		{
                        		  "type": "plain_text",
                        		  "text": "pkg:maven/org.acme/projectName@projectVersion"
                        		}
                        	  ]
                        	},
                        	{
                        	  "type": "actions",
                        	  "elements": [
                        	    {
                        		  "type": "button",
                        		  "text": {
                        		    "type": "plain_text",
                        			"text": "View Project"
                        		  },
                        		  "action_id": "actionId-1",
                        		  "url": "https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95"
                        		},
                        		{
                        		  "type": "button",
                        		  "text": {
                        		    "type": "plain_text",
                        			"text": "View Component"
                        		  },
                        		  "action_id": "actionId-2",
                        		  "url": "https://example.com/components/94f87321-a5d1-4c2f-b2fe-95165debebc6"
                        		},
                        	    {
                        		  "type": "button",
                        		  "text": {
                        		    "type": "plain_text",
                        			"text": "View Vulnerability"
                        		  },
                        		  "action_id": "actionId-3",
                        		  "url": "https://example.com/vulnerabilities/INTERNAL/INT-001"
                        		}
                        	  ]
                        	}
                          ]
                        }
                        """)));
    }

}
