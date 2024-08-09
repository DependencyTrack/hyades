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

import com.google.protobuf.Any;
import com.google.protobuf.util.Timestamps;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.dependencytrack.persistence.model.NotificationLevel;
import org.dependencytrack.persistence.model.NotificationRule;
import org.dependencytrack.persistence.model.NotificationScope;
import org.dependencytrack.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.BomProcessingFailedSubject;
import org.dependencytrack.proto.notification.v1.BomValidationFailedSubject;
import org.dependencytrack.proto.notification.v1.Component;
import org.dependencytrack.proto.notification.v1.NewVulnerabilitySubject;
import org.dependencytrack.proto.notification.v1.NewVulnerableDependencySubject;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.proto.notification.v1.PolicyViolationAnalysisDecisionChangeSubject;
import org.dependencytrack.proto.notification.v1.PolicyViolationSubject;
import org.dependencytrack.proto.notification.v1.Project;
import org.dependencytrack.proto.notification.v1.ProjectVulnAnalysisCompleteSubject;
import org.dependencytrack.proto.notification.v1.UserSubject;
import org.dependencytrack.proto.notification.v1.VexConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.VulnerabilityAnalysisDecisionChangeSubject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_CONSUMED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_PROCESSED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_PROCESSING_FAILED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_VALIDATION_FAILED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_NEW_VULNERABILITY;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_NEW_VULNERABLE_DEPENDENCY;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_POLICY_VIOLATION;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_PROJECT_AUDIT_CHANGE;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_PROJECT_CREATED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_PROJECT_VULN_ANALYSIS_COMPLETE;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_USER_CREATED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_VEX_CONSUMED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_VEX_PROCESSED;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_PORTFOLIO;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_SYSTEM;

class PublishContextTest {

    @Nested
    class FromRecordTest {

