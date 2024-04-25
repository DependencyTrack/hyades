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
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.vulnmirror.datasource.osv;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Client for the OSV REST API.
 */
@ApplicationScoped
class OsvClient {

    private final CloseableHttpClient httpClient;
    private final OsvConfig osvConfig;

    @Inject
    OsvClient(@Named("httpClient") final CloseableHttpClient httpClient, final OsvConfig osvConfig) {
        this.httpClient = httpClient;
        this.osvConfig = osvConfig;
    }

    Path downloadEcosystemZip(String ecosystem) throws IOException {
        final String baseUrl = osvConfig.baseUrl().orElseThrow(() -> new IllegalStateException("No base URL configured"));
        ecosystem = Objects.requireNonNull(StringUtils.trimToNull(ecosystem), "Ecosystem cannot be null");
        final var request = new HttpGet(baseUrl + "/" + URLEncoder.encode(ecosystem, StandardCharsets.UTF_8).replace("+", "%20")
                + "/all.zip");
        try (final CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    Path tempFileLocation = Files.createTempFile("osv-ecosystem-" + ecosystem, ".zip");
                    try (FileOutputStream outStream = new FileOutputStream(tempFileLocation.toFile())) {
                        entity.writeTo(outStream);
                        return tempFileLocation;
                    }
                }
            } else {
                throw new WebApplicationException(
                        "Unexpected response status: " + response.getStatusLine().getStatusCode() + " for ecosystem: " + ecosystem,
                        Response.status(response.getStatusLine().getStatusCode()).build());
            }
        }
        return null;
    }

}
