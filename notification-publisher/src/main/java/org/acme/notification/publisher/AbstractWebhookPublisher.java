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
package org.acme.notification.publisher;

import alpine.common.logging.Logger;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.acme.common.ManagedHttpClient;
import org.acme.common.ManagedHttpClientFactory;
import org.acme.commonnotification.NotificationConstants;
import org.acme.commonnotification.NotificationGroup;
import org.acme.commonnotification.NotificationScope;
import org.acme.model.Notification;
import org.acme.model.NotificationLevel;
import org.acme.persistence.ConfigPropertyRepository;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.io.IOException;

public abstract class AbstractWebhookPublisher implements Publisher {
    private static final Logger LOGGER = Logger.getLogger(AbstractWebhookPublisher.class);
    @Inject
    ManagedHttpClientFactory managedHttpClientFactory;

    public void publish(final String publisherName, final PebbleTemplate template, final Notification notification, final JsonObject config, final ConfigPropertyRepository configPropertyRepository) {
        final Logger logger = Logger.getLogger(this.getClass());
        logger.debug("Preparing to publish notification");
        if (config == null) {
            logger.warn("No configuration found. Skipping notification.");
            return;
        }
        final String destination = config.getString("destination");
        final String content = prepareTemplate(notification, template, configPropertyRepository);
        if (destination == null || content == null) {
            logger.warn("A destination or template was not found. Skipping notification");
            return;
        }
        try {
            final HttpPost request = new HttpPost(destination);
            final String mimeType = getTemplateMimeType(config);
            final ManagedHttpClient pooledHttpClient = managedHttpClientFactory.newManagedHttpClient();
            CloseableHttpClient threadSafeClient = pooledHttpClient.getHttpClient();
            request.addHeader("content-type", mimeType);
            request.addHeader("accept", mimeType);
            StringEntity entity = new StringEntity(content);
            request.setEntity(entity);
            final CloseableHttpResponse response = threadSafeClient.execute(request);

            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                logger.error("An error was encountered publishing notification to " + publisherName);
                logger.error("HTTP Status : " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                logger.error("Destination: " + destination);
                logger.debug(content);
            }
        }catch (IOException ex){
            handleRequestException(LOGGER, ex);
        }
    }

    protected void handleRequestException(final Logger logger, final Exception e) {
        logger.error("Request failure", e);
        Notification.dispatch(new Notification()
                .scope(NotificationScope.SYSTEM)
                .group(NotificationGroup.REPOSITORY)
                .title(NotificationConstants.Title.REPO_ERROR)
                .content("An error occurred publishing notification. Check log for details. " + e.getMessage())
                .level(NotificationLevel.ERROR)
        );
    }
}

