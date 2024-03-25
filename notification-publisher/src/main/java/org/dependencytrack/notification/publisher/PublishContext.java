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

import com.google.common.base.MoreObjects;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.dependencytrack.persistence.model.NotificationRule;
import org.dependencytrack.proto.ProtobufUtil;
import org.dependencytrack.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.BomProcessingFailedSubject;
import org.dependencytrack.proto.notification.v1.NewVulnerabilitySubject;
import org.dependencytrack.proto.notification.v1.NewVulnerableDependencySubject;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.proto.notification.v1.PolicyViolationAnalysisDecisionChangeSubject;
import org.dependencytrack.proto.notification.v1.PolicyViolationSubject;
import org.dependencytrack.proto.notification.v1.ProjectVulnAnalysisCompleteSubject;
import org.dependencytrack.proto.notification.v1.VexConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.VulnerabilityAnalysisDecisionChangeSubject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PublishContext {

    private static final String SUBJECT_COMPONENT = "component";
    private static final String SUBJECT_PROJECT = "project";
    private static final String SUBJECT_VULNERABILITY = "vulnerability";
    private static final String SUBJECT_VULNERABILITIES = "vulnerabilities";

    private final String kafkaTopic;
    private final int kafkaTopicPartition;
    private final long kafkaPartitionOffset;
    private final String notificationGroup;
    private final String notificationLevel;
    private final String notificationScope;
    private final String notificationTimestamp;
    private final Map<String, Object> notificationSubjects;
    private String ruleName;
    private String ruleScope;
    private String ruleLevel;
    private boolean logSuccess;

    /**
     * Create a new {@link PublishContext} instance.
     *
     * @param kafkaTopic            The Kafka topic the {@link Notification} was retrieved from
     * @param kafkaTopicPartition   The partition of the Kafka topic the {@link Notification} was retrieved from
     * @param kafkaPartitionOffset  The offset in the partition of the Kafka topic the {@link Notification} was retrieved from
     * @param notificationGroup     The group of the {@link Notification}
     * @param notificationLevel     The level of the {@link Notification}
     * @param notificationScope     The scope of the {@link Notification}
     * @param notificationTimestamp The timestamp of the {@link Notification}
     * @param notificationSubjects  The subjects of the {@link Notification} (can contain either {@link Component}, {@link Project}, or both)
     */
    PublishContext(final String kafkaTopic, final int kafkaTopicPartition, final long kafkaPartitionOffset,
                   final String notificationGroup, final String notificationLevel, final String notificationScope,
                   final String notificationTimestamp, final Map<String, Object> notificationSubjects) {
        this.kafkaTopic = kafkaTopic;
        this.kafkaTopicPartition = kafkaTopicPartition;
        this.kafkaPartitionOffset = kafkaPartitionOffset;
        this.notificationGroup = notificationGroup;
        this.notificationLevel = notificationLevel;
        this.notificationScope = notificationScope;
        this.notificationTimestamp = notificationTimestamp;
        this.notificationSubjects = notificationSubjects;
    }

    public static PublishContext fromRecord(final ConsumerRecord<String, Notification> consumerRecord) throws IOException {
        final Notification notification = consumerRecord.value();
        final var notificationSubjects = new HashMap<String, Object>();

        if (notification.getSubject().is(BomConsumedOrProcessedSubject.class)) {
            final BomConsumedOrProcessedSubject subject = notification.getSubject().unpack(BomConsumedOrProcessedSubject.class);
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject.getProject()));
        } else if (notification.getSubject().is(BomProcessingFailedSubject.class)) {
            final BomProcessingFailedSubject subject = notification.getSubject().unpack(BomProcessingFailedSubject.class);
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject.getProject()));
        } else if (notification.getSubject().is(NewVulnerabilitySubject.class)) {
            final NewVulnerabilitySubject subject = notification.getSubject().unpack(NewVulnerabilitySubject.class);
            notificationSubjects.put(SUBJECT_COMPONENT, Component.convert(subject.getComponent()));
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject.getProject()));
            notificationSubjects.put(SUBJECT_VULNERABILITY, Vulnerability.convert(subject.getVulnerability()));
        } else if (notification.getSubject().is(NewVulnerableDependencySubject.class)) {
            final NewVulnerableDependencySubject subject = notification.getSubject().unpack(NewVulnerableDependencySubject.class);
            notificationSubjects.put(SUBJECT_COMPONENT, Component.convert(subject.getComponent()));
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject.getProject()));
            if (subject.getVulnerabilitiesList() != null) {
                notificationSubjects.put(SUBJECT_VULNERABILITIES, subject.getVulnerabilitiesList().stream().map(Vulnerability::convert).toList());
            } else {
                notificationSubjects.put(SUBJECT_VULNERABILITIES, null);
            }
        } else if (notification.getSubject().is(org.dependencytrack.proto.notification.v1.Project.class)) {
            final org.dependencytrack.proto.notification.v1.Project subject = notification.getSubject().unpack(org.dependencytrack.proto.notification.v1.Project.class);
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject));
        } else if (notification.getSubject().is(ProjectVulnAnalysisCompleteSubject.class)) {
            final ProjectVulnAnalysisCompleteSubject subject = notification.getSubject().unpack(ProjectVulnAnalysisCompleteSubject.class);
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject.getProject()));
        } else if (notification.getSubject().is(PolicyViolationSubject.class)) {
            final PolicyViolationSubject subject = notification.getSubject().unpack(PolicyViolationSubject.class);
            notificationSubjects.put(SUBJECT_COMPONENT, Component.convert(subject.getComponent()));
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject.getProject()));
        } else if (notification.getSubject().is(PolicyViolationAnalysisDecisionChangeSubject.class)) {
            final PolicyViolationAnalysisDecisionChangeSubject subject = notification.getSubject().unpack(PolicyViolationAnalysisDecisionChangeSubject.class);
            notificationSubjects.put(SUBJECT_COMPONENT, Component.convert(subject.getComponent()));
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject.getProject()));
        } else if (notification.getSubject().is(VulnerabilityAnalysisDecisionChangeSubject.class)) {
            final VulnerabilityAnalysisDecisionChangeSubject subject = notification.getSubject().unpack(VulnerabilityAnalysisDecisionChangeSubject.class);
            notificationSubjects.put(SUBJECT_COMPONENT, Component.convert(subject.getComponent()));
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject.getProject()));
            notificationSubjects.put(SUBJECT_VULNERABILITY, Vulnerability.convert(subject.getVulnerability()));
        } else if (notification.getSubject().is(VexConsumedOrProcessedSubject.class)) {
            final VexConsumedOrProcessedSubject subject = notification.getSubject().unpack(VexConsumedOrProcessedSubject.class);
            notificationSubjects.put(SUBJECT_PROJECT, Project.convert(subject.getProject()));
        }

        return new PublishContext(consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(),
                notification.getGroup().name(), Optional.ofNullable(notification.getLevel()).map(Enum::name).orElse(null), notification.getScope().name(),
                ProtobufUtil.formatTimestamp(notification.getTimestamp()), notificationSubjects);
    }

    /**
     * Enrich the {@link PublishContext} with additional information about the {@link NotificationRule} once known.
     *
     * @param rule The applicable {@link NotificationRule}
     * @return This {@link PublishContext}
     */
    public PublishContext withRule(final NotificationRule rule) {
        this.ruleName = rule.getName();
        this.ruleLevel = rule.getNotificationLevel().name();
        this.ruleScope = rule.getScope().name();
        this.logSuccess = rule.isLogSuccessfulPublish();
        return this;
    }

    public boolean shouldLogSuccess() {
        return this.logSuccess;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("kafkaTopic", kafkaTopic)
                .add("kafkaTopicPartition", kafkaTopicPartition)
                .add("kafkaPartitionOffset", kafkaPartitionOffset)
                .add("notificationGroup", notificationGroup)
                .add("notificationLevel", notificationLevel)
                .add("notificationScope", notificationScope)
                .add("notificationTimestamp", notificationTimestamp)
                .add("notificationSubjects", notificationSubjects)
                .add("ruleName", ruleName)
                .add("ruleScope", ruleScope)
                .add("ruleLevel", ruleLevel)
                .omitNullValues()
                .toString();
    }

    public record Component(String uuid, String group, String name, String version) {

        private static Component convert(final org.dependencytrack.proto.notification.v1.Component notificationComponent) {
            if (notificationComponent == null) {
                return null;
            }
            return new Component(
                    notificationComponent.getUuid(),
                    notificationComponent.getGroup(),
                    notificationComponent.getName(),
                    notificationComponent.getVersion()
            );
        }

    }

    public record Project(String uuid, String name, String version) {

        private static Project convert(final org.dependencytrack.proto.notification.v1.Project notificationProject) {
            if (notificationProject == null) {
                return null;
            }
            return new Project(
                    notificationProject.getUuid(),
                    notificationProject.getName(),
                    notificationProject.getVersion()
            );
        }

    }

    public record Vulnerability(String id, String source) {

        private static Vulnerability convert(final org.dependencytrack.proto.notification.v1.Vulnerability notificationVuln) {
            if (notificationVuln == null) {
                return null;
            }
            return new Vulnerability(notificationVuln.getVulnId(), notificationVuln.getSource());
        }

    }

    public String kafkaTopic() {
        return kafkaTopic;
    }

    public int kafkaTopicPartition() {
        return kafkaTopicPartition;
    }

    public long kafkaPartitionOffset() {
        return kafkaPartitionOffset;
    }

    public String notificationGroup() {
        return notificationGroup;
    }

    public String notificationLevel() {
        return notificationLevel;
    }

    public String notificationScope() {
        return notificationScope;
    }

    public String notificationTimestamp() {
        return notificationTimestamp;
    }

    public Map<String, Object> notificationSubjects() {
        return notificationSubjects;
    }

    public String ruleName() {
        return ruleName;
    }

    public String ruleScope() {
        return ruleScope;
    }

    public String ruleLevel() {
        return ruleLevel;
    }

}
