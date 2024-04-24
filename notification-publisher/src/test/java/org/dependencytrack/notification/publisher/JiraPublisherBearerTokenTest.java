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
import org.dependencytrack.notification.util.WireMockTestResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@QuarkusTest
@TestProfile(JiraPublisherBearerTokenTest.TestProfile.class)
public class JiraPublisherBearerTokenTest extends AbstractWebhookPublisherTest<JiraPublisher> {

    public static class TestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.ofEntries(
                    Map.entry("dtrack.general.base.url", "https://example.com"),
                    Map.entry("dtrack.integrations.jira.password", "Pk7KKYvayX0yN9ZeIJFXs6jDBxDKszT1Gw9bTKxxZTc=")
            );
        }

        @Override
        public List<TestResourceEntry> testResources() {
            return List.of(new TestResourceEntry(
                    WireMockTestResource.class,
                    Map.of("serverUrlProperty", "dtrack.integrations.jira.url")
            ));
        }

    }

    @Override
    JsonObjectBuilder extraConfig() {
        return super.extraConfig()
                .add(Publisher.CONFIG_DESTINATION, "PROJECT")
                .add("jiraTicketType", "Task");
    }

    @Test
    @TestTransaction
    void testInformWithBearerToken() throws Exception {
        super.testInformWithBomConsumedNotification();

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Bearer jiraToken"))
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

}
