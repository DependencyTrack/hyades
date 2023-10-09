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
package org.hyades.repositories;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hyades.commonutil.DateUtil;
import org.hyades.commonutil.HttpUtil;
import org.hyades.model.IntegrityMeta;
import org.hyades.persistence.model.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Base abstract class that all IMetaAnalyzer implementations should likely extend.
 *
 * @author Steve Springett
 * @since 3.1.0
 */

public abstract class AbstractMetaAnalyzer implements IMetaAnalyzer {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String GMT_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";

    protected CloseableHttpClient httpClient;

    protected String baseUrl;

    protected String username;

    protected String password;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHttpClient(final CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRepositoryBaseUrl(String baseUrl) {
        baseUrl = StringUtils.trimToNull(baseUrl);
        if (baseUrl == null) {
            return;
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
    }

    @Override
    public void setRepositoryUsernameAndPassword(String username, String password) {
        this.username = StringUtils.trimToNull(username);
        this.password = StringUtils.trimToNull(password);
    }

    @Override
    public IntegrityMeta getIntegrityMeta(Component component) {
        throw new UnsupportedOperationException("Skipping integrity meta fetch as this analyzer does not support Integrity Metadata.");
    }

    protected void handleUnexpectedHttpResponse(final Logger logger, String url, final int statusCode, final String statusText, final Component component) {
        logger.debug("HTTP Status : " + statusCode + " " + statusText);
        logger.debug(" - RepositoryType URL : " + url);
        logger.debug(" - Package URL : " + component.getPurl().canonicalize());
        // TODO: Send a notification to the dtrack.notification.repository topic.
        // This should be incorporated into the Kafka Streams topology, otherwise we'll have
        // to spin up a separate producer.
//        Notification.dispatch(new Notification()
//                .scope(NotificationScope.SYSTEM)
//                .group(NotificationGroup.REPOSITORY)
//                .title(NotificationConstants.Title.REPO_ERROR)
//                .content("An error occurred while communicating with an " + supportedRepositoryType().name() + " repository. URL: " + url + " HTTP Status: " + statusCode + ". Check log for details.")
//                .level(NotificationLevel.ERROR)
//        );
    }

    protected void handleRequestException(final Logger logger, final Exception e) {
        logger.error("Request failure", e);
        // TODO: Send a notification to the dtrack.notification.repository topic.
        // This should be incorporated into the Kafka Streams topology, otherwise we'll have
        // to spin up a separate producer.
//        Notification.dispatch(new Notification()
//                .scope(NotificationScope.SYSTEM)
//                .group(NotificationGroup.REPOSITORY)
//                .title(NotificationConstants.Title.REPO_ERROR)
//                .content("An error occurred while communicating with an " + supportedRepositoryType().name() + " repository. Check log for details. " + e.getMessage())
//                .level(NotificationLevel.ERROR)
//        );
    }

    protected CloseableHttpResponse processHttpRequest(String url) throws IOException {
        final HttpUriRequest request = new HttpGet(url);
        request.addHeader("accept", "application/json");
        if (username != null || password != null) {
            request.addHeader("Authorization", HttpUtil.basicAuthHeaderValue(username, password));
        }
        return httpClient.execute(request);
    }

    protected CloseableHttpResponse processHttpHeadRequest(String url) throws IOException {
        final HttpUriRequest request = new HttpHead(url);
        request.addHeader("accept", "application/json");
        if (username != null || password != null) {
            request.addHeader("Authorization", HttpUtil.basicAuthHeaderValue(username, password));
        }
        return httpClient.execute(request);
    }

    protected IntegrityMeta extractIntegrityModelFromResponse(CloseableHttpResponse response, IntegrityMeta integrityMeta, Component component) {
        var headers = response.getAllHeaders();
        for (var header : headers) {
            if (header.getName().equalsIgnoreCase("X-Checksum-MD5")) {
                integrityMeta.setMd5(header.getValue());
            } else if (header.getName().equalsIgnoreCase("X-Checksum-SHA1")) {
                integrityMeta.setSha1(header.getValue());
            } else if (header.getName().equalsIgnoreCase("X-Checksum-SHA256")) {
                integrityMeta.setSha256(header.getValue());
            } else if (header.getName().equalsIgnoreCase("X-Checksum-SHA512")) {
                integrityMeta.setSha512(header.getValue());
            } else if (header.getName().equalsIgnoreCase("Last-Modified") && header.getValue() != null) {
                integrityMeta.setCurrentVersionLastModified(DateUtil.parseDate(header.getValue(), GMT_FORMAT));
            }
        }
        return integrityMeta;
    }

    public IntegrityMeta fetchIntegrityMeta(String url, Component component) {
        var integrityMeta = new IntegrityMeta();
        integrityMeta.setRepositoryUrl(url);
        try (final CloseableHttpResponse response = processHttpHeadRequest(url)) {
            final StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == HttpStatus.SC_OK) {
                return extractIntegrityModelFromResponse(response, integrityMeta, component);
            } else {
                handleUnexpectedHttpResponse(logger, url, status.getStatusCode(), status.getReasonPhrase(), component);
            }
        } catch (Exception ex) {
            logger.warn("Head request for integrity meta failed. Not caching response for component with purl:{}", component.getPurl());
            handleRequestException(logger, ex);
        }
        return integrityMeta;
    }
}
