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
package org.acme.repositories;

import alpine.common.logging.Logger;
import com.github.packageurl.PackageURL;

import org.acme.common.ManagedHttpClient;
import org.acme.common.ManagedHttpClientFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.acme.commonutil.HttpUtil;
import org.acme.model.Component;
import org.acme.model.MetaModel;
import org.acme.model.RepositoryType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

/**
 * An IMetaAnalyzer implementation that supports Ruby Gems.
 *
 * @author Steve Springett
 * @since 3.1.0
 */

@ApplicationScoped
public class GemMetaAnalyzer extends AbstractMetaAnalyzer {
    @Inject
    ManagedHttpClientFactory managedHttpClientFactory;
    private static final Logger LOGGER = Logger.getLogger(GemMetaAnalyzer.class);
    private static final String DEFAULT_BASE_URL = "https://rubygems.org";
    private static final String API_URL = "/api/v1/versions/%s/latest.json";

    GemMetaAnalyzer() {
        this.baseUrl = DEFAULT_BASE_URL;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isApplicable(final Component component) {
        return component.getPurl() != null && PackageURL.StandardTypes.GEM.equals(component.getPurl().getType());
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryType supportedRepositoryType() {
        return RepositoryType.GEM;
    }

    /**
     * {@inheritDoc}
     */
    public MetaModel analyze(final Component component) {
        final MetaModel meta = new MetaModel(component);
        if (component.getPurl() != null) {
            final String url = String.format(baseUrl + API_URL, component.getPurl().getName());
            try {
                final HttpUriRequest request = new HttpGet(url);
                request.setHeader("accept", "application/json");
                if (username != null || password != null) {
                    request.setHeader("Authorization", HttpUtil.basicAuthHeaderValue(username, password));
                }
                final ManagedHttpClient pooledHttpClient = managedHttpClientFactory.newManagedHttpClient();
                CloseableHttpClient threadSafeClient = pooledHttpClient.getHttpClient();
                try (final CloseableHttpResponse response = threadSafeClient.execute(request)) {
                    if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                        String jsonString = EntityUtils.toString(response.getEntity());
                        if (jsonString == null) {
                            handleUnexpectedHttpResponse(LOGGER, url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), component);
                        } else {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            final String latest = jsonObject.getString("version");
                            meta.setLatestVersion(latest);
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } catch (org.apache.http.ParseException e) {
                handleRequestException(LOGGER, e);
            }
        }
        return meta;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
