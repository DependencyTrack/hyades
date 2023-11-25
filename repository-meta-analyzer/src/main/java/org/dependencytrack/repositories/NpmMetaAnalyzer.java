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
package org.dependencytrack.repositories;

import com.github.packageurl.PackageURL;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.dependencytrack.model.IntegrityMeta;
import org.dependencytrack.model.MetaAnalyzerException;
import org.dependencytrack.model.MetaModel;
import org.dependencytrack.persistence.model.Component;
import org.dependencytrack.persistence.model.RepositoryType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An IMetaAnalyzer implementation that supports NPM.
 *
 * @author Steve Springett
 * @since 3.1.0
 */
public class NpmMetaAnalyzer extends AbstractMetaAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpmMetaAnalyzer.class);
    private static final String DEFAULT_BASE_URL = "https://registry.npmjs.org";
    private static final String API_URL = "/-/package/%s/dist-tags";

    NpmMetaAnalyzer() {
        this.baseUrl = DEFAULT_BASE_URL;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isApplicable(final Component component) {
        return component.getPurl() != null && PackageURL.StandardTypes.NPM.equals(component.getPurl().getType());
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryType supportedRepositoryType() {
        return RepositoryType.NPM;
    }

    /**
     * {@inheritDoc}
     */
    public MetaModel analyze(final Component component) {
        final MetaModel meta = new MetaModel(component);
        if (component.getPurl() != null) {

            final String packageName;
            if (component.getPurl().getNamespace() != null) {
                packageName = component.getPurl().getNamespace().replace("@", "%40") + "%2F" + component.getPurl().getName();
            } else {
                packageName = component.getPurl().getName();
            }

            final String url = String.format(baseUrl + API_URL, packageName);
            try (final CloseableHttpResponse response = processHttpRequest(url)) {
                if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                    String responseString = EntityUtils.toString(response.getEntity());
                    if (!responseString.equalsIgnoreCase("") && !responseString.equalsIgnoreCase("{}")) {
                        JSONObject jsonResponse = new JSONObject(responseString);
                        final String latest = jsonResponse.optString("latest");
                        if (latest != null) {
                            meta.setLatestVersion(latest);
                        }
                    }
                } else {
                    handleUnexpectedHttpResponse(LOGGER, url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), component);
                }
            } catch (IOException e) {
                handleRequestException(LOGGER, e);
            } catch (Exception ex) {
                throw new MetaAnalyzerException(ex);
            }
        }
        return meta;
    }

    @Override
    public IntegrityMeta getIntegrityMeta(Component component) {
        if (component != null) {
            var packageUrl = component.getPurl();
            if (packageUrl != null) {
                String type = "tgz";
                if (packageUrl.getQualifiers() != null) {
                    type = packageUrl.getQualifiers().getOrDefault("type", "tgz");
                }
                String npmArtifactoryUrl = "";
                if (packageUrl.getNamespace() != null) {
                    npmArtifactoryUrl += packageUrl.getNamespace().replaceAll("\\.", "/") + "/";
                }
                npmArtifactoryUrl += packageUrl.getName();
                final String url = this.baseUrl + "/" + npmArtifactoryUrl + "/-/"
                        + npmArtifactoryUrl + "-" + packageUrl.getVersion() + "." + type;
                return fetchIntegrityMeta(url, component);
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
