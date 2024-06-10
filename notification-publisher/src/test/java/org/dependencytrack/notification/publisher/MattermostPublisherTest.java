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
@TestProfile(MattermostPublisherTest.TestProfile.class)
public class MattermostPublisherTest extends AbstractWebhookPublisherTest<MattermostPublisher> {

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
                          "username": "Dependency Track",
                          "icon_url": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                          "text": "#### Bill of Materials Consumed\\nA CycloneDX BOM was consumed and will be processed\\n**Project**: pkg:maven/org.acme/projectName@projectVersion\\n[View Project](https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95)"
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
                          "username": "Dependency Track",
                          "icon_url": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                          "text": "#### Bill of Materials Processing Failed\\nAn error occurred while processing a BOM\\n"
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
                          "username": "Dependency Track",
                          "icon_url": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                          "text": "#### Bill of Materials Processing Failed\\nAn error occurred while processing a BOM\\n"
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
                          "username" : "Dependency Track",
                          "icon_url" : "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                          "text" : "#### GitHub Advisory Mirroring\\nAn error occurred mirroring the contents of GitHub Advisories. Check log for details.\\n"
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
                          "username": "Dependency Track",
                          "icon_url": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                          "text": "#### New Vulnerability Identified\\n\\n**Component**: componentName : componentVersion\\n**Vulnerability**: INT-001, MEDIUM\\n[View Component](https://example.com/components/94f87321-a5d1-4c2f-b2fe-95165debebc6) - [View Vulnerability](https://example.com/vulnerabilities/INTERNAL/INT-001)"
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
                          "username": "Dependency Track",
                          "icon_url": "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                          "text": "#### Analysis Decision: Finding Suppressed\\n\\n**Project**: pkg:maven/org.acme/projectName@projectVersion\\n**Component**: componentName : componentVersion\\n**Vulnerability**: INT-001, MEDIUM\\n**Analysis**: FALSE_POSITIVE, suppressed: true\\n[View Project](https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95) - [View Component](https://example.com/components/94f87321-a5d1-4c2f-b2fe-95165debebc6) - [View Vulnerability](https://example.com/vulnerabilities/INTERNAL/INT-001)"
                        }
                        """)));
    }

    @Test
    @Override
    @TestTransaction
    public void testInformWithNewVulnerableDependencyNotification() {
        super.testInformWithNewVulnerableDependencyNotification();

        wireMock.verifyThat(postRequestedFor(anyUrl())
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "username" : "Dependency Track",
                          "icon_url" : "https://raw.githubusercontent.com/DependencyTrack/branding/master/dt-logo-symbol-blue-background.png",
                          "text" : "#### Vulnerable Dependency Introduced\\n\\n**Project**: pkg:maven/org.acme/projectName@projectVersion\\n**Component**: componentName : componentVersion\\n[View Project](https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95) - [View Component](https://example.com/components/94f87321-a5d1-4c2f-b2fe-95165debebc6)"
                        }
                        """)));
    }
}
