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

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.hyades.model.IntegrityModel;
import org.hyades.model.MetaAnalyzerException;
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An IMetaAnalyzer implementation that supports Pypi.
 *
 * @author Steve Springett
 * @since 3.4.0
 */
public class PypiMetaAnalyzer extends AbstractMetaAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PypiMetaAnalyzer.class);
    private static final String DEFAULT_BASE_URL = "https://pypi.org";
    private static final String API_URL = "/pypi/%s/json";

    PypiMetaAnalyzer() {
        this.baseUrl = DEFAULT_BASE_URL;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isApplicable(final Component component) {
        return component.getPurl() != null && PackageURL.StandardTypes.PYPI.equals(component.getPurl().getType());
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryType supportedRepositoryType() {
        return RepositoryType.PYPI;
    }

    /**
     * {@inheritDoc}
     */
    public MetaModel analyze(final Component component) {
        final MetaModel meta = new MetaModel(component);
        MetaModel successMeta = new MetaModel(component);
        if (component.getPurl() != null) {
            final String url = String.format(baseUrl + API_URL, component.getPurl().getName());
            try (final CloseableHttpResponse response = processHttpGetRequest(url)) {
                if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                    successMeta = processSuccessResponse(response, meta);
                } else {
                    handleUnexpectedHttpResponse(LOGGER, url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), component);
                }
            } catch (IOException e) {
                handleRequestException(LOGGER, e);
            } catch (Exception ex) {
                throw new MetaAnalyzerException(ex);
            }
        }
        return successMeta;
    }

    @Override
    public CloseableHttpResponse getIntegrityCheckResponse(PackageURL packageURL) throws MalformedPackageURLException {
        if (packageURL != null) {
            String type = "tar.gz";
            if (packageURL.getQualifiers() != null) {
                type = packageURL.getQualifiers().getOrDefault("type", "tar.gz");
            }
            final String pythonUrl = baseUrl + "/" + API_URL + "/" + packageURL.getName() + "/" + "/" + packageURL.getName() + "-" + packageURL.getVersion() + "." + type;
            try (final CloseableHttpResponse response = processHttpHeadRequest(pythonUrl)) {
                final StatusLine status = response.getStatusLine();
                if (status.getStatusCode() == HttpStatus.SC_OK) {
                    return response;
                }
            } catch (IOException ex) {
                throw new MetaAnalyzerException(ex);
            }
        }
        throw new MalformedPackageURLException("Purl sent for component integrity check is null");
    }

    private MetaModel processSuccessResponse(CloseableHttpResponse response, MetaModel meta) throws IOException {
        MetaModel updatedMeta = new MetaModel(meta.getComponent());
        String responseString = EntityUtils.toString(response.getEntity());
        if (!responseString.equalsIgnoreCase("") && !responseString.equalsIgnoreCase("{}")) {
            JSONObject jsonResponse = new JSONObject(responseString);
            final JSONObject info = jsonResponse.getJSONObject("info");
            final String latest = info.optString("version", null);
            if (latest != null) {
                meta.setLatestVersion(latest);
                final JSONObject releases = jsonResponse.getJSONObject("releases");
                final JSONArray latestArray = releases.getJSONArray(latest);
                if (latestArray.length() > 0) {
                    final JSONObject release = latestArray.getJSONObject(0);
                    final String updateTime = release.optString("upload_time", null);
                    if (updateTime != null) {
                        updatedMeta = setTimeStamp(meta, updateTime);
                    }
                }
            }
        }
        return updatedMeta;
    }

    private MetaModel setTimeStamp(MetaModel meta, String updateTime) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            final Date published = dateFormat.parse(updateTime);
            meta.setPublishedTimestamp(published);
        } catch (ParseException e) {
            LOGGER.warn("An error occurred while parsing upload time", e);
        }
        return meta;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public IntegrityModel checkIntegrityOfComponent(Component component, CloseableHttpResponse response) {
        return null;
    }
}
