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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.protobuf.Any;
import com.google.protobuf.util.Timestamps;
import io.pebbletemplates.pebble.error.ParserException;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkiverse.wiremock.devservice.WireMockConfigKey;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.EntityManager;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.dependencytrack.notification.NotificationConstants;
import org.dependencytrack.proto.notification.v1.BackReference;
import org.dependencytrack.proto.notification.v1.Bom;
import org.dependencytrack.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.BomProcessingFailedSubject;
import org.dependencytrack.proto.notification.v1.BomValidationFailedSubject;
import org.dependencytrack.proto.notification.v1.Component;
import org.dependencytrack.proto.notification.v1.NewVulnerabilitySubject;
import org.dependencytrack.proto.notification.v1.NewVulnerableDependencySubject;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.proto.notification.v1.Project;
import org.dependencytrack.proto.notification.v1.Vulnerability;
import org.dependencytrack.proto.notification.v1.VulnerabilityAnalysis;
import org.dependencytrack.proto.notification.v1.VulnerabilityAnalysisDecisionChangeSubject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_ANALYZER;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_CONSUMED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_PROCESSING_FAILED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_VALIDATION_FAILED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_DATASOURCE_MIRRORING;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_NEW_VULNERABILITY;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_NEW_VULNERABLE_DEPENDENCY;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_PROJECT_AUDIT_CHANGE;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_PORTFOLIO;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_SYSTEM;

@ConnectWireMock
abstract class AbstractPublisherTest<T extends Publisher> {

    WireMock wireMock;

    @ConfigProperty(name = WireMockConfigKey.PORT)
    Integer wireMockPort;

    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    T publisherInstance;

    @Inject
    EntityManager entityManager;

    void testInformWithBomConsumedNotification() throws Exception {
        final var subject = BomConsumedOrProcessedSubject.newBuilder()
                .setProject(createProject())
                .setBom(Bom.newBuilder()
                        .setContent("bomContent")
                        .setFormat("CycloneDX")
                        .setSpecVersion("1.5"))
                .build();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_BOM_CONSUMED)
                .setTitle(NotificationConstants.Title.BOM_CONSUMED)
                .setContent("A CycloneDX BOM was consumed and will be processed")
                .setLevel(LEVEL_INFORMATIONAL)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .setSubject(Any.pack(subject))
                .build();

