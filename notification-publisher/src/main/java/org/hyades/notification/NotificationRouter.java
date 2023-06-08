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
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.hyades.notification;

import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.parallelconsumer.PCRetriableException;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.hibernate.QueryTimeoutException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.hyades.model.Project;
import org.hyades.model.Team;
import org.hyades.notification.model.NotificationPublisher;
import org.hyades.notification.model.NotificationRule;
import org.hyades.notification.model.NotificationScope;
import org.hyades.notification.model.PublisherClass;
import org.hyades.notification.persistence.NotificationRuleRepository;
import org.hyades.notification.publisher.Publisher;
import org.hyades.notification.publisher.PublisherException;
import org.hyades.notification.publisher.SendMailPublisher;
import org.hyades.persistence.TeamRepository;
import org.hyades.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.hyades.proto.notification.v1.BomProcessingFailedSubject;
import org.hyades.proto.notification.v1.NewVulnerabilitySubject;
import org.hyades.proto.notification.v1.NewVulnerableDependencySubject;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.proto.notification.v1.PolicyViolationAnalysisDecisionChangeSubject;
import org.hyades.proto.notification.v1.PolicyViolationSubject;
import org.hyades.proto.notification.v1.VexConsumedOrProcessedSubject;
import org.hyades.proto.notification.v1.VulnerabilityAnalysisDecisionChangeSubject;
import org.jboss.logging.Logger;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static org.hyades.proto.notification.v1.Scope.SCOPE_PORTFOLIO;
import static org.hyades.proto.notification.v1.Scope.SCOPE_SYSTEM;
import static org.hyades.util.ModelConverter.convert;

@ApplicationScoped
public class NotificationRouter {

    private static final Logger LOGGER = Logger.getLogger(NotificationRouter.class);

    private final ParallelStreamProcessor<String, Notification> parallelConsumer;
    private final NotificationRuleRepository ruleRepository;
    private final TeamRepository teamRepository;

    public NotificationRouter(final ParallelStreamProcessor<String, Notification> parallelConsumer,
                              final NotificationRuleRepository ruleRepository,
                              final TeamRepository teamRepository) {
        this.parallelConsumer = parallelConsumer;
        this.ruleRepository = ruleRepository;
        this.teamRepository = teamRepository;
    }

    void onStart(@Observes final StartupEvent event) {
        parallelConsumer.poll(pollCtx -> {
            try {
                inform(pollCtx.value());
            } catch (RuntimeException e) {
                final Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (rootCause instanceof ConnectTimeoutException
                        || rootCause instanceof QueryTimeoutException
                        || rootCause instanceof SocketTimeoutException) {
                    LOGGER.warn("Encountered retryable exception", e);
                    throw new PCRetriableException(e);
                }

                LOGGER.error("Encountered non-retryable exception; Skipping", e);
            }
        });
    }

