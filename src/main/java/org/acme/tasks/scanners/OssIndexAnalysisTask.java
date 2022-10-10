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
package org.acme.tasks.scanners;

import alpine.common.logging.Logger;
import alpine.event.framework.Event;
import alpine.event.framework.Subscriber;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import kong.unirest.*;
import kong.unirest.json.JSONObject;
import org.acme.common.*;

import java.time.Instant;
import java.util.*;

import org.acme.consumer.ConfigConsumer;
import org.acme.parser.common.resolver.CweResolver;
import org.acme.producer.VulnerabilityResultProducer;
import us.springett.cvss.Cvss;
import us.springett.cvss.CvssV2;
import us.springett.cvss.CvssV3;
import us.springett.cvss.Score;

import org.acme.model.*;
import org.apache.http.HttpHeaders;
import kong.unirest.UnirestInstance;
import org.acme.event.OssIndexAnalysisEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.acme.parser.OssIndexParser;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Subscriber task that performs an analysis of component using Sonatype OSS Index REST API.
 *
 * @author Steve Springett
 * @since 3.2.0
 */
@ApplicationScoped
public class OssIndexAnalysisTask extends BaseComponentAnalyzerTask implements Subscriber {

    private static final String API_BASE_URL = "https://ossindex.sonatype.org/api/v3/component-report";
    private static final Logger LOGGER = Logger.getLogger(OssIndexAnalysisTask.class);
    private static final int PAGE_SIZE = 100;

    @Inject
    ConfigConsumer configConsumer;
    @Inject
    VulnerablityResult vulnerablityResult;




    @Inject
    VulnerabilityResultProducer vulnerabilityResultProducer;

    @Inject
    OssIndexParser parser;

    private String apiUsername;
    private String apiToken;

    public AnalyzerIdentity getAnalyzerIdentity() {
        return AnalyzerIdentity.OSSINDEX_ANALYZER;
    }

    /**
     * {@inheritDoc}
     */
    public void inform(final Event e) {
        apiUsername = String.valueOf(configConsumer.getConfigProperty(ConfigPropertyConstants.SCANNER_OSSINDEX_API_USERNAME.getPropertyName()).getPropertyValue());
        apiToken = String.valueOf(configConsumer.getConfigProperty(ConfigPropertyConstants.SCANNER_OSSINDEX_API_TOKEN.getPropertyName()).getPropertyValue());
        if (e instanceof OssIndexAnalysisEvent) {

            final OssIndexAnalysisEvent event = (OssIndexAnalysisEvent) e;
            LOGGER.info("Starting Sonatype OSS Index analysis task");
            if (event.getComponents().size() > 0) {
                analyze(event.getComponents());
            }
            LOGGER.info("Sonatype OSS Index analysis complete");
        }
    }
    /**
     * Determines if the {@link OssIndexAnalysisTask} is capable of analyzing the specified Component.
     *
     * @param component the Component to analyze
     * @return true if OssIndexAnalysisTask should analyze, false if not
     */
    public boolean isCapable(final Component component) {
        return component.getPurl() != null
                && component.getPurl().getName() != null
                && component.getPurl().getVersion() != null;
    }

    /**
     * Determines if the {@link OssIndexAnalysisTask} should analyze the specified PackageURL.
     *
     * @param purl the PackageURL to analyze
     * @return true if OssIndexAnalysisTask should analyze, false if not
     */


    /**
     * Analyzes the specified component from local {@link org.dependencytrack.model.ComponentAnalysisCache}.
     *
     * @param component component the Component to analyze from cache
     */

    /**
     * Analyzes a list of Components.
     *
     * @param components a list of Components
     */
    public void analyze(final List<Component> components) {
        final List<String> coordinates = new ArrayList<>();
        for (final Component component : components) {
            if (!component.isInternal() && isCapable(component)) {
                if (component.getPurl() != null) {
                    if (!isCacheCurrent(Vulnerability.Source.OSSINDEX, API_BASE_URL, component.getPurl().toString())) {
                        LOGGER.info("Cache is not current");
                        coordinates.add(minimizePurl(component.getPurl()));
                    } else {
                        LOGGER.info("Cache is current, apply analysis from cache");
                        applyAnalysisFromCache(Vulnerability.Source.OSSINDEX, API_BASE_URL, component.getPurl().toString(), component, getAnalyzerIdentity());
                    }
                }

            }
        }
        if (!CollectionUtils.isEmpty(coordinates)) {
            final JSONObject json = new JSONObject();
            json.put("coordinates", coordinates);
            try {
                final List<ComponentReport> report = submit(json);
                processResults(report, components);

            } catch (UnirestException e) {
            }
            LOGGER.info("Analyzing " + coordinates.size() + " component(s)");
        }

    }