        @Test
        void testWithBomConsumedSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_BOM_CONSUMED)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(BomConsumedOrProcessedSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_BOM_CONSUMED");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects()).hasEntrySatisfying("project", projectObj -> {
                assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                final var project = (PublishContext.Project) projectObj;
                assertThat(project.uuid()).isEqualTo("projectUuid");
                assertThat(project.name()).isEqualTo("projectName");
                assertThat(project.version()).isEqualTo("projectVersion");
            });
        }

        @Test
        void testWithBomProcessedSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_BOM_PROCESSED)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(BomConsumedOrProcessedSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_BOM_PROCESSED");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects()).hasEntrySatisfying("project", projectObj -> {
                assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                final var project = (PublishContext.Project) projectObj;
                assertThat(project.uuid()).isEqualTo("projectUuid");
                assertThat(project.name()).isEqualTo("projectName");
                assertThat(project.version()).isEqualTo("projectVersion");
            });
        }

        @Test
        void testWithBomProcessingFailedSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_BOM_PROCESSING_FAILED)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(BomProcessingFailedSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_BOM_PROCESSING_FAILED");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects()).hasEntrySatisfying("project", projectObj -> {
                assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                final var project = (PublishContext.Project) projectObj;
                assertThat(project.uuid()).isEqualTo("projectUuid");
                assertThat(project.name()).isEqualTo("projectName");
                assertThat(project.version()).isEqualTo("projectVersion");
            });
        }

        @Test
        void testWithBomValidationFailedSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_BOM_VALIDATION_FAILED)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(BomValidationFailedSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_BOM_VALIDATION_FAILED");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects()).hasEntrySatisfying("project", projectObj -> {
                assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                final var project = (PublishContext.Project) projectObj;
                assertThat(project.uuid()).isEqualTo("projectUuid");
                assertThat(project.name()).isEqualTo("projectName");
                assertThat(project.version()).isEqualTo("projectVersion");
            });
        }

        @Test
        void testWithNewVulnerabilitySubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_NEW_VULNERABILITY)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .setComponent(Component.newBuilder()
                                    .setUuid("componentUuid")
                                    .setGroup("componentGroup")
                                    .setName("componentName")
                                    .setVersion("componentVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_NEW_VULNERABILITY");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects())
                    .hasEntrySatisfying("project", projectObj -> {
                        assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                        final var project = (PublishContext.Project) projectObj;
                        assertThat(project.uuid()).isEqualTo("projectUuid");
                        assertThat(project.name()).isEqualTo("projectName");
                        assertThat(project.version()).isEqualTo("projectVersion");
                    })
                    .hasEntrySatisfying("component", componentObj -> {
                        assertThat(componentObj).isInstanceOf(PublishContext.Component.class);
                        final var component = (PublishContext.Component) componentObj;
                        assertThat(component.uuid()).isEqualTo("componentUuid");
                        assertThat(component.group()).isEqualTo("componentGroup");
                        assertThat(component.name()).isEqualTo("componentName");
                        assertThat(component.version()).isEqualTo("componentVersion");
                    });
        }

        @Test
        void testWithNewVulnerableDependencySubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_NEW_VULNERABLE_DEPENDENCY)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(NewVulnerableDependencySubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .setComponent(Component.newBuilder()
                                    .setUuid("componentUuid")
                                    .setGroup("componentGroup")
                                    .setName("componentName")
                                    .setVersion("componentVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_NEW_VULNERABLE_DEPENDENCY");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects())
                    .hasEntrySatisfying("project", projectObj -> {
                        assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                        final var project = (PublishContext.Project) projectObj;
                        assertThat(project.uuid()).isEqualTo("projectUuid");
                        assertThat(project.name()).isEqualTo("projectName");
                        assertThat(project.version()).isEqualTo("projectVersion");
                    })
                    .hasEntrySatisfying("component", componentObj -> {
                        assertThat(componentObj).isInstanceOf(PublishContext.Component.class);
                        final var component = (PublishContext.Component) componentObj;
                        assertThat(component.uuid()).isEqualTo("componentUuid");
                        assertThat(component.group()).isEqualTo("componentGroup");
                        assertThat(component.name()).isEqualTo("componentName");
                        assertThat(component.version()).isEqualTo("componentVersion");
                    });
        }

        @Test
        void testWithProjectCreatedSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_PROJECT_CREATED)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(Project.newBuilder()
                            .setUuid("projectUuid")
                            .setName("projectName")
                            .setVersion("projectVersion")
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_PROJECT_CREATED");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects()).hasEntrySatisfying("project", projectObj -> {
                assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                final var project = (PublishContext.Project) projectObj;
                assertThat(project.uuid()).isEqualTo("projectUuid");
                assertThat(project.name()).isEqualTo("projectName");
                assertThat(project.version()).isEqualTo("projectVersion");
            });
        }

        @Test
        void testWithProjectVulnAnalysisCompletedSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_PROJECT_VULN_ANALYSIS_COMPLETE)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(ProjectVulnAnalysisCompleteSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_PROJECT_VULN_ANALYSIS_COMPLETE");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects()).hasEntrySatisfying("project", projectObj -> {
                assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                final var project = (PublishContext.Project) projectObj;
                assertThat(project.uuid()).isEqualTo("projectUuid");
                assertThat(project.name()).isEqualTo("projectName");
                assertThat(project.version()).isEqualTo("projectVersion");
            });
        }

        @Test
        void testWithPolicyViolationSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_POLICY_VIOLATION)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(PolicyViolationSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .setComponent(Component.newBuilder()
                                    .setUuid("componentUuid")
                                    .setGroup("componentGroup")
                                    .setName("componentName")
                                    .setVersion("componentVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_POLICY_VIOLATION");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects())
                    .hasEntrySatisfying("project", projectObj -> {
                        assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                        final var project = (PublishContext.Project) projectObj;
                        assertThat(project.uuid()).isEqualTo("projectUuid");
                        assertThat(project.name()).isEqualTo("projectName");
                        assertThat(project.version()).isEqualTo("projectVersion");
                    })
                    .hasEntrySatisfying("component", componentObj -> {
                        assertThat(componentObj).isInstanceOf(PublishContext.Component.class);
                        final var component = (PublishContext.Component) componentObj;
                        assertThat(component.uuid()).isEqualTo("componentUuid");
                        assertThat(component.group()).isEqualTo("componentGroup");
                        assertThat(component.name()).isEqualTo("componentName");
                        assertThat(component.version()).isEqualTo("componentVersion");
                    });
        }

        @Test
        void testWithPolicyViolationAnalysisDecisionChangeSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_PROJECT_AUDIT_CHANGE)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(PolicyViolationAnalysisDecisionChangeSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .setComponent(Component.newBuilder()
                                    .setUuid("componentUuid")
                                    .setGroup("componentGroup")
                                    .setName("componentName")
                                    .setVersion("componentVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_PROJECT_AUDIT_CHANGE");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects())
                    .hasEntrySatisfying("project", projectObj -> {
                        assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                        final var project = (PublishContext.Project) projectObj;
                        assertThat(project.uuid()).isEqualTo("projectUuid");
                        assertThat(project.name()).isEqualTo("projectName");
                        assertThat(project.version()).isEqualTo("projectVersion");
                    })
                    .hasEntrySatisfying("component", componentObj -> {
                        assertThat(componentObj).isInstanceOf(PublishContext.Component.class);
                        final var component = (PublishContext.Component) componentObj;
                        assertThat(component.uuid()).isEqualTo("componentUuid");
                        assertThat(component.group()).isEqualTo("componentGroup");
                        assertThat(component.name()).isEqualTo("componentName");
                        assertThat(component.version()).isEqualTo("componentVersion");
                    });
        }

        @Test
        void testWithVulnerabilityAnalysisDecisionChangeSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_PROJECT_AUDIT_CHANGE)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(VulnerabilityAnalysisDecisionChangeSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .setComponent(Component.newBuilder()
                                    .setUuid("componentUuid")
                                    .setGroup("componentGroup")
                                    .setName("componentName")
                                    .setVersion("componentVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_PROJECT_AUDIT_CHANGE");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects())
                    .hasEntrySatisfying("project", projectObj -> {
                        assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                        final var project = (PublishContext.Project) projectObj;
                        assertThat(project.uuid()).isEqualTo("projectUuid");
                        assertThat(project.name()).isEqualTo("projectName");
                        assertThat(project.version()).isEqualTo("projectVersion");
                    })
                    .hasEntrySatisfying("component", componentObj -> {
                        assertThat(componentObj).isInstanceOf(PublishContext.Component.class);
                        final var component = (PublishContext.Component) componentObj;
                        assertThat(component.uuid()).isEqualTo("componentUuid");
                        assertThat(component.group()).isEqualTo("componentGroup");
                        assertThat(component.name()).isEqualTo("componentName");
                        assertThat(component.version()).isEqualTo("componentVersion");
                    });
        }

        @Test
        void testWithVexConsumedSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_VEX_CONSUMED)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(VexConsumedOrProcessedSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_VEX_CONSUMED");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects()).hasEntrySatisfying("project", projectObj -> {
                assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                final var project = (PublishContext.Project) projectObj;
                assertThat(project.uuid()).isEqualTo("projectUuid");
                assertThat(project.name()).isEqualTo("projectName");
                assertThat(project.version()).isEqualTo("projectVersion");
            });
        }

        @Test
        void testWithVexProcessedSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_VEX_PROCESSED)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_PORTFOLIO)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(VexConsumedOrProcessedSubject.newBuilder()
                            .setProject(Project.newBuilder()
                                    .setUuid("projectUuid")
                                    .setName("projectName")
                                    .setVersion("projectVersion"))
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_VEX_PROCESSED");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_PORTFOLIO");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects()).hasEntrySatisfying("project", projectObj -> {
                assertThat(projectObj).isInstanceOf(PublishContext.Project.class);
                final var project = (PublishContext.Project) projectObj;
                assertThat(project.uuid()).isEqualTo("projectUuid");
                assertThat(project.name()).isEqualTo("projectName");
                assertThat(project.version()).isEqualTo("projectVersion");
            });
        }

        @Test
        void testWithUserSubject() throws Exception {
            final var notification = Notification.newBuilder()
                    .setGroup(GROUP_USER_CREATED)
                    .setLevel(LEVEL_INFORMATIONAL)
                    .setScope(SCOPE_SYSTEM)
                    .setTimestamp(Timestamps.fromSeconds(666))
                    .setSubject(Any.pack(UserSubject.newBuilder()
                            .setUsername("username")
                            .setEmail("email.com")
                            .build()))
                    .build();

            final PublishContext ctx = PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
            assertThat(ctx.kafkaTopic()).isEqualTo("topic");
            assertThat(ctx.kafkaTopicPartition()).isEqualTo(1);
            assertThat(ctx.kafkaPartitionOffset()).isEqualTo(2L);
            assertThat(ctx.notificationGroup()).isEqualTo("GROUP_USER_CREATED");
            assertThat(ctx.notificationLevel()).isEqualTo("LEVEL_INFORMATIONAL");
            assertThat(ctx.notificationScope()).isEqualTo("SCOPE_SYSTEM");
            assertThat(ctx.notificationTimestamp()).isEqualTo("1970-01-01T00:11:06.000Z");
            assertThat(ctx.notificationSubjects())
                    .hasEntrySatisfying("user", userObj -> {
                        assertThat(userObj).isInstanceOf(PublishContext.User.class);
                        final var user = (PublishContext.User) userObj;
                        assertThat(user.username()).isEqualTo("username");
                        assertThat(user.email()).isEqualTo("email.com");
                    });
        }
    }

    @Test
    void testWithRule() {
        var ctx = new PublishContext(null, 0, 0L, null, null, null, null, null);

        final var notificationRule = new NotificationRule();
        notificationRule.setName("foo");
        notificationRule.setNotificationLevel(NotificationLevel.ERROR);
        notificationRule.setScope(NotificationScope.SYSTEM);

        ctx = ctx.withRule(notificationRule);
        assertThat(ctx.ruleName()).isEqualTo("foo");
        assertThat(ctx.ruleLevel()).isEqualTo("ERROR");
        assertThat(ctx.ruleScope()).isEqualTo("SYSTEM");
    }

}