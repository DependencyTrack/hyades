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
import alpine.common.util.Pageable;
import alpine.event.framework.Event;
import alpine.event.framework.Subscriber;
import com.github.packageurl.PackageURL;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;
import kong.unirest.json.JSONObject;
import org.acme.common.ManagedHttpClientFactory;
import org.acme.common.UnirestFactory;
import org.acme.event.OssIndexAnalysisEvent;
import org.acme.model.Component;
import org.acme.model.ComponentAnalysisCache.CacheType;
import org.acme.model.ComponentReport;
import org.acme.model.ComponentReportVulnerability;
import org.acme.model.Cwe;
import org.acme.model.Vulnerability;
import org.acme.model.Vulnerability.Source;
import org.acme.model.VulnerablityResult;
import org.acme.parser.OssIndexParser;
import org.acme.parser.common.resolver.CweResolver;
import org.acme.producer.VulnerabilityResultProducer;
import org.apache.http.HttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import us.springett.cvss.Cvss;
import us.springett.cvss.CvssV2;
import us.springett.cvss.CvssV3;
import us.springett.cvss.Score;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Subscriber task that performs an analysis of component using Sonatype OSS Index REST API.
 *
 * @author Steve Springett
 * @since 3.2.0
 */
//@ApplicationScoped
public class OssIndexAnalysisTask extends BaseComponentAnalyzerTask implements Subscriber {

    private static final String API_BASE_URL = "https://ossindex.sonatype.org/api/v3/component-report";
    private static final Logger LOGGER = Logger.getLogger(OssIndexAnalysisTask.class);
    private static final int PAGE_SIZE = 128;

    @Inject
    VulnerabilityResultProducer vulnerabilityResultProducer;

    @Inject
    OssIndexParser parser;

    private final String apiUsername;
    private final String apiToken;

    private final boolean ossEnabled;

    @Inject
    public OssIndexAnalysisTask(@ConfigProperty(name = "scanner.ossindex.api.username") Optional<String> apiUsername,
                                @ConfigProperty(name = "scanner.ossindex.api.token") Optional<String> apiToken,
                                @ConfigProperty(name = "scanner.cache.validity.period") String cacheValidity,
                                @ConfigProperty(name = "scanner.ossindex.enabled") Optional<Boolean> enabled) {
        super.cacheValidityPeriod = Long.parseLong(cacheValidity);
        this.apiUsername = apiUsername.orElse(null);
        this.apiToken = apiToken.orElse(null);
        this.ossEnabled = enabled.orElse(false);
    }

    public AnalyzerIdentity getAnalyzerIdentity() {
        return AnalyzerIdentity.OSSINDEX_ANALYZER;
    }

