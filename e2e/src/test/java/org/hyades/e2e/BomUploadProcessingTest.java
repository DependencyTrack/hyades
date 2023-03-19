package org.hyades.e2e;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.hyades.apiserver.model.BomProcessingResponse;
import org.hyades.apiserver.model.BomUploadRequest;
import org.hyades.apiserver.model.BomUploadResponse;
import org.hyades.apiserver.model.CreateNotificationRuleRequest;
import org.hyades.apiserver.model.CreateNotificationRuleRequest.Publisher;
import org.hyades.apiserver.model.CreateVulnerabilityRequest;
import org.hyades.apiserver.model.CreateVulnerabilityRequest.AffectedComponent;
import org.hyades.apiserver.model.Finding;
import org.hyades.apiserver.model.NotificationPublisher;
import org.hyades.apiserver.model.NotificationRule;
import org.hyades.apiserver.model.Project;
import org.hyades.apiserver.model.UpdateNotificationRuleRequest;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@WireMockTest
public class BomUploadProcessingTest extends AbstractE2eTest {

    @Override
    protected void customizeVulnAnalyzerContainer(final GenericContainer<?> container) {
        // Disable all scanners except the internal one.
        container
                .withEnv("SCANNER_INTERNAL_ENABLED", "true")
                .withEnv("SCANNER_OSSINDEX_ENABLED", "false")
                .withEnv("SCANNER_SNYK_ENABLED", "false");
    }

    @Test
    void test(final WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
        // Find the webhook notification publisher.
        final NotificationPublisher webhookPublisher = apiServerClient.getAllNotificationPublishers().stream()
                .filter(publisher -> publisher.name().equals("Outbound Webhook"))
                .findAny()
                .orElseThrow(() -> new AssertionError("Unable to find webhook notification publisher"));

        // Create a webhook alert for NEW_VULNERABILITY notifications and point it to WireMock.
        final NotificationRule rule = apiServerClient.createNotificationRule(new CreateNotificationRuleRequest(
                "foo", "PORTFOLIO", "INFORMATIONAL", new Publisher(webhookPublisher.uuid())));
        apiServerClient.updateNotificationRule(new UpdateNotificationRuleRequest(rule.uuid(), rule.name(), true,
                "INFORMATIONAL", Set.of("NEW_VULNERABILITY"), """
                {
                  "destination": "http://host.docker.internal:%d/notification"
                }
                """.formatted(wireMockRuntimeInfo.getHttpPort())));

        // Create a new internal vulnerability for jackson-databind.
        apiServerClient.createVulnerability(new CreateVulnerabilityRequest("INT-123", List.of(
                new AffectedComponent("PURL", "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2.2", "EXACT")
        )));

        // Parse and base64 encode a BOM.
        final byte[] bomBytes = IOUtils.resourceToByteArray("/dtrack-apiserver-4.5.0.bom.json");
        final String bomBase64 = Base64.getEncoder().encodeToString(bomBytes);

        // Upload the BOM
        final BomUploadResponse response = apiServerClient.uploadBom(new BomUploadRequest("foo", "bar", true, bomBase64));
        assertThat(response.token()).isNotEmpty();

        // Wait up to 15sec for the BOM processing to complete.
        await("BOM processing")
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    final BomProcessingResponse processingResponse = apiServerClient.isBomBeingProcessed(response.token());
                    assertThat(processingResponse.processing()).isFalse();
                });

        // Lookup the project we just created.
        final Project project = apiServerClient.lookupProject("foo", "bar");

        // Ensure the internal vulnerability has been flagged.
        final List<Finding> findings = apiServerClient.getFindings(project.uuid());
        assertThat(findings).satisfiesExactly(
                finding -> {
                    assertThat(finding.component().name()).isEqualTo("jackson-databind");
                    assertThat(finding.project().name()).isEqualTo("foo");
                    assertThat(finding.vulnerability().vulnId()).isEqualTo("INT-123");
                }
        );

        // Verify that we received the alert about jackson-databind being vulnerable.
        await("NEW_VULNERABILITY notification")
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(this::verifyNotification);
    }

    private void verifyNotification() {
        verify(postRequestedFor(urlPathEqualTo("/notification"))
                .withRequestBody(equalToJson("""
                        {
                          "notification": {
                            "level": "INFORMATIONAL",
                            "scope": "PORTFOLIO",
                            "group": "NEW_VULNERABILITY",
                            "timestamp": "${json-unit.any-string}",
                            "title": "${json-unit.any-string}",
                            "content": "${json-unit.any-string}",
                            "subject": {
                              "component": {
                                "uuid": "${json-unit.any-string}",
                                "group": "com.fasterxml.jackson.core",
                                "name": "jackson-databind",
                                "version": "2.13.2.2",
                                "purl": "${json-unit.any-string}",
                                "md5": "${json-unit.any-string}",
                                "sha1": "${json-unit.any-string}",
                                "sha256": "${json-unit.any-string}",
                                "sha512": "${json-unit.any-string}"
                              },
                              "project": {
                                "uuid": "${json-unit.any-string}",
                                "name": "foo",
                                "version": "bar"
                              },
                              "vulnerability": {
                                "uuid": "${json-unit.any-string}",
                                "id": "INT-123",
                                "source": "INTERNAL"
                              },
                              "affectedProjects": [
                                {
                                  "uuid": "${json-unit.any-string}",
                                  "name": "foo",
                                  "version": "bar"
                                }
                              ]
                            }
                          }
                        }
                        """)
                )
        );
    }

}