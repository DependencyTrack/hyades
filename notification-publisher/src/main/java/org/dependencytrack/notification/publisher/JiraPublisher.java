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
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import org.dependencytrack.common.SecretDecryptor;
import org.dependencytrack.persistence.dao.ConfigPropertyDao;
import org.dependencytrack.proto.notification.v1.Notification;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.dependencytrack.persistence.model.ConfigProperties.PROPERTY_JIRA_PASSWORD;
import static org.dependencytrack.persistence.model.ConfigProperties.PROPERTY_JIRA_URL;
import static org.dependencytrack.persistence.model.ConfigProperties.PROPERTY_JIRA_USERNAME;

@ApplicationScoped
@Startup // Force bean creation even though no direct injection points exist
public class JiraPublisher extends AbstractWebhookPublisher implements Publisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraPublisher.class);

    private final PebbleEngine pebbleEngine;
    private final Jdbi jdbi;
    private final SecretDecryptor secretDecryptor;
    private String jiraProjectKey;
    private String jiraTicketType;

    @Inject
    public JiraPublisher(@Named("pebbleEngineJson") final PebbleEngine pebbleEngine,
                         final Jdbi jdbi,
                         final SecretDecryptor secretDecryptor) {
        this.pebbleEngine = pebbleEngine;
        this.jdbi = jdbi;
        this.secretDecryptor = secretDecryptor;
    }

    @Override
    public String getDestinationUrl(final JsonObject config) {
        return jdbi.withExtension(ConfigPropertyDao.class, dao -> dao.getValue(PROPERTY_JIRA_URL))
                .map(value -> value.replaceAll("/$", ""))
                .map(value -> value + "/rest/api/2/issue")
                .orElse(null);
    }

    @Override
    protected AuthCredentials getAuthCredentials() throws Exception {
        final String username;
        final String encryptedPassword;
        try (final Handle jdbiHandle = jdbi.open()) {
            final var dao = jdbiHandle.attach(ConfigPropertyDao.class);
            username = dao.getValue(PROPERTY_JIRA_USERNAME).orElse(null);
            encryptedPassword = dao.getValue(PROPERTY_JIRA_PASSWORD).orElse(null);
        }

        final String decryptedPassword = encryptedPassword != null
                ? secretDecryptor.decryptAsString(encryptedPassword)
                : null;

        return new AuthCredentials(username, decryptedPassword);
    }

    @Override
    public void inform(final PublishContext ctx, final Notification notification, final JsonObject config) throws Exception {
        if (config == null) {
            LOGGER.warn("No publisher configuration provided; Skipping notification (%s)".formatted(ctx));
            return;
        }

        jiraTicketType = config.getString("jiraTicketType", null);
        if (jiraTicketType == null) {
            LOGGER.warn("No JIRA ticket type configured; Skipping notification (%s)".formatted(ctx));
            return;
        }

        jiraProjectKey = config.getString(CONFIG_DESTINATION, null);
        if (jiraProjectKey == null) {
            LOGGER.warn("No JIRA project key configured; Skipping notification (%s)".formatted(ctx));
            return;
        }

        publish(ctx, getTemplate(config), notification, config);
    }

    @Override
    public PebbleEngine getTemplateEngine() {
        return pebbleEngine;
    }

    @Override
    public void enrichTemplateContext(final Map<String, Object> context, JsonObject config) {
        jiraTicketType = config.getString("jiraTicketType");
        jiraProjectKey = config.getString(CONFIG_DESTINATION);
        context.put("jiraProjectKey", jiraProjectKey);
        context.put("jiraTicketType", jiraTicketType);
    }
}