    /**
     * Sonatype OSSIndex (as of December 2018) has an issue that fails to identify vulnerabilities when
     * HTTP POST is used and PackageURL is specified that contains qualifiers (and possibly a subpath).
     * Therefore, this method will return a String representation of a PackageURL without qualifier
     * or subpath.
     * <p>
     * Additionally, as of October 2021, versions prefixed with "v" (as commonly done in the Go and PHP ecosystems)
     * are triggering a bug in OSS Index that causes all vulnerabilities for the given component to be returned,
     * not just the ones for the requested version: https://github.com/OSSIndex/vulns/issues/129#issuecomment-740666614
     * As a result, this method will remove "v" prefixes from versions.
     * <p>
     * This method should be removed at a future date when OSSIndex resolves the issues.
     * <p>
     * TODO: Delete this method and workaround for OSSIndex bugs once Sonatype resolves them.
     *
     * @since 3.4.0
     */
    @Deprecated
    private static String minimizePurl(final PackageURL purl) {
        if (purl == null) {
            return null;
        }
        String p = purl.canonicalize();
        p = p.replaceFirst("@v", "@");
        if (p.contains("?")) {
            p = p.substring(0, p.lastIndexOf("?"));
        }
        if (p.contains("#")) {
            p = p.substring(0, p.lastIndexOf("#"));
        }
        return p;
    }

