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

import com.github.packageurl.PackageURL;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.hyades.model.IntegrityModel;
import org.hyades.model.MetaAnalyzerException;
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;
import org.hyades.proto.repointegrityanalysis.v1.HashMatchStatus;
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
public class NpmMetaAnalyzer extends AbstractMetaAnalyzer implements IntegrityAnalyzer {

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
            try (final CloseableHttpResponse response = processHttpGetRequest(url)) {
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
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public CloseableHttpResponse getIntegrityCheckResponse(PackageURL packageURL) {
        if (packageURL != null) {
            String type = "tgz";
            if (packageURL.getQualifiers() != null) {
                type = packageURL.getQualifiers().getOrDefault("type", "tgz");
            }
            var purlNamespace = packageURL.getNamespace() != null
                    ? packageURL.getNamespace().replaceAll("\\.", "/")
                    : null;
            String npmArtifactoryUrl = "";
            if (purlNamespace != null) {
                npmArtifactoryUrl += purlNamespace + "/";
            }
            npmArtifactoryUrl += packageURL.getName();
            final String url = this.baseUrl + "/" + npmArtifactoryUrl + "/-/"
                    + npmArtifactoryUrl + "-" + packageURL.getVersion() + "." + type;
            try (final CloseableHttpResponse response = processHttpHeadRequest(url)) {
                final StatusLine status = response.getStatusLine();
                if (status.getStatusCode() == HttpStatus.SC_OK) {
                    return response;
                } else {
                    throw new MetaAnalyzerException("Response status returned is not 200: " + response.getStatusLine().getStatusCode());
                }
            } catch (Exception ex) {
                LOGGER.warn("Head request for npm integrity failed. Not caching response");
                throw new MetaAnalyzerException("Head request for npm integrity failed. Not caching response", ex);
            }
        }
        return null;
    }

    @Override
    public IntegrityModel getIntegrityModel(Component component) {
        if (component != null) {
            var response = getIntegrityCheckResponse(component.getPurl());
            IntegrityModel integrityModel = new IntegrityModel();
            integrityModel.setComponent(component);
            try (response) {
                Header[] headers = response.getAllHeaders();
                String md5 = "";
                String sha1 = "";
                String sha256 = "";
                for (Header header : headers) {
                    if (header.getName().equalsIgnoreCase("X-Checksum-MD5")) {
                        md5 = header.getValue();
                    } else if (header.getName().equalsIgnoreCase("X-Checksum-SHA1")) {
                        sha1 = header.getValue();
                    } else if (header.getName().equalsIgnoreCase("X-Checksum-SHA256")) {
                        sha256 = header.getValue();
                    }
                }
                if (component.getMd5().isEmpty()) {
                    integrityModel.setMd5HashMatched(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH);
                }
                if (component.getSha1().isEmpty()) {
                    integrityModel.setSha1HashMatched(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH);
                }
                if (component.getSha256().isEmpty()) {
                    integrityModel.setSha256HashMatched(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH);
                }
                if (md5.equals("")) {
                    integrityModel.setMd5HashMatched(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN);
                }
                if (sha1.equals("")) {
                    integrityModel.setSha1HashMatched(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN);
                }
                if (sha256.equals("")) {
                    integrityModel.setSha256HashMatched(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN);
                }
                if (integrityModel.isMd5HashMatched() == null) {
                    //md5, sha1 or sha256 still "" means that the source of truth repo does not have this hash info and in that case, if there is a match with the others it is a valid component
                    if (component.getMd5() != null && component.getMd5().equals(md5)) {
                        LOGGER.debug("Md5 hash matched: expected value :{}, actual value: {}", component.getMd5(), md5);
                        integrityModel.setMd5HashMatched(HashMatchStatus.HASH_MATCH_STATUS_PASS);
                    } else {
                        LOGGER.debug("Md5 hash did not match: expected value :{}, actual value: {}", component.getMd5(), md5);
                        integrityModel.setMd5HashMatched(HashMatchStatus.HASH_MATCH_STATUS_FAIL);
                    }
                }
                if (integrityModel.isSha1HashMatched() == null) {
                    if (component.getSha1() != null && component.getSha1().equals(sha1)) {
                        LOGGER.debug("sha1 hash matched: expected value: {}, actual value:{} ", component.getSha1(), sha1);
                        integrityModel.setSha1HashMatched(HashMatchStatus.HASH_MATCH_STATUS_PASS);
                    } else {
                        LOGGER.debug("sha1 hash did not match: expected value :{}, actual value: {}", component.getSha1(), sha1);
                        integrityModel.setSha1HashMatched(HashMatchStatus.HASH_MATCH_STATUS_FAIL);
                    }
                }
                if (integrityModel.isSha256HashMatched() == null) {
                    if (component.getSha256() != null && component.getSha256().equals(sha256)) {
                        LOGGER.debug("sha256 hash matched: expected value: {}, actual value:{}", component.getSha256(), sha256);
                        integrityModel.setSha256HashMatched(HashMatchStatus.HASH_MATCH_STATUS_PASS);
                    } else {
                        LOGGER.debug("sha256 hash did not match: expected value :{}, actual value: {}", component.getSha256(), sha256);
                        integrityModel.setSha256HashMatched(HashMatchStatus.HASH_MATCH_STATUS_FAIL);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("An error occurred while performing head request for component: " + ex);
            }
            return integrityModel;
        }
        return null;
    }
}
