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
import org.hyades.commonutil.DateUtil;
import org.hyades.commonutil.XmlUtil;
import org.hyades.model.Component;
import org.hyades.model.MetaModel;
import org.hyades.model.RepositoryType;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
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

@ApplicationScoped
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
            try (final CloseableHttpResponse response = processHttpRequest(url)) {
                final StatusLine status = response.getStatusLine();
                if (status.getStatusCode() == 200) {
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
