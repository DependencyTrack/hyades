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
package org.dependencytrack.notification.publisher;

import io.pebbletemplates.pebble.template.PebbleTemplate;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.dependencytrack.persistence.repository.ConfigPropertyRepository;
import org.dependencytrack.proto.notification.v1.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;

public abstract class AbstractWebhookPublisher implements Publisher {

    @Inject
    @Named("httpClient")
    CloseableHttpClient httpClient;

    public void publish(final PublishContext ctx, final PebbleTemplate template, final Notification notification, final JsonObject config, final ConfigPropertyRepository configPropertyRepository) throws Exception {
        final Logger logger = LoggerFactory.getLogger(this.getClass());
        if (config == null) {
            logger.warn("No publisher configuration found; Skipping notification (%s)".formatted(ctx));
            return;
        }
        final String destination = getDestinationUrl(config);
        if (destination == null) {
            logger.warn("No destination configured; Skipping notification (%s)".formatted(ctx));
            return;
        }

        final BasicAuthCredentials credentials;
        try {
            credentials = getBasicAuthCredentials();
        } catch (RuntimeException e) {
            logger.warn("""
                    An error occurred during the retrieval of credentials needed for notification \
                    publication; Skipping notification (%s)""".formatted(ctx), e);
            return;
        }
        final String content;
        try {
            content = prepareTemplate(notification, template, configPropertyRepository, config);
        } catch (IOException | RuntimeException e) {
            logger.error("Failed to prepare notification content (%s)".formatted(ctx), e);
            return;
        }

        final var request = new HttpPost(destination);
        final String mimeType = getTemplateMimeType(config);
        request.addHeader("content-type", mimeType);
        request.addHeader("accept", mimeType);

        if (credentials != null) {
            request.addHeader("Authorization", getBasicAuthenticationHeader(credentials.user(), credentials.password()));
        }
        StringEntity entity = new StringEntity(content);
        request.setEntity(entity);
        try (final CloseableHttpResponse response = httpClient.execute(request);) {
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                logger.warn("Destination responded with with status code %d, likely indicating a processing failure (%s)"
                        .formatted(statusCode, ctx));
                if (logger.isDebugEnabled()) {
                    logger.debug("Response headers: %s".formatted((Object[]) response.getAllHeaders()));
                    logger.debug("Response body: %s".formatted(EntityUtils.toString(response.getEntity())));
                }
            } else if (ctx.shouldLogSuccess()) {
                logger.info("Destination acknowledged reception of notification with status code %d (%s)"
                        .formatted(statusCode, ctx));
            } else {
                logger.info("Destination {} acknowledged reception of notification with status code {} ({})",
                        destination, response.getStatusLine().getStatusCode(), ctx);
            }
        }
    }

    protected void handleRequestException(final PublishContext ctx, final Logger logger, final Exception e) {
        logger.error("Failed to send notification request (%s)".formatted(ctx), e);
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    protected BasicAuthCredentials getBasicAuthCredentials() throws Exception {
        return null;
    }

    protected record BasicAuthCredentials(String user, String password) {
    }

    protected String getDestinationUrl(final JsonObject config) {
        return config.getString(CONFIG_DESTINATION, null);
    }
}