    /**
     * Submits the payload to the Sonatype OSS Index service
     */
    private List<ComponentReport> submit(final JSONObject payload) throws UnirestException {
        final UnirestInstance ui = UnirestFactory.getUnirestInstance();
        final HttpRequestWithBody request = ui.post(API_BASE_URL)
                .header(HttpHeaders.ACCEPT, "application/json")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.USER_AGENT, ManagedHttpClientFactory.getUserAgent());
        if (apiUsername != null && apiToken != null) {
            request.basicAuth(apiUsername, apiToken);
        }
        final HttpResponse<JsonNode> jsonResponse = request.body(payload).asJson();
        if (jsonResponse.getStatus() == 200) {

            return parser.parse(jsonResponse.getBody());
        } else {
            /*handleUnexpectedHttpResponse(LOGGER, API_BASE_URL, jsonResponse.getStatus(), jsonResponse.getStatusText());*/
        }
        return new ArrayList<>();
    }


    /**
     * Sonatype OSS Index currently uses an old/outdated version of the PackageURL specification.
     * Attempt to convert it into the current spec format and return it.
     */
    private PackageURL oldPurlResolver(String coordinates) {
        try {
            // Check if OSSIndex has updated their implementation or not
            if (coordinates.startsWith("pkg:")) {
                return new PackageURL(coordinates);
            }
            // Nope, they're still using the 'old' style. Force update it.
            return new PackageURL("pkg:" + coordinates.replaceFirst(":", "/"));
        } catch (MalformedPackageURLException e) {
            return null;
        }
    }

    @Override
    protected void applyAnalysisFromCache(Vulnerability.Source source, String targetHost, String target, Component component, AnalyzerIdentity analyzerIdentity) {
        super.applyAnalysisFromCache(source, targetHost, target, component, analyzerIdentity);
    }

    private void processResults(final List<ComponentReport> report, final List<Component> componentsScanned) {
        for (final ComponentReport componentReport : report) {
            for (final Component component : componentsScanned) {
                final String componentPurl = minimizePurl(component.getPurl());
                final PackageURL sonatypePurl = oldPurlResolver(componentReport.getCoordinates());
                final String minimalSonatypePurl = minimizePurl(sonatypePurl);
                if (componentPurl != null && (componentPurl.equals(componentReport.getCoordinates()) ||
                        (sonatypePurl != null && componentPurl.equals(minimalSonatypePurl)))) {
                        Vulnerability vulnerability = null;
                        /*
                        Found the component
                         */
                    for (final ComponentReportVulnerability reportedVuln : componentReport.getVulnerabilities()) {
                        vulnerability = generateVulnerability(reportedVuln);
                        addVulnerabilityToCache(component, vulnerability);

                    }

                    //Event.dispatch(new MetricsUpdateEvent(component));

                    updateComponentAnalysisCache(ComponentAnalysisCache.CacheType.VULNERABILITY, API_BASE_URL, Vulnerability.Source.OSSINDEX.name(), component.getPurl().toString(), Date.from(Instant.now()), component.getCacheResult());
                    if (vulnerability != null) {

                        LOGGER.info("Sending final Vulnerability result back to DT");

                        vulnerablityResult.setComponent(component);
                        vulnerablityResult.setVulnerability(vulnerability);
                        vulnerablityResult.setIdentity(getAnalyzerIdentity());
                        vulnerabilityResultProducer.sendVulnResultToDT(component.getUuid(), vulnerablityResult);
                    }
                }

            }
        }

    }

    private Vulnerability generateVulnerability(final ComponentReportVulnerability reportedVuln) {
        Vulnerability vulnerability = new Vulnerability();
        if (reportedVuln.getCve() != null) {
            vulnerability.setSource(Vulnerability.Source.NVD);
            vulnerability.setVulnId(reportedVuln.getCve());
        } else {
            vulnerability.setSource(Vulnerability.Source.OSSINDEX);
            vulnerability.setVulnId(reportedVuln.getId());
            vulnerability.setTitle(reportedVuln.getTitle());
        }
        vulnerability.setDescription(reportedVuln.getDescription());

        if (reportedVuln.getCwe() != null) {
            CweResolver cweResolver = new CweResolver();
            Cwe cwe  = cweResolver.resolve(reportedVuln.getCwe());
            if (cwe != null) {

                vulnerability.addCwe(cwe);
            }
        }

        final StringBuilder sb = new StringBuilder();
        final String reference = reportedVuln.getReference();
        if (reference != null) {
            sb.append("* [").append(reference).append("](").append(reference).append(")\n");
        }
        for (String externalReference : reportedVuln.getExternalReferences()) {
            sb.append("* [").append(externalReference).append("](").append(externalReference).append(")\n");
        }
        final String references = sb.toString();
        if (references.length() > 0) {
            vulnerability.setReferences(references.substring(0, references.lastIndexOf("\n")));
        }

        if (reportedVuln.getCvssVector() != null) {
            final Cvss cvss = Cvss.fromVector(reportedVuln.getCvssVector());
            if (cvss != null) {
                final Score score = cvss.calculateScore();
                if (cvss instanceof CvssV2) {
                    vulnerability.setCvssV2BaseScore(BigDecimal.valueOf(score.getBaseScore()));
                    vulnerability.setCvssV2ImpactSubScore(BigDecimal.valueOf(score.getImpactSubScore()));
                    vulnerability.setCvssV2ExploitabilitySubScore(BigDecimal.valueOf(score.getExploitabilitySubScore()));
                    vulnerability.setCvssV2Vector(cvss.getVector());
                } else if (cvss instanceof CvssV3) {
                    vulnerability.setCvssV3BaseScore(BigDecimal.valueOf(score.getBaseScore()));
                    vulnerability.setCvssV3ImpactSubScore(BigDecimal.valueOf(score.getImpactSubScore()));
                    vulnerability.setCvssV3ExploitabilitySubScore(BigDecimal.valueOf(score.getExploitabilitySubScore()));
                    vulnerability.setCvssV3Vector(cvss.getVector());
                }
            }
        }
        return vulnerability;
    }

    public Integer parseCweString(final String cweString) {
        if (cweString != null) {
            final String string = cweString.trim();
            String lookupString = "";
            if (string.startsWith("CWE-") && string.contains(" ")) {
                // This is likely to be in the following format:
                // CWE-264 Permissions, Privileges, and Access Controls
                lookupString = string.substring(4, string.indexOf(" "));
            } else if (string.startsWith("CWE-") && string.length() < 9) {
                // This is likely to be in the following format:
                // CWE-264
                lookupString = string.substring(4);
            } else if (string.length() < 5) {
                // This is likely to be in the following format:
                // 264
                lookupString = string;
            }
            try {
                return Integer.valueOf(lookupString);
            } catch (NumberFormatException e) {
                // throw it away
            }
        }
        return null;
    }
}
