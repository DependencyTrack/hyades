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
import org.acme.commonutil.HttpUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.acme.model.MetaModel;
import org.apache.commons.lang3.StringUtils;
//import org.acme.common.UnirestFactory;
import org.acme.model.Component;
import org.acme.model.RepositoryType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @see <a href="https://golang.org/ref/mod#goproxy-protocol">GOPROXY protocol</a>
 *  An IMetaAnalyzer implementation that supports Golang.
 *
 *  @author Steve Springett
 * @since 4.3.0
 */
@ApplicationScoped
public class GoModulesMetaAnalyzer extends AbstractMetaAnalyzer {
    @Inject
    ManagedHttpClientFactory managedHttpClientFactory;
    private static final Logger LOGGER = Logger.getLogger(GoModulesMetaAnalyzer.class);
    private static final String DEFAULT_BASE_URL = "https://proxy.golang.org";
    private static final String API_URL = "/%s/%s/@latest";

    GoModulesMetaAnalyzer() {
        this.baseUrl = DEFAULT_BASE_URL;
    }

    @Override
    public RepositoryType supportedRepositoryType() {
        return RepositoryType.GO_MODULES;
    }

    @Override
    public boolean isApplicable(final Component component) {
        return component.getPurl() != null && PackageURL.StandardTypes.GOLANG.equals(component.getPurl().getType());
    }

    @Override
//    public MetaModel analyze(final Component component) {
//        final var meta = new MetaModel(component);
//        var successMeta = new MetaModel(component);
//
//        if (component.getPurl() == null || component.getPurl().getNamespace() == null) {
//            return meta;
//        }
//
//        final UnirestInstance ui = UnirestFactory.getUnirestInstance();
//        final String url = String.format(baseUrl + API_URL, caseEncode(component.getPurl().getNamespace()), caseEncode(component.getPurl().getName()));
//
//        try {
//            final HttpRequest<GetRequest> request = ui.get(url)
//                    .header("accept", "application/json");
//            if (username != null || password != null) {
//                request.basicAuth(username, password);
//            }
//            final HttpResponse<JsonNode> response = request.asJson();
//
//            if (response.getStatus() == 200) {
//                successMeta = processResponse(meta, response, component);
//            } else {
//                handleUnexpectedHttpResponse(LOGGER, url, response.getStatus(), response.getStatusText(), component);
//            }
//        } catch (UnirestException e) {
//            handleRequestException(LOGGER, e);
//        }
//
//        return successMeta;
//    }

    public MetaModel analyze(final Component component) {
        final var meta = new MetaModel(component);
        var successMeta = new MetaModel(component);

        if (component.getPurl() == null || component.getPurl().getNamespace() == null) {
            return meta;
        }

        final String url = String.format(baseUrl + API_URL, caseEncode(component.getPurl().getNamespace()), caseEncode(component.getPurl().getName()));

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
                    successMeta = processResponse(meta, response, component);
                }
                else {
                    handleUnexpectedHttpResponse(LOGGER, url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), component);
                }
            }
        } catch (org.apache.http.ParseException | IOException e) {
            handleRequestException(LOGGER, e);
        }

        return successMeta;
    }

    private MetaModel processResponse(MetaModel meta, CloseableHttpResponse response, Component component) {
        try {
            String jsonString = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = new JSONObject(jsonString);
            if(response.getEntity()!=null) {
                meta.setLatestVersion(jsonObject.getString("Version"));

                // Module versions are prefixed with "v" in the Go ecosystem.
                // Because some services (like OSS Index as of July 2021) do not support
                // versions with this prefix, components in DT may not be prefixed either.
                //
                // In order to make the versions comparable still, we strip the "v" prefix as well,
                // if it was done for the analyzed component.
                if (component.getVersion() != null && !component.getVersion().startsWith("v")) {
                    meta.setLatestVersion(StringUtils.stripStart(meta.getLatestVersion(), "v"));
                }

                final String commitTimestamp = jsonObject.getString("Time");
                if (StringUtils.isNotBlank(commitTimestamp)) { // Time is optional
                    meta.setPublishedTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(commitTimestamp));
                }
            }
        } catch (ParseException e) {
            handleRequestException(LOGGER, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return meta;
    }

    /**
     * "To avoid ambiguity when serving from case-insensitive file systems, the $module [...] elements are
     * case-encoded by replacing every uppercase letter with an exclamation mark followed by the corresponding
     * lower-case letter."
     *
     * @param modulePath The module path to encode
     * @return The encoded module path
     */
    String caseEncode(final String modulePath) {
        return modulePath.replaceAll("([A-Z])", "!$1").toLowerCase();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