    /**
     * {@inheritDoc}
     */
    public void inform(final Event e) {
        if (this.ossEnabled && e instanceof OssIndexAnalysisEvent) {

            final OssIndexAnalysisEvent event = (OssIndexAnalysisEvent) e;
            LOGGER.info("Starting Sonatype OSS Index analysis task");
            if (event.getComponents().size() > 0) {
                analyze(event.getComponents());
            }
            LOGGER.info("Sonatype OSS Index analysis complete");
        } else {
            LOGGER.warn("OSS analyzer is currently disabled.");
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
     * Analyzes a list of Components.
     *
     * @param components a list of Components
     */
    public void analyze(final List<Component> components) {
        // We (ab-)use the fact that OSS Index works exclusively with "minimal" PURLs
        // to reduce the amount of requests we have to make. There may be multiple
        // components with the same PURL, albeit with different qualifiers or sub-paths.
        final var purlComponents = new MultivaluedHashMap<String, Component>();

        for (final Component component : components) {
            // Safety check: Components we can't analyze should've been filtered out in OSSIndexBatcher already.
            if (component.isInternal() || !isCapable(component) || component.getPurl() == null) {
                LOGGER.warn("Incapable of analyzing " + component + "; It should have been filtered out before");
                continue;
            }

            // Immediately provide feedback for components for which we have results cached
            // TODO: Move this to OSSIndexBatcher maybe?
            final String minimizedPurl = minimizePurl(component.getPurl());
            if (isCacheCurrent(Source.OSSINDEX, API_BASE_URL, minimizedPurl)) {
                LOGGER.info("Cache is current for PURL " + minimizedPurl);
                applyAnalysisFromCache(Source.OSSINDEX, API_BASE_URL, minimizedPurl, component, AnalyzerIdentity.OSSINDEX_ANALYZER);
                continue;
            }

            purlComponents.add(minimizedPurl, component);
        }

        final Pageable<String> paginatedPurls = new Pageable<>(PAGE_SIZE, new ArrayList<>(purlComponents.keySet()));
        int purlsAnalyzed = 0;

        while (!paginatedPurls.isPaginationComplete()) {
            final List<String> page = paginatedPurls.getPaginatedList();

            final JSONObject json = new JSONObject();
            json.put("coordinates", page);

            try {
                final List<ComponentReport> report = submit(json);
                processResults(report, purlComponents);
            } catch (UnirestException e) {
                LOGGER.error("Failed to process results", e);
            }

            purlsAnalyzed += page.size();
            LOGGER.info("PURLs analyzed: " + purlsAnalyzed + "/" + paginatedPurls.getList().size());

            paginatedPurls.nextPage();
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
        if (this.apiUsername != null && this.apiToken != null) {
            request.basicAuth(this.apiUsername, this.apiToken);
        }
        final HttpResponse<JsonNode> jsonResponse = request.body(payload).asJson();
        if (jsonResponse.getStatus() == 200) {
            return parser.parse(jsonResponse.getBody());
        } else {
            LOGGER.error("Got unexpected response status from OSS Index: " + jsonResponse.getStatus());
        }
        return new ArrayList<>();
    }

    @Override
    protected void applyAnalysisFromCache(Source source, String targetHost, String target, Component component, AnalyzerIdentity analyzerIdentity) {
        super.applyAnalysisFromCache(source, targetHost, target, component, analyzerIdentity);
    }

    private void processResults(final List<ComponentReport> reports, final MultivaluedMap<String, Component> purlComponents) {
        for (final ComponentReport report : reports) {
            final List<Component> components = purlComponents.get(report.getCoordinates());
            if (components == null || components.isEmpty()) {
                LOGGER.warn("No components found for coordinates " + report.getCoordinates());
                continue;
            } else if (report.getVulnerabilities().isEmpty()) {
                // Report "no vulnerabilities" for all components matching the report PURL
                for (final Component component : components) {
                    final var result = new VulnerablityResult();
                    result.setIdentity(AnalyzerIdentity.OSSINDEX_ANALYZER);
                    vulnerabilityResultProducer.sendVulnResultToDT(component.getUuid(), result);
                }

                // Cache "no vulnerabilities" for the report PURL
                updateComponentAnalysisCache(CacheType.VULNERABILITY, API_BASE_URL,
                        Source.OSSINDEX.name(), report.getCoordinates(), new Date(),
                        Json.createObjectBuilder().add("vulnIds", Json.createArrayBuilder()).build());

                continue;
            }

            JsonObject cacheResult = null;
            for (final ComponentReportVulnerability reportedVulnerability : report.getVulnerabilities()) {
                final Vulnerability vulnerability = generateVulnerability(reportedVulnerability);
                vulnCacheProducer.sendVulnCacheToKafka(vulnerability.getId(), vulnerability);
                cacheResult = addVulnerabilityToCache(cacheResult, vulnerability.getId());

                // Report vulnerability for all components matching the report PURL
                for (final Component component : components) {
                    final var result = new VulnerablityResult();
                    result.setIdentity(AnalyzerIdentity.OSSINDEX_ANALYZER);
                    result.setVulnerability(vulnerability);
                    vulnerabilityResultProducer.sendVulnResultToDT(component.getUuid(), result);
                }
            }

            updateComponentAnalysisCache(CacheType.VULNERABILITY, API_BASE_URL,
                    Source.OSSINDEX.name(), report.getCoordinates(), new Date(), cacheResult);
        }
    }

    private Vulnerability generateVulnerability(final ComponentReportVulnerability reportedVuln) {
        Vulnerability vulnerability = new Vulnerability();
        if (reportedVuln.getCve() != null) {
            vulnerability.setSource(Source.NVD);
            vulnerability.setVulnId(reportedVuln.getCve());
        } else {
            vulnerability.setSource(Source.OSSINDEX);
            vulnerability.setVulnId(reportedVuln.getId());
            vulnerability.setTitle(reportedVuln.getTitle());
        }
        vulnerability.setDescription(reportedVuln.getDescription());

        if (reportedVuln.getCwe() != null) {
            CweResolver cweResolver = new CweResolver();
            Cwe cwe = cweResolver.resolve(reportedVuln.getCwe());
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

        // FIXME: We still use the ID as key for the Kafka topic, but the ID was previously populated
        // by the persistence layer. For now, a hash code of source+vulnId works, but ultimately we
        // should use another key.
        vulnerability.setId(Objects.hash(vulnerability.getSource(), vulnerability.getVulnId()));

        return vulnerability;
    }

}
