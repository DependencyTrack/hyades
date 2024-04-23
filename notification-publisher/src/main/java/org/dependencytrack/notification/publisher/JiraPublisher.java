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
import org.dependencytrack.proto.notification.v1.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ApplicationScoped
@Startup // Force bean creation even though no direct injection points exist
public class JiraPublisher extends AbstractWebhookPublisher implements Publisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraPublisher.class);

    private final PebbleEngine pebbleEngine;
    private final JiraPublisherConfig publisherConfig;
    private final SecretDecryptor secretDecryptor;
    private String jiraProjectKey;
    private String jiraTicketType;

    @Inject
    JiraPublisher(@Named("pebbleEngineJson") final PebbleEngine pebbleEngine,
                  final JiraPublisherConfig publisherConfig,
                  final SecretDecryptor secretDecryptor) {
        this.pebbleEngine = pebbleEngine;
        this.publisherConfig = publisherConfig;
        this.secretDecryptor = secretDecryptor;
    }

    @Override
    public String getDestinationUrl(final JsonObject config) {
        final String baseUrl = publisherConfig.baseUrl().orElse(null);
        if (baseUrl == null) {
            return null;
        }

        return (baseUrl.endsWith("/") ? baseUrl : baseUrl + '/') + "rest/api/2/issue";
    }

    @Override
    protected AuthCredentials getAuthCredentials() throws Exception {
        final String jiraUsername = publisherConfig.username().orElse(null);
        final String encryptedPassword = publisherConfig.password().orElse(null);
        final String jiraPassword = (encryptedPassword == null) ? null : secretDecryptor.decryptAsString(encryptedPassword);
        return new AuthCredentials(jiraUsername, jiraPassword);
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
