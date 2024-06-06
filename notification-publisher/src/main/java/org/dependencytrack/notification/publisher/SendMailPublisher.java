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

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.quarkus.runtime.Startup;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.mail.MailClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import org.dependencytrack.common.SecretDecryptor;
import org.dependencytrack.persistence.model.Team;
import org.dependencytrack.persistence.repository.UserRepository;
import org.dependencytrack.proto.notification.v1.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ApplicationScoped
@Startup // Force bean creation even though no direct injection points exist
public class SendMailPublisher implements Publisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMailPublisher.class);

    private final PebbleEngine pebbleEngine;
    private final UserRepository userRepository;
    private final SendMailPublisherConfig publisherConfig;
    private final SecretDecryptor secretDecryptor;
    private final Vertx vertx;

    @Inject
    SendMailPublisher(@Named("pebbleEnginePlainText") final PebbleEngine pebbleEngine,
                      final UserRepository userRepository,
                      final SendMailPublisherConfig publisherConfig,
                      final SecretDecryptor secretDecryptor,
                      final Vertx vertx) {
        this.pebbleEngine = pebbleEngine;
        this.userRepository = userRepository;
        this.secretDecryptor = secretDecryptor;
        this.publisherConfig = publisherConfig;
        this.vertx = vertx;
    }

    public void inform(final PublishContext ctx, final Notification notification, final JsonObject config) throws Exception {
        if (config == null) {
            LOGGER.warn("No configuration found; Skipping notification (%s)".formatted(ctx));
            return;
        }
        final String[] destinations = parseDestination(config);
        sendNotification(ctx, notification, config, destinations);
    }

    public void inform(final PublishContext ctx, final Notification notification, final JsonObject config, List<Team> teams) throws Exception {
        if (config == null) {
            LOGGER.warn("No configuration found; Skipping notification (%s)".formatted(ctx));
            return;
        }
        final String[] destinations = parseDestination(config, teams);
        sendNotification(ctx, notification, config, destinations);
    }

    private void sendNotification(final PublishContext ctx, Notification notification, JsonObject config, String[] destinations) throws IOException {
        if (config == null) {
            LOGGER.warn("No publisher configuration found; Skipping notification (%s)".formatted(ctx));
            return;
        }
        if (destinations == null) {
            LOGGER.warn("No destination(s) provided; Skipping notification (%s)".formatted(ctx));
            return;
        }

        final String content;
        try {
            final PebbleTemplate template = getTemplate(config);
            content = prepareTemplate(notification, template, config);
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Failed to prepare notification content (%s)".formatted(ctx), e);
            return;
        }

        boolean smtpEnabled = publisherConfig.isSmtpEnabled().orElse(false);
        if (!smtpEnabled) {
            LOGGER.warn("SMTP is not enabled; Skipping notification (%s)".formatted(ctx));
            return;
        }

        String emailSubjectPrefix;
        // emailSubjectPrefix = qm.getConfigProperty(EMAIL_PREFIX.getGroupName(), EMAIL_PREFIX.getPropertyName()).getPropertyValue();
        //emailSubjectPrefix = emailSubjectPrefix == null ? " " : emailSubjectPrefix;
        emailSubjectPrefix = publisherConfig.emailPrefix().orElse(" ");
        if (emailSubjectPrefix == null) {
            LOGGER.warn("Email prefix is not configured; Skipping notification (%s)".formatted(ctx));
            return;
        }

        final String fromAddress = publisherConfig.fromAddress().orElse(null);
        if (fromAddress == null) {
            LOGGER.warn("From address is not configured; Skipping notification (%s)".formatted(ctx));
            return;
        }

        final MailClient mailClient;
        try {
            mailClient = createMailClient();
        } catch (RuntimeException e) {
            LOGGER.error("Failed to create mail client; Skipping notification (%s)".formatted(ctx), e);
            return;
        }

        try {
            for (String destination : destinations) {
                final var message = new MailMessage();
                message.setFrom(fromAddress);
                message.setTo(destination);
                message.setSubject(emailSubjectPrefix + " " + notification.getTitle());
                message.setText(content);

                mailClient.sendMailAndAwait(message);
                LOGGER.info("Notification successfully sent to destination {} ({})", destination, ctx);
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred sending output email notification ({})", ctx, e);
        } finally {
            mailClient.closeAndForget();
        }
    }

    @Override
    public PebbleEngine getTemplateEngine() {
        return pebbleEngine;
    }

    public static String[] parseDestination(final JsonObject config) {
        JsonString destinationString = config.getJsonString("destination");
        if ((destinationString == null) || destinationString.getString().isEmpty()) {
            return null;
        }
        return destinationString.getString().split(",");
    }

    String[] parseDestination(final JsonObject config, final List<Team> teams) {
        String[] destination = teams.stream().flatMap(
                        team -> Stream.of(
                                        Optional.ofNullable(config.getJsonString("destination"))
                                                .map(JsonString::getString)
                                                .stream()
                                                .flatMap(dest -> Arrays.stream(dest.split(",")))
                                                .filter(Predicate.not(String::isEmpty)),
                                        Optional.ofNullable(userRepository.findEmailsByTeam(team.getId())).orElseGet(Collections::emptyList).stream()
                                )
                                .reduce(Stream::concat)
                                .orElseGet(Stream::empty)
                )
                .distinct()
                .toArray(String[]::new);
        return destination.length == 0 ? null : destination;
    }

    private MailClient createMailClient() {
        // TODO: Can we cache clients based on configuration values?
        //   Ideally we wouldn't create a new client instance if the config did not change.
        //   Caching needs to accommodate for closing of old instances to prevent resource leakage.

        final var mailConfig = new MailConfig();
        mailConfig.setHostname(publisherConfig.serverHostname()
                .orElseThrow(() -> new IllegalArgumentException("No server hostname configured")));
        mailConfig.setPort(publisherConfig.serverPort()
                .orElseThrow(() -> new IllegalArgumentException("No server port configured")));

        publisherConfig.username().ifPresent(mailConfig::setUsername);
        publisherConfig.password()
                .map(encryptedPassword -> {
                    try {
                        return secretDecryptor.decryptAsString(encryptedPassword);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to decrypt password", e);
                    }
                })
                .ifPresent(mailConfig::setPassword);

        publisherConfig.tlsEnabled().ifPresent(mailConfig::setSsl);
        publisherConfig.trustCertificate().ifPresent(mailConfig::setTrustAll);

        return MailClient.create(vertx, mailConfig);
    }

}
