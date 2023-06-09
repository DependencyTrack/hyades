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
package org.hyades.notification.publisher;

import io.pebbletemplates.pebble.template.PebbleTemplate;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hyades.persistence.repository.ConfigPropertyRepository;
import org.hyades.proto.notification.v1.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public abstract class AbstractWebhookPublisher implements Publisher {

    @Inject
    @Named("httpClient")
    CloseableHttpClient httpClient;

    public void publish(final String publisherName, final PebbleTemplate template, final Notification notification, final JsonObject config, final ConfigPropertyRepository configPropertyRepository) throws Exception {
        final Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.debug("Preparing to publish notification");
        if (config == null) {
            logger.warn("No configuration found. Skipping notification.");
            return;
        }
        final String destination = getDestinationUrl(config);
        final String content = prepareTemplate(notification, template, configPropertyRepository, config);
        if (destination == null || content == null) {
            logger.warn("A destination or template was not found. Skipping notification");
            return;
        }

        final HttpPost request = new HttpPost(destination);
        final String mimeType = getTemplateMimeType(config);
        request.addHeader("content-type", mimeType);
        request.addHeader("accept", mimeType);
        final BasicAuthCredentials credentials;
        try {
            credentials = getBasicAuthCredentials();
        } catch (PublisherException e) {
            logger.warn("An error occurred during the retrieval of credentials needed for notification publication. Skipping notification", e);
            return;
        }
        if (credentials != null) {
            request.addHeader("Authorization", getBasicAuthenticationHeader(credentials.user(), credentials.password()));
        }
        StringEntity entity = new StringEntity(content);
        request.setEntity(entity);
        try (final CloseableHttpResponse response = httpClient.execute(request);) {
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                logger.error("An error was encountered publishing notification to " + publisherName);
                logger.error("HTTP Status : " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                logger.error("Destination: " + destination);
                logger.debug(content);
            }
        }

    }

    private static final String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    protected BasicAuthCredentials getBasicAuthCredentials() throws Exception {
        return null;
    }

    protected record BasicAuthCredentials(String user, String password) {
    }

    protected String getDestinationUrl(final JsonObject config) {
        return config.getString(CONFIG_DESTINATION);
    }
}

