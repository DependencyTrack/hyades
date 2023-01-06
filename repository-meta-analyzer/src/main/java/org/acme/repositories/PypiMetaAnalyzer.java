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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.acme.model.Component;
import org.acme.model.MetaModel;
import org.acme.model.RepositoryType;

import javax.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
public class PypiMetaAnalyzer extends AbstractMetaAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(PypiMetaAnalyzer.class);
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
            final String url = String.format(baseUrl+API_URL, component.getPurl().getName());
            try (final CloseableHttpResponse response = processHttpRequest(url)) {
                if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                    successMeta = processSuccessResponse(response, meta);
                } else {
                    handleUnexpectedHttpResponse(LOGGER, url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), component);
                }
            } catch (IOException e) {
                handleRequestException(LOGGER, e);
            }
        }
        return successMeta;
    }

    private MetaModel processSuccessResponse(CloseableHttpResponse response, MetaModel meta) throws IOException {
        MetaModel updatedMeta = new MetaModel(meta.getComponent());
        String responseString = EntityUtils.toString(response.getEntity());
        if (responseString != null) {
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
}
