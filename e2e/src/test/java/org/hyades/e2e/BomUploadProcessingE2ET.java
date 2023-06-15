package org.hyades.e2e;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.hyades.apiserver.model.BomProcessingResponse;
import org.hyades.apiserver.model.BomUploadRequest;
import org.hyades.apiserver.model.BomUploadResponse;
import org.hyades.apiserver.model.ConfigProperty;
import org.hyades.apiserver.model.CreateNotificationRuleRequest;
import org.hyades.apiserver.model.CreateNotificationRuleRequest.Publisher;
import org.hyades.apiserver.model.CreateVulnerabilityRequest;
import org.hyades.apiserver.model.CreateVulnerabilityRequest.AffectedComponent;
import org.hyades.apiserver.model.Finding;
import org.hyades.apiserver.model.NotificationPublisher;
import org.hyades.apiserver.model.NotificationRule;
import org.hyades.apiserver.model.Project;
import org.hyades.apiserver.model.UpdateNotificationRuleRequest;
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

class BomUploadProcessingE2ET extends AbstractE2ET {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetup.SMTP.dynamicPort());

    @Override
    @BeforeEach
    void beforeEach() throws Exception {
        // host.docker.internal may not always be available, so use testcontainer's
        // solution for host port exposure instead: https://www.testcontainers.org/features/networking/#exposing-host-ports-to-the-container
        Testcontainers.exposeHostPorts(greenMail.getSmtp().getPort(), wireMock.getRuntimeInfo().getHttpPort());

        // Users must be created before the notification-publisher container is started.
        greenMail.getUserManager().createUser("from@localhost", "from", "fromPass");
        greenMail.getUserManager().createUser("to@localhost", "to", "toPass");

        super.beforeEach();
    }

    @Override
    protected void customizeNotificationPublisherContainer(final GenericContainer<?> container) {
        container
                .withEnv("QUARKUS_MAILER_FROM", "from@localhost")
                .withEnv("QUARKUS_MAILER_HOST", "host.testcontainers.internal")
                .withEnv("QUARKUS_MAILER_PORT", Integer.toString(greenMail.getSmtp().getPort()))
                .withEnv("QUARKUS_MAILER_USERNAME", "from")
                .withEnv("QUARKUS_MAILER_PASSWORD", "fromPass");
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

        // Find the email notification publisher.
        final NotificationPublisher emailPublisher = publishers.stream()
                .filter(publisher -> publisher.name().equals("Email"))
                .findAny()
                .orElseThrow(() -> new AssertionError("Unable to find email notification publisher"));

        // Find the webhook notification publisher.
        final NotificationPublisher webhookPublisher = publishers.stream()
                .filter(publisher -> publisher.name().equals("Outbound Webhook"))
                .findAny()
                .orElseThrow(() -> new AssertionError("Unable to find webhook notification publisher"));

        // Create an email alert for NEW_VULNERABILITY notifications and point it to GreenMail.
        final NotificationRule emailRule = apiServerClient.createNotificationRule(new CreateNotificationRuleRequest(
                "email", "PORTFOLIO", "INFORMATIONAL", new Publisher(emailPublisher.uuid())));
        apiServerClient.updateNotificationRule(new UpdateNotificationRuleRequest(emailRule.uuid(), emailRule.name(), true,
                "INFORMATIONAL", Set.of("NEW_VULNERABILITY"), """
                {
                  "destination": "to@localhost"
                }
                """));

        // Enable email on the API server side.
        // TODO: We should decide where to put the email configuration,
        // it currently is split between API server and notification publisher.
        apiServerClient.updateConfigProperty(new ConfigProperty("email", "smtp.enabled", "true"));

        // Create a webhook alert for NEW_VULNERABILITY notifications and point it to WireMock.
        final NotificationRule webhookRule = apiServerClient.createNotificationRule(new CreateNotificationRuleRequest(
                "foo", "PORTFOLIO", "INFORMATIONAL", new Publisher(webhookPublisher.uuid())));
        apiServerClient.updateNotificationRule(new UpdateNotificationRuleRequest(webhookRule.uuid(), webhookRule.name(), true,
                "INFORMATIONAL", Set.of("NEW_VULNERABILITY"), """
                {
                  "destination": "http://host.testcontainers.internal:%d/notification"
                }
                """.formatted(wireMock.getPort())));

        //Create notification rule for project vulnerability analysis complete
        // Create a webhook alert for NEW_VULNERABILITY notifications and point it to WireMock.
        final NotificationRule projectVulnAnalysisCompleteWebhookRule = apiServerClient.createNotificationRule(new CreateNotificationRuleRequest(
                "projectVulnAnalysisCompleteWebhookRule", "PORTFOLIO", "INFORMATIONAL", new Publisher(webhookPublisher.uuid())));
        apiServerClient.updateNotificationRule(new UpdateNotificationRuleRequest(projectVulnAnalysisCompleteWebhookRule.uuid(), projectVulnAnalysisCompleteWebhookRule.name(), true,
                "INFORMATIONAL", Set.of("PROJECT_VULN_ANALYSIS_COMPLETE"), """
                {
                  "destination": "http://host.testcontainers.internal:%d/notification"
                }
                """.formatted(wireMock.getPort())));
        wireMock.stubFor(post(urlPathEqualTo("/notification"))
                .willReturn(aResponse()
                        .withStatus(201)));

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
                    assertThat(finding.vulnerability().vulnId()).isEqualTo("INT-123");
                    assertThat(finding.attribution().analyzerIdentity()).isEqualTo("INTERNAL_ANALYZER");
                    assertThat(finding.attribution().attributedOn()).isNotBlank();
                }
        );

        // Verify that we received alerts about jackson-databind being vulnerable
        // via both email and webhook notifications.
        await("NEW_VULNERABILITY webhook notification")
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(this::verifyWebhookNotification);
        await("NEW_VULNERABILITY email notification")
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(this::verifyEmailNotification);

        await(" PROJECT_VULN_ANALYSIS_COMPLETE webhook notification")
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(this::verifyProjectVulnAnalysisCompleteNotification);
    }

    private void verifyEmailNotification() {
        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        final MimeMessage email = greenMail.getReceivedMessages()[0];
        // assertThat(email.getSubject()).isEqualTo("[Dependency-Track] New Vulnerability Identified on Project: [foo : bar]"); // TODO
        // assertThat(email.getContent()).asString().matches(""); // TODO
    }

    private void verifyWebhookNotification() {
        wireMock.verify(postRequestedFor(urlPathEqualTo("/notification"))
                .withRequestBody(equalToJson("""
                        {
                          "notification": {
                            "level": "LEVEL_INFORMATIONAL",
                            "scope": "SCOPE_PORTFOLIO",
                            "group": "GROUP_NEW_VULNERABILITY",
                            "timestamp": "${json-unit.any-string}",
                            "title": "New Vulnerability Identified on Project: [foo : bar]",
                            "content": "INT-123",
                            "subject": {
                              "component": {
                                "uuid": "${json-unit.any-string}",
                                "group": "com.fasterxml.jackson.core",
                                "name": "jackson-databind",
                                "version": "2.13.2.2",
                                "purl": "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2.2?type=jar",
                                "md5": "055c97cb488b0956801e13abcc2a0cfe",
                                "sha1": "ffeb635597d093509f33e1e94274d14be610f933",
                                "sha256": "efb86b148712a838b94b3cfc95769785a116b3461f709b4cc510055a58b804b2",
                                "sha512": "0e9398591d86f80f16fc2d6ff0dda3e7821033e2c59472981eaab61443be3d77198655682905b85260fb2186a2cf0f33988aff689a49bb54e56c07e02f607e8a"
                              },
                              "project": {
                                "uuid": "${json-unit.any-string}",
                                "name": "foo",
                                "version": "bar"
                              },
                              "vulnerability": {
                                "uuid": "${json-unit.any-string}",
                                "vulnId": "INT-123",
                                "source": "INTERNAL",
                                "severity": "UNASSIGNED"
                              },
                              "affectedProjects": {
                                "apiUri": "/api/v1/vulnerability/source/INTERNAL/vuln/INT-123/projects",
                                "frontendUri": "/vulnerabilities/INTERNAL/INT-123/affectedProjects"
                              },
                              "vulnerabilityAnalysisLevel": "BOM_UPLOAD_ANALYSIS"
                            }
                          }
                        }
                        """)
                )
        );
    }

    private void verifyProjectVulnAnalysisCompleteNotification() {
        wireMock.verify(postRequestedFor(urlPathEqualTo("/notification"))
                .withRequestBody(equalToJson("""
                        {
                           "notification" : {
                             "level" : "LEVEL_INFORMATIONAL",
                             "scope" : "SCOPE_PORTFOLIO",
                             "group" : "GROUP_PROJECT_VULN_ANALYSIS_COMPLETE",
                             "timestamp" : "seconds: ${json-unit.any-number}\\n",
                             "title" : "Project vulnrability analysis complete",
                             "content" : "project analysis complete for project foo with id: ${json-unit.any-string} and with version: bar. Vulnerability details added to subject ",
                             "subject" : {
                               "project" : {
                                 "uuid": "${json-unit.any-string}",
                                 "name" : "foo",
                                 "version" : "bar"
                               },
                               "componentAnalysisComplete" : [ {
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
                                 "vulnerability" : [ {
                                   "uuid": "${json-unit.any-string}",
                                   "vulnId" : "INT-123",
                                   "source" : "INTERNAL",
                                   "title" : "NA",
                                   "subTitle" : "NA",
                                   "recommendation" : "NA",
                                   "severity" : "UNASSIGNED"
                                 } ]
                               } ]
                             }
                           }
                         }
                        """)
                )
        );
    }

}