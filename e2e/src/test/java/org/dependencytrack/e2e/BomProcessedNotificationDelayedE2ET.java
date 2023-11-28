package org.dependencytrack.e2e;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.dependencytrack.apiserver.model.BomUploadRequest;
import org.dependencytrack.apiserver.model.BomUploadResponse;
import org.dependencytrack.apiserver.model.CreateNotificationRuleRequest;
import org.dependencytrack.apiserver.model.CreateVulnerabilityRequest;
import org.dependencytrack.apiserver.model.NotificationPublisher;
import org.dependencytrack.apiserver.model.NotificationRule;
import org.dependencytrack.apiserver.model.UpdateNotificationRuleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class BomProcessedNotificationDelayedE2ET extends AbstractE2ET {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Override
    @BeforeEach
    void beforeEach() throws Exception {
        // host.docker.internal may not always be available, so use testcontainer's
        // solution for host port exposure instead: https://www.testcontainers.org/features/networking/#exposing-host-ports-to-the-container
        Testcontainers.exposeHostPorts(wireMock.getRuntimeInfo().getHttpPort());

        super.beforeEach();
    }

    @Override
    protected void customizeApiServerContainer(final GenericContainer<?> container) {
        container.withEnv("TMP_DELAY_BOM_PROCESSED_NOTIFICATION", "true");
    }

    @Override
    protected void customizeVulnAnalyzerContainer(final GenericContainer<?> container) {
        // Disable all scanners except the internal one.
        container
                .withEnv("SCANNER_INTERNAL_ENABLED", "true")
                .withEnv("SCANNER_OSSINDEX_ENABLED", "false")
                .withEnv("SCANNER_SNYK_ENABLED", "false");
    }

    @Test
    void test() throws Exception {
        final List<NotificationPublisher> publishers = apiServerClient.getAllNotificationPublishers();

        // Find the webhook notification publisher.
        final NotificationPublisher webhookPublisher = publishers.stream()
                .filter(publisher -> publisher.name().equals("Outbound Webhook"))
                .findAny()
                .orElseThrow(() -> new AssertionError("Unable to find webhook notification publisher"));

        // Create a webhook alert for NEW_VULNERABILITY notifications and point it to WireMock.
        final NotificationRule webhookRule = apiServerClient.createNotificationRule(new CreateNotificationRuleRequest(
                "foo", "PORTFOLIO", "INFORMATIONAL", new CreateNotificationRuleRequest.Publisher(webhookPublisher.uuid())));
        apiServerClient.updateNotificationRule(new UpdateNotificationRuleRequest(webhookRule.uuid(), webhookRule.name(), true,
                "INFORMATIONAL", Set.of("BOM_PROCESSED", "PROJECT_VULN_ANALYSIS_COMPLETE"), """
                {
                  "destination": "http://host.testcontainers.internal:%d/notification"
                }
                """.formatted(wireMock.getPort())));

        wireMock.stubFor(post(urlPathEqualTo("/notification"))
                .willReturn(aResponse()
                        .withStatus(201)));

        // Create a new internal vulnerability for jackson-databind.
        apiServerClient.createVulnerability(new CreateVulnerabilityRequest("INT-123", "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H", List.of(917, 502), List.of(
                new CreateVulnerabilityRequest.AffectedComponent("PURL", "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2.2", "EXACT")
        )));

        // Parse and base64 encode a BOM.
        final byte[] bomBytes = IOUtils.resourceToByteArray("/dtrack-apiserver-4.5.0.bom.json");
        final String bomBase64 = Base64.getEncoder().encodeToString(bomBytes);

        // Upload the BOM
        final BomUploadResponse response = apiServerClient.uploadBom(new BomUploadRequest("foo", "bar", true, bomBase64));
        assertThat(response.token()).isNotEmpty();

        // Wait up to 15sec for the BOM processing to complete.
        await("BOM_PROCESSED webhook notification")
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(this::verifyBomProcessedWebhookNotification);

        await("PROJECT_VULN_ANALYSIS_COMPLETE webhook notification")
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(this::verifyProjectVulnAnalysisCompleteNotification);
    }

    private void verifyBomProcessedWebhookNotification() {
        wireMock.verify(1, postRequestedFor(urlPathEqualTo("/notification"))
                .withRequestBody(equalToJson("""
                        {
                          "notification" : {
                            "level" : "LEVEL_INFORMATIONAL",
                            "scope" : "SCOPE_PORTFOLIO",
                            "group" : "GROUP_BOM_PROCESSED",
                            "timestamp" : "${json-unit.regex}(^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\\\.[0-9]{3}Z$)",
                            "title" : "Bill of Materials Processed",
                            "content" : "A CycloneDX BOM was processed",
                            "subject" : {
                              "token": "${json-unit.any-string}",
                              "project" : {
                                "uuid" : "${json-unit.any-string}",
                                "name" : "foo",
                                "version" : "bar",
                                "purl" : "pkg:maven/org.dependencytrack/dependency-track@4.5.0?type=war"
                              },
                              "bom" : {
                                "content" : "(Omitted)",
                                "format" : "CycloneDX",
                                "specVersion" : "Unknown"
                              }
                            }
                          }
                        }
                        """)));
    }

    private void verifyProjectVulnAnalysisCompleteNotification() {
        wireMock.verify(1, postRequestedFor(urlPathEqualTo("/notification"))
                .withRequestBody(equalToJson("""
                        {
                           "notification" : {
                             "level" : "LEVEL_INFORMATIONAL",
                             "scope" : "SCOPE_PORTFOLIO",
                             "group" : "GROUP_PROJECT_VULN_ANALYSIS_COMPLETE",
                             "timestamp" : "${json-unit.regex}(^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\\\.[0-9]{3}Z$)",
                             "title" : "Project vulnerability analysis complete",
                             "content" : "${json-unit.any-string}",
                             "subject" : {
                               "token": "${json-unit.any-string}",
                               "project" : {
                                 "uuid": "${json-unit.any-string}",
                                 "name" : "foo",
                                 "version" : "bar",
                                 "purl": "pkg:maven/org.dependencytrack/dependency-track@4.5.0?type=war"
                               },
                               "findings" : [ {
                                 "component" : {
                                   "uuid": "${json-unit.any-string}",
                                   "group" : "com.fasterxml.jackson.core",
                                   "name" : "jackson-databind",
                                   "version" : "2.13.2.2",
                                   "purl" : "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2.2?type=jar",
                                   "md5" : "055c97cb488b0956801e13abcc2a0cfe",
                                   "sha1" : "ffeb635597d093509f33e1e94274d14be610f933",
                                   "sha256" : "efb86b148712a838b94b3cfc95769785a116b3461f709b4cc510055a58b804b2",
                                   "sha512" : "0e9398591d86f80f16fc2d6ff0dda3e7821033e2c59472981eaab61443be3d77198655682905b85260fb2186a2cf0f33988aff689a49bb54e56c07e02f607e8a"
                                 },
                                 "vulnerabilities" : [ {
                                   "uuid": "${json-unit.any-string}",
                                   "vulnId" : "INT-123",
                                   "source" : "INTERNAL",
                                   "severity" : "CRITICAL",
                                   "cvssv3": 10.0
                                 } ]
                               } ],
                               "status" : "PROJECT_VULN_ANALYSIS_STATUS_COMPLETED"
                             }
                           }
                         }
                        """)));
    }

}
