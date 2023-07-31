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
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.hyades.commonutil.DateUtil;
import org.hyades.commonutil.XmlUtil;
import org.hyades.model.IntegrityModel;
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;
import org.hyades.proto.repometaanalysis.v1.HashMatchStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * An IMetaAnalyzer implementation that supports Maven repositories (including Maven Central).
 *
 * @author Steve Springett
 * @since 3.1.0
 */
public class MavenMetaAnalyzer extends AbstractMetaAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenMetaAnalyzer.class);
    private static final String DEFAULT_BASE_URL = "https://repo1.maven.org/maven2";
    private static final String REPO_METADATA_URL = "/%s/maven-metadata.xml";

    MavenMetaAnalyzer() {
        this.baseUrl = DEFAULT_BASE_URL;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isApplicable(final Component component) {
        return component.getPurl() != null && PackageURL.StandardTypes.MAVEN.equals(component.getPurl().getType());
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryType supportedRepositoryType() {
        return RepositoryType.MAVEN;
    }

    /**
     * {@inheritDoc}
     */
    public MetaModel analyze(final Component component) {
        final MetaModel meta = new MetaModel(component);
        if (component.getPurl() != null) {
            final String mavenGavUrl = component.getPurl().getNamespace().replaceAll("\\.", "/") + "/" + component.getPurl().getName();
            final String url = String.format(baseUrl + REPO_METADATA_URL, mavenGavUrl);
            try (final CloseableHttpResponse response = processHttpGetRequest(url)) {
                final StatusLine status = response.getStatusLine();
                if (status.getStatusCode() == HttpStatus.SC_OK) {
                    final HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        try (InputStream in = entity.getContent()) {
                            Document document = XmlUtil.buildSecureDocumentBuilder().parse(in);
                            XPathFactory xpathFactory = XPathFactory.newInstance();
                            XPath xpath = xpathFactory.newXPath();

                            XPathExpression releaseExpression = xpath.compile("/metadata/versioning/release");
                            XPathExpression latestExpression = xpath.compile("/metadata/versioning/latest");
                            String release = (String) releaseExpression.evaluate(document, XPathConstants.STRING);
                            final String latest = (String) latestExpression.evaluate(document, XPathConstants.STRING);

                            final XPathExpression lastUpdatedExpression = xpath.compile("/metadata/versioning/lastUpdated");
                            final String lastUpdated = (String) lastUpdatedExpression.evaluate(document, XPathConstants.STRING);

                            meta.setLatestVersion(release != null ? release : latest);
                            if (lastUpdated != null) {
                                meta.setPublishedTimestamp(DateUtil.parseDate(lastUpdated));
                            }
                        }
                    }
                } else {
                    handleUnexpectedHttpResponse(LOGGER, url, status.getStatusCode(), status.getReasonPhrase(), component);
                }
            } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
                LOGGER.error("Failed to perform repo meta analysis for component with purl:{}", component.getPurl());
                handleRequestException(LOGGER, e);
            }
        }
        return meta;
    }

    @Override
    public IntegrityModel checkIntegrityOfComponent(Component component) throws IOException {
        IntegrityModel integrityModel = new IntegrityModel();
        integrityModel.setComponent(component);
        if (component.getPurl() != null) {
            String type = "jar";
            if (component.getPurl().getQualifiers() != null) {
                type = component.getPurl().getQualifiers().getOrDefault("type", "jar");
            }
            final String mavenGavUrl = component.getPurl().getNamespace().replaceAll("\\.", "/") + "/" + component.getPurl().getName();
            final String url = baseUrl + "/" + mavenGavUrl + "/" + component.getPurl().getVersion() + "/" + component.getPurl().getName() + "-" + component.getPurl().getVersion() + "." + type;
            try (final CloseableHttpResponse response = processHttpHeadRequest(url)) {
                final StatusLine status = response.getStatusLine();
                if (status.getStatusCode() == HttpStatus.SC_OK) {
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
                    if (component.getMd5() == null || component.getMd5().equals("")) {
                        integrityModel.setMd5HashMatched(HashMatchStatus.COMPONENT_MISSING_HASH);
                    } else if (component.getSha1() == null || component.getSha1().equals("")) {
                        integrityModel.setSha1HashMatched(HashMatchStatus.COMPONENT_MISSING_HASH);
                    } else if (component.getSha256() == null || component.getSha256().equals("")) {
                        integrityModel.setSha256HashMatched(HashMatchStatus.COMPONENT_MISSING_HASH);
                    }

                    if (md5.equals("")) {
                        integrityModel.setMd5HashMatched(HashMatchStatus.UNKNOWN);
                    } else if (sha1.equals("")) {
                        integrityModel.setSha1HashMatched(HashMatchStatus.UNKNOWN);
                    } else if (sha256.equals("")) {
                        integrityModel.setSha256HashMatched(HashMatchStatus.UNKNOWN);
                    }
                    if (integrityModel.isMd5HashMatched() == null) {
                        //md5, sha1 or sha256 still "" means that the source of truth repo does not have this hash info and in that case, if there is a match with the others it is a valid component
                        if (component.getMd5() != null && component.getMd5().equals(md5)) {
                            LOGGER.debug("Md5 hash matched: expected value :{}, actual value: {}", component.getMd5(), md5);
                            integrityModel.setMd5HashMatched(HashMatchStatus.PASS);
                        } else {
                            LOGGER.debug("Md5 hash did not match: expected value :{}, actual value: {}", component.getMd5(), md5);
                            integrityModel.setMd5HashMatched(HashMatchStatus.FAIL);
                        }
                    }
                    if (integrityModel.isSha1HashMatched() == null) {
                        if (component.getSha1() != null && component.getSha1().equals(sha1)) {
                            LOGGER.debug("sha1 hash matched: expected value: {}, actual value:{} ", component.getSha1(), sha1);
                            integrityModel.setSha1HashMatched(HashMatchStatus.PASS);
                        } else {
                            LOGGER.debug("sha1 hash did not match: expected value :{}, actual value: {}", component.getSha1(), sha1);
                            integrityModel.setSha1HashMatched(HashMatchStatus.FAIL);
                        }
                    }
                    if (integrityModel.isSha256HashMatched() == null) {
                        if (component.getSha256() != null && component.getSha256().equals(sha256)) {
                            LOGGER.debug("sha256 hash matched: expected value: {}, actual value:{}", component.getSha256(), sha256);
                            integrityModel.setSha256HashMatched(HashMatchStatus.PASS);
                        } else {
                            LOGGER.debug("sha256 hash did not match: expected value :{}, actual value: {}", component.getSha256(), sha256);
                            integrityModel.setSha256HashMatched(HashMatchStatus.FAIL);
                        }
                    }
                }
            }
        }
        return integrityModel;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
