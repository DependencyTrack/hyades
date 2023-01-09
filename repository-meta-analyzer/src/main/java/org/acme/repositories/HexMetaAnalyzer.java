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
import org.json.JSONObject;
import org.acme.model.Component;
import org.acme.model.MetaModel;
import org.acme.model.RepositoryType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An IMetaAnalyzer implementation that supports Hex.
 *
 * @author Steve Springett
 * @since 3.7.0
 */
@ApplicationScoped
public class HexMetaAnalyzer extends AbstractMetaAnalyzer {
    private static final Logger LOGGER = Logger.getLogger(HexMetaAnalyzer.class);
    private static final String DEFAULT_BASE_URL = "https://hex.pm";
    private static final String API_URL = "/api/packages/%s";

    HexMetaAnalyzer() {
        this.baseUrl = DEFAULT_BASE_URL;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isApplicable(final Component component) {
        return component.getPurl() != null && PackageURL.StandardTypes.HEX.equals(component.getPurl().getType());
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryType supportedRepositoryType() {
        return RepositoryType.HEX;
    }

    /**
     * {@inheritDoc}
     */
    public MetaModel analyze(final Component component) {
        final MetaModel meta = new MetaModel(component);
        MetaModel successMeta = new MetaModel(component);
        if (component.getPurl() != null) {

            final String packageName;
            if (component.getPurl().getNamespace() != null) {
                packageName = component.getPurl().getNamespace().replace("@", "%40") + "%2F" + component.getPurl().getName();
            } else {
                packageName = component.getPurl().getName();
            }

            final String url = String.format(baseUrl + API_URL, packageName);
            try (final CloseableHttpResponse response = processHttpRequest(url)) {
                String jsonString = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                    if(!jsonString.equalsIgnoreCase("") && !jsonString.equalsIgnoreCase("{}")) {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        successMeta = processResponse(meta, jsonObject);
                    }
                } else {
                    handleUnexpectedHttpResponse(LOGGER, url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), component);
                }
            } catch (IOException | org.apache.http.ParseException e) {
                handleRequestException(LOGGER, e);
            }
        }
        return successMeta;
    }

    private MetaModel processResponse(MetaModel meta, JSONObject response) {
        if (response != null) {
            if (response.optJSONArray("releases") != null && response.optJSONArray("releases").length() > 0) {
                final JSONObject release = response.optJSONArray("releases").getJSONObject(0);
                final String latest = release.optString("version", null);
                meta.setLatestVersion(latest);
                final String insertedAt = release.optString("inserted_at", null);
                if (insertedAt != null) {
                    final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    try {
                        final Date published = dateFormat.parse(insertedAt);
                        meta.setPublishedTimestamp(published);
                    } catch (ParseException e) {
                        LOGGER.warn("An error occurred while parsing published time", e);
                    }
                }
            }
        }
        return meta;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
