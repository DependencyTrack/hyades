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
@TestProfile(SlackPublisherWithoutBaseUrlTest.TestProfile.class)
public class SlackPublisherWithoutBaseUrlTest extends AbstractWebhookPublisherTest<SlackPublisher> {

        public static class TestProfile implements QuarkusTestProfile {

            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.ofEntries(
                        Map.entry("dtrack.general.base.url", "")
                );
            }
        }

        @Override
        JsonObjectBuilder extraConfig() {
            return super.extraConfig()
                    .add(Publisher.CONFIG_DESTINATION, "http://localhost:" + wireMockPort);
        }

        @Test
        @TestTransaction
        public void testInformWithNewVulnerabilityNotificationWithoutBaseUrl() throws Exception {
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
                            }
                          ]
                        }
                        """)));
        }
}