    public void inform(final Notification notification) {
        // Workaround for the fact that we can't currently use @Transactional.
        // Even read-only operations require an active transaction in Quarkus,
        // but @Transactional only works when the caller of the method is also
        // a CDI-managed bean. Because we invoke the inform method from the
        // Parallel Consumer thread pool, the caller is not CDI-managed.
        QuarkusTransaction.joiningExisting().run(() -> {
            try {
                informInternal(notification);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void informInternal(final Notification notification) throws Exception {
        for (final NotificationRule rule : resolveRules(notification)) {

            // Not all publishers need configuration (i.e. ConsolePublisher)
            JsonObject config = Json.createObjectBuilder().build();
            if (rule.getPublisherConfig() != null) {
                try (StringReader stringReader = new StringReader(rule.getPublisherConfig());
                     final JsonReader jsonReader = Json.createReader(stringReader)) {
                    config = jsonReader.readObject();
                } catch (Exception e) {
                    LOGGER.error("An error occurred while preparing the configuration for the notification publisher", e);
                }
            }
            try {
                NotificationPublisher notificationPublisher = rule.getPublisher();
                final Class<?> publisherClass = PublisherClass.getPublisherClass(notificationPublisher.getPublisherClass());
                if (publisherClass != null && Publisher.class.isAssignableFrom(publisherClass)) {
                    // Instead of instantiating publisher classes ad-hoc, look the up in the CDI context.
                    // This way publishers can make use of dependency injection.
                    // TODO: Ensure all publisher implementations are available in CDI
                    final Publisher publisher = (Publisher) CDI.current().select(publisherClass).get();
                    JsonObject notificationPublisherConfig = Json.createObjectBuilder()
                            .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, notificationPublisher.getTemplateMimeType())
                            .add(Publisher.CONFIG_TEMPLATE_KEY, notificationPublisher.getTemplate())
                            .addAll(Json.createObjectBuilder(config))
                            .build();

                    final List<Team> ruleTeams = teamRepository.findByNotificationRule(rule.getId());
                    if (publisherClass != SendMailPublisher.class || ruleTeams.isEmpty()) {
                        publisher.inform(notification, notificationPublisherConfig);
                    } else {
                        ((SendMailPublisher) publisher).inform(notification, notificationPublisherConfig, ruleTeams);
                    }


                } else {
                    LOGGER.error("The defined notification publisher is not assignable from " + Publisher.class.getCanonicalName());
                }
            } catch (ClassNotFoundException | UnsatisfiedResolutionException e) {
                LOGGER.error("An error occurred while instantiating a notification publisher", e);
            } catch (PublisherException publisherException) {
                LOGGER.error("An error occured during the publication of the notification", publisherException);
            }
        }
    }

    List<NotificationRule> resolveRules(final Notification notification) throws InvalidProtocolBufferException {
        // The notification rules to process for this specific notification
        final List<NotificationRule> rules = new ArrayList<>();

        if (notification == null) {
            return rules;
        }


        final NotificationScope scope;
        if (notification.getScope() == SCOPE_PORTFOLIO) {
            scope = NotificationScope.PORTFOLIO;
        } else if (notification.getScope() == SCOPE_SYSTEM) {
            scope = NotificationScope.SYSTEM;
        } else {
            LOGGER.error("Invalid notification scope: " + notification.getScope());
            return rules;
        }

        final List<NotificationRule> result = ruleRepository.findByScopeAndForLevel(scope, convert(notification.getLevel()));
        if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(NewVulnerabilitySubject.class)) {
            final var subject = notification.getSubject().unpack(NewVulnerabilitySubject.class);
            // If the rule specified one or more projects as targets, reduce the execution
            // of the notification down to those projects that the rule matches and which
            // also match project the component is included in.
            // NOTE: This logic is slightly different from what is implemented in limitToProject()
            for (final NotificationRule rule : result) {
                if (rule.getNotifyOn().contains(convert(notification.getGroup()))) {
                    if (rule.getProjects() != null && !rule.getProjects().isEmpty()
                            && subject.hasComponent() && subject.hasProject()) {
                        for (final Project project : rule.getProjects()) {
                            if (subject.getProject().getUuid().equals(project.getUuid().toString()) || (Boolean.TRUE.equals(rule.isNotifyChildren() && checkIfChildrenAreAffected(project, subject.getProject().getUuid())))) {
                                rules.add(rule);
                            }
                        }
                    } else {
                        rules.add(rule);
                    }
                }
            }
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(NewVulnerableDependencySubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(NewVulnerableDependencySubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(BomConsumedOrProcessedSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(BomConsumedOrProcessedSubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(BomProcessingFailedSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(BomProcessingFailedSubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(VexConsumedOrProcessedSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(VexConsumedOrProcessedSubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(PolicyViolationSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(PolicyViolationSubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(VulnerabilityAnalysisDecisionChangeSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(VulnerabilityAnalysisDecisionChangeSubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(PolicyViolationAnalysisDecisionChangeSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(PolicyViolationAnalysisDecisionChangeSubject.class).getProject());
        } else {
            for (final NotificationRule rule : result) {
                if (rule.getNotifyOn().contains(convert(notification.getGroup()))) {
                    rules.add(rule);
                }
            }
        }
        return rules;
    }

    /**
     * if the rule specified one or more projects as targets, reduce the execution
     * of the notification down to those projects that the rule matches and which
     * also match projects affected by the vulnerability.
     */
    private void limitToProject(final List<NotificationRule> applicableRules, final List<NotificationRule> rules,
                                final Notification notification, final org.hyades.proto.notification.v1.Project limitToProject) {
        for (final NotificationRule rule : rules) {
            if (rule.getNotifyOn().contains(convert(notification.getGroup()))) {
                if (rule.getProjects() != null && !rule.getProjects().isEmpty()) {
                    for (final Project project : rule.getProjects()) {
                        if (project.getUuid().toString().equals(limitToProject.getUuid()) || (Boolean.TRUE.equals(rule.isNotifyChildren()) && checkIfChildrenAreAffected(project, limitToProject.getUuid()))) {
                            applicableRules.add(rule);
                        }
                    }
                } else {
                    applicableRules.add(rule);
                }
            }
        }
    }

    private boolean checkIfChildrenAreAffected(Project parent, String uuid) {
        boolean isChild = false;
        if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
            return false;
        }
        for (Project child : parent.getChildren()) {
            if ((child.getUuid().toString().equals(uuid) && Boolean.TRUE.equals(child.isActive())) || isChild) {
                return true;
            }
            isChild = checkIfChildrenAreAffected(child, uuid);
        }
        return isChild;
    }
}