        assertThatNoException()
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, createConfig()));
    }

    void testInformWithBomProcessingFailedNotification() throws Exception {
        final var subject = BomProcessingFailedSubject.newBuilder()
                .setProject(createProject())
                .setBom(Bom.newBuilder()
                        .setContent("bomContent")
                        .setFormat("CycloneDX")
                        .setSpecVersion("1.5"))
                .setCause("cause")
                .build();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_BOM_PROCESSING_FAILED)
                .setTitle(NotificationConstants.Title.BOM_PROCESSING_FAILED)
                .setContent("An error occurred while processing a BOM")
                .setLevel(LEVEL_ERROR)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .setSubject(Any.pack(subject))
                .build();

        assertThatNoException()
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, createConfig()));
    }

    // https://github.com/DependencyTrack/dependency-track/issues/3197
    void testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject() throws Exception {
        final var subject = BomProcessingFailedSubject.newBuilder()
                .setProject(createProject())
                .setBom(Bom.newBuilder()
                                .setContent("bomContent")
                                .setFormat("CycloneDX")
                        /* .setSpecVersion("1.5") */)
                .setCause("cause")
                .build();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_BOM_PROCESSING_FAILED)
                .setTitle(NotificationConstants.Title.BOM_PROCESSING_FAILED)
                .setContent("An error occurred while processing a BOM")
                .setLevel(LEVEL_ERROR)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .setSubject(Any.pack(subject))
                .build();

        assertThatNoException()
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, createConfig()));
    }

    void testInformWithBomValidationFailedNotificationSubject() throws Exception {
        final var subject = BomValidationFailedSubject.newBuilder()
                .setProject(createProject())
                .setBom(Bom.newBuilder()
                        .setContent("bomContent")
                        .setFormat("CycloneDX"))
                .addErrors("cause 1")
                .addErrors("cause 2")
                .build();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_BOM_VALIDATION_FAILED)
                .setTitle(NotificationConstants.Title.BOM_VALIDATION_FAILED)
                .setContent("An error occurred while validating a BOM")
                .setLevel(LEVEL_ERROR)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .setSubject(Any.pack(subject))
                .build();

        assertThatNoException()
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, createConfig()));
    }

    void testInformWithDataSourceMirroringNotification() throws Exception {
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_SYSTEM)
                .setGroup(GROUP_DATASOURCE_MIRRORING)
                .setTitle(NotificationConstants.Title.GITHUB_ADVISORY_MIRROR)
                .setContent("An error occurred mirroring the contents of GitHub Advisories. Check log for details.")
                .setLevel(LEVEL_ERROR)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .build();

        assertThatNoException()
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, createConfig()));
    }

    void testInformWithNewVulnerabilityNotification() throws Exception {
        final var project = createProject();
        final var component = createComponent();
        final var vuln = createVulnerability();

        final var subject = NewVulnerabilitySubject.newBuilder()
                .setComponent(component)
                .setProject(project)
                .setVulnerability(vuln)
                .setVulnerabilityAnalysisLevel("BOM_UPLOAD_ANALYSIS")
                .addAffectedProjects(project)
                .setAffectedProjectsReference(BackReference.newBuilder()
                        .setApiUri("/api/v1/vulnerability/source/INTERNAL/vuln/INT-001/projects")
                        .setFrontendUri("/vulnerabilities/INTERNAL/INT-001/affectedProjects"))
                .build();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setTitle(NotificationConstants.Title.NEW_VULNERABILITY)
                .setContent("")
                .setLevel(LEVEL_INFORMATIONAL)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .setSubject(Any.pack(subject))
                .build();

        assertThatNoException()
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, createConfig()));
    }

    void testInformWithNewVulnerableDependencyNotification() throws Exception {
        final var project = createProject();
        final var component = createComponent();
        final var vuln = createVulnerability();

        final var subject = NewVulnerableDependencySubject.newBuilder()
                .setComponent(component)
                .setProject(project)
                .addVulnerabilities(vuln).build();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABLE_DEPENDENCY)
                .setTitle(NotificationConstants.Title.NEW_VULNERABLE_DEPENDENCY)
                .setContent("")
                .setLevel(LEVEL_INFORMATIONAL)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .setSubject(Any.pack(subject))
                .build();

        assertThatNoException()
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, createConfig()));
    }

    void testInformWithProjectAuditChangeNotification() throws Exception {
        final var project = createProject();
        final var component = createComponent();
        final var vuln = createVulnerability();
        final var analysis = createAnalysis(component, vuln);

        final var subject = VulnerabilityAnalysisDecisionChangeSubject.newBuilder()
                .setComponent(component)
                .setProject(project)
                .setVulnerability(vuln)
                .setAnalysis(analysis)
                .build();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_PROJECT_AUDIT_CHANGE)
                .setTitle(NotificationConstants.Title.ANALYSIS_DECISION_SUPPRESSED)
                .setContent("")
                .setLevel(LEVEL_INFORMATIONAL)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .setSubject(Any.pack(subject))
                .build();

        assertThatNoException()
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, createConfig()));
    }

    void testInformWithTemplateInclude() throws Exception {
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_SYSTEM)
                .setGroup(GROUP_ANALYZER)
                .setTitle(NotificationConstants.Title.NOTIFICATION_TEST)
                .setLevel(LEVEL_ERROR)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .build();

        final JsonObject config = Json.createObjectBuilder(createConfig())
                .add(Publisher.CONFIG_TEMPLATE_KEY, "{% include '/some/path' %}")
                .build();

        assertThatExceptionOfType(ParserException.class)
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, config))
                .withMessage("Unexpected tag name \"include\" ({% include '/some/path' %}:1)");
    }

    JsonObject createConfig() throws Exception {
        return Json.createObjectBuilder()
                .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, getTemplateMimeType())
                .add(Publisher.CONFIG_TEMPLATE_KEY, getTemplate())
                .addAll(extraConfig())
                .build();
    }

    JsonObjectBuilder extraConfig() {
        return Json.createObjectBuilder();
    }

    private String getTemplateMimeType() {
        if (publisherInstance instanceof CsWebexPublisher
            || publisherInstance instanceof JiraPublisher
            || publisherInstance instanceof MattermostPublisher
            || publisherInstance instanceof MsTeamsPublisher
            || publisherInstance instanceof SlackPublisher
            || publisherInstance instanceof WebhookPublisher) {
            return "application/json";
        } else if (publisherInstance instanceof ConsolePublisher
                   || publisherInstance instanceof SendMailPublisher) {
            return "text/plain";
        }

        throw new IllegalStateException();
    }

    private String getTemplate() throws Exception {
        final String templateFile;
        if (publisherInstance instanceof CsWebexPublisher) {
            templateFile = "cswebex.peb";
        } else if (publisherInstance instanceof ConsolePublisher) {
            templateFile = "console.peb";
        } else if (publisherInstance instanceof SendMailPublisher) {
            templateFile = "email.peb";
        } else if (publisherInstance instanceof JiraPublisher) {
            templateFile = "jira.peb";
        } else if (publisherInstance instanceof MattermostPublisher) {
            templateFile = "mattermost.peb";
        } else if (publisherInstance instanceof MsTeamsPublisher) {
            templateFile = "msteams.peb";
        } else if (publisherInstance instanceof SlackPublisher) {
            templateFile = "slack.peb";
        } else if (publisherInstance instanceof WebhookPublisher) {
            templateFile = "webhook.peb";
        } else {
            throw new IllegalStateException();
        }

        return IOUtils.resourceToString("/templates/" + templateFile, UTF_8);
    }

    private static Component createComponent() {
        return Component.newBuilder()
                .setUuid("94f87321-a5d1-4c2f-b2fe-95165debebc6")
                .setName("componentName")
                .setVersion("componentVersion")
                .build();
    }

    private static Project createProject() {
        return Project.newBuilder()
                .setUuid("c9c9539a-e381-4b36-ac52-6a7ab83b2c95")
                .setName("projectName")
                .setVersion("projectVersion")
                .setDescription("projectDescription")
                .setPurl("pkg:maven/org.acme/projectName@projectVersion")
                .addAllTags(List.of("tag1", "tag2"))
                .build();
    }

    private static Vulnerability createVulnerability() {
        return Vulnerability.newBuilder()
                .setUuid("bccec5d5-ec21-4958-b3e8-22a7a866a05a")
                .setVulnId("INT-001")
                .setSource("INTERNAL")
                .addAliases(Vulnerability.Alias.newBuilder()
                        .setId("OSV-001")
                        .setSource("OSV")
                        .build())
                .setTitle("vulnerabilityTitle")
                .setSubTitle("vulnerabilitySubTitle")
                .setDescription("vulnerabilityDescription")
                .setRecommendation("vulnerabilityRecommendation")
                .setCvssV2(5.5)
                .setCvssV3(6.6)
                .setOwaspRrLikelihood(1.1)
                .setOwaspRrTechnicalImpact(2.2)
                .setOwaspRrBusinessImpact(3.3)
                .setSeverity("MEDIUM")
                .addCwes(Vulnerability.Cwe.newBuilder()
                        .setCweId(666)
                        .setName("Operation on Resource in Wrong Phase of Lifetime"))
                .addCwes(Vulnerability.Cwe.newBuilder()
                        .setCweId(777)
                        .setName("Regular Expression without Anchors"))
                .build();
    }

    private static VulnerabilityAnalysis createAnalysis(final Component component, final Vulnerability vuln) {
        return VulnerabilityAnalysis.newBuilder()
                .setComponent(component)
                .setVulnerability(vuln)
                .setState("FALSE_POSITIVE")
                .setSuppressed(true)
                .build();
    }

    static PublishContext createPublishContext(final Notification notification) throws Exception {
        final var record = new ConsumerRecord<>("topic", 1, 2L, "key", notification);
        return PublishContext.fromRecord(record);
    }

}
