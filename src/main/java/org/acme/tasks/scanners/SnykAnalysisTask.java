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
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import org.acme.common.UnirestFactory;
import org.acme.event.SnykAnalysisEvent;
import org.acme.model.Component;
import org.acme.model.ComponentAnalysisCache.CacheType;
import org.acme.model.Cwe;
import org.acme.model.Severity;
import org.acme.model.Vulnerability;
import org.acme.model.Vulnerability.Source;
import org.acme.model.VulnerabilityAlias;
import org.acme.model.VulnerableSoftware;
import org.acme.model.VulnerablityResult;
import org.acme.parser.common.resolver.CweResolver;
import org.acme.producer.VulnerabilityResultProducer;
import org.apache.http.HttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.acme.util.JsonUtil.jsonStringToTimestamp;

/**
 * Subscriber task that performs an analysis of component using Snyk vulnerability REST API.
 */
//@ApplicationScoped
public class SnykAnalysisTask extends BaseComponentAnalyzerTask implements Subscriber {
    @Inject
    CweResolver cweResolver;

    private final Logger LOGGER = Logger.getLogger(SnykAnalysisTask.class);

    private String API_BASE_URL = "";

    private String orgId;

    private String snykToken;

    private boolean snykEnabled;
    @Inject
    VulnerabilityResultProducer vulnerabilityResultProducer;

    public AnalyzerIdentity getAnalyzerIdentity() {
        return AnalyzerIdentity.SNYK_ANALYZER;
    }

    @Inject
    public SnykAnalysisTask(@ConfigProperty(name = "scanner.snyk.org.id") Optional<String> orgId,
                            @ConfigProperty(name = "scanner.snyk.token") Optional<String> snykToken,
                            @ConfigProperty(name = "scanner.cache.validity.period") long cacheValidity,
                            @ConfigProperty(name = "scanner.snyk.enabled") Optional<Boolean> isEnabled) {
        super.cacheValidityPeriod = cacheValidity;
        this.orgId = orgId.orElse(null);
        this.snykToken = snykToken.map(token -> "token " + token).orElse(null);
        this.snykEnabled = isEnabled.orElse(false);
    }

    public SnykAnalysisTask() {

    }

    /**
     * {@inheritDoc}
     */
    public void inform(final Event e) {
        if(this.snykEnabled && this.orgId != null && this.snykToken != null){
            API_BASE_URL = "https://api.snyk.io/rest/orgs/" + this.orgId + "/packages/";
            Instant start = Instant.now();
            SnykAnalysisEvent event = (SnykAnalysisEvent) e;
            LOGGER.info("Starting Snyk vulnerability analysis task");
            if (!event.getComponents().isEmpty()) {
                analyze(event.getComponents());
            }
            LOGGER.info("Snyk vulnerability analysis complete");
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            LOGGER.info("Time taken to complete snyk vulnerability analysis task: " + timeElapsed.toMillis() + " milliseconds");
        } else{
            LOGGER.warn("SNYK analyzer is currently disabled.");
        }
    }

    /**
     * Determines if the {@link SnykAnalysisTask} is capable of analyzing the specified Component.
     *
     * @param component the Component to analyze
     * @return true if SnykAnalysisTask should analyze, false if not
     */
    public boolean isCapable(final Component component) {
        return component.getPurl() != null
                && component.getPurl().getScheme() != null
                && component.getPurl().getType() != null
                && component.getPurl().getName() != null
                && component.getPurl().getVersion() != null;
    }

    private String parsePurlToSnykUrlParam(PackageURL purl) {

        String url = purl.getScheme() + "%3A" + purl.getType() + "%2f";
        if (purl.getNamespace() != null) {
            url = url + purl.getNamespace() + "%2f";
        }
        url = url + purl.getName() + "%40" + purl.getVersion();
        return url;
    }

    /**
     * Analyzes a list of Components.
     *
     * @param components a list of Components
     */
    public void analyze(final List<Component> components) {
        int PAGE_SIZE = 100;
        final Pageable<Component> paginatedComponents = new Pageable<>(PAGE_SIZE, components);
        while (!paginatedComponents.isPaginationComplete()) {
            final List<Component> paginatedList = paginatedComponents.getPaginatedList();
            //Starting with number of threads as 10
            LOGGER.info("Printing current page size: " + paginatedList.size());
            int numThreads = 10;
            int trackComponent = 0;
            try {
                while (trackComponent < paginatedList.size()) {
                    for (int i = 0; i < numThreads; i++) {
                        final List<Component> temp = new ArrayList<>();
                        int k = 0;
                        while (trackComponent < paginatedList.size() && k < numThreads) {
                            temp.add(paginatedList.get(trackComponent));
                            k += 1;
                            trackComponent += 1;
                        }
                        for (Component component : temp) {
                            LOGGER.info(component.getName());
                        }
                        Thread analysisUtil = new Thread(new SnykAnalysisTaskUtil(temp, this.snykToken));
                        analysisUtil.start();
                        analysisUtil.join();
                    }
                }
                paginatedComponents.nextPage();
            } catch (InterruptedException ex) {
                LOGGER.info("Exception occured: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public void handle(Component component, JSONObject object) {
        try {
            String purl = null;
            final JSONObject metaInfo = object.optJSONObject("meta");
            if (metaInfo != null) {
                purl = metaInfo.optJSONObject("package").optString("url");
                if (purl == null) {
                    purl = component.getPurl().toString();
                }
            }
            JsonObject cacheResult = null;
            final JSONArray data = object.optJSONArray("data");
            if (data != null) {
                for (int count = 0; count < data.length(); count++) {
                    Vulnerability vulnerability = new Vulnerability();
                    vulnerability.setSource(Source.SNYK);
                    // get the id of the data record (vulnerability)
                    vulnerability.setVulnId(data.optJSONObject(count).optString("id", null));
                    final JSONObject vulnAttributes = data.optJSONObject(count).optJSONObject("attributes");
                    if (vulnAttributes != null) {
                        if (vulnAttributes.optString("type").equalsIgnoreCase("package_vulnerability")) {
                            // get the references of the data record (vulnerability)
                            final JSONObject slots = vulnAttributes.optJSONObject("slots");
                            if (slots != null) {
                                final JSONArray links = slots.optJSONArray("references");
                                if (links != null) {
                                    final StringBuilder sb = new StringBuilder();
                                    for (int linkCount = 0; linkCount < links.length(); linkCount++) {
                                        final JSONObject link = links.getJSONObject(linkCount);
                                        String reference = link.optString("url", null);
                                        if (reference != null) {
                                            sb.append("* [").append(reference).append("](").append(reference).append(")\n");
                                        }
                                    }
                                    vulnerability.setReferences(sb.toString());
                                }
                            }
                            vulnerability.setTitle(vulnAttributes.optString("title", null));
                            vulnerability.setDescription(vulnAttributes.optString("description", null));
                            vulnerability.setCreated(Date.from(jsonStringToTimestamp(vulnAttributes.optString("created_at")).toInstant()));
                            vulnerability.setUpdated(Date.from(jsonStringToTimestamp(vulnAttributes.optString("updated_at")).toInstant()));

                            final JSONArray problems = vulnAttributes.optJSONArray("problems");
                            if (problems != null) {
                                List<VulnerabilityAlias> vulnerabilityAliasList = new ArrayList<>();
                                for (int i = 0; i < problems.length(); i++) {
                                    final JSONObject problem = problems.optJSONObject(i);
                                    String source = problem.optString("source");
                                    String id = problem.optString("id");
                                    // CWE
                                    if (source.equalsIgnoreCase("CWE")) {
                                        Cwe cwe = cweResolver.resolve(id);
                                        if (cwe != null) {
                                            vulnerability.addCwe(cwe);
                                        }
                                    }
                                }
                                vulnerability.setAliases(vulnerabilityAliasList);
                            }

                            final JSONArray cvssArray = vulnAttributes.optJSONArray("severities");
                            if (cvssArray != null) {
                                final JSONObject cvss = cvssArray.getJSONObject(0);
                                if (cvss != null) {
                                    String severity = cvss.optString("level", null);
                                    if (severity != null) {
                                        if (severity.equalsIgnoreCase("CRITICAL")) {
                                            vulnerability.setSeverity(Severity.CRITICAL);
                                        } else if (severity.equalsIgnoreCase("HIGH")) {
                                            vulnerability.setSeverity(Severity.HIGH);
                                        } else if (severity.equalsIgnoreCase("MEDIUM")) {
                                            vulnerability.setSeverity(Severity.MEDIUM);
                                        } else if (severity.equalsIgnoreCase("LOW")) {
                                            vulnerability.setSeverity(Severity.LOW);
                                        } else {
                                            vulnerability.setSeverity(Severity.UNASSIGNED);
                                        }
                                    }
                                    vulnerability.setCvssV3Vector(cvss.optString("vector", null));
                                    final JSONObject cvssScore = cvss.optJSONObject("score");
                                    if (cvssScore != null) {
                                        vulnerability.setCvssV3BaseScore(BigDecimal.valueOf(Double.parseDouble(cvssScore.optString("base_score"))));
                                    }
                                }
                            }

                            JSONArray coordinates = vulnAttributes.optJSONArray("coordinates");
                            if (coordinates != null) {
                                for (int countCoordinates = 0; countCoordinates < coordinates.length(); countCoordinates++) {
                                    JSONArray representation = coordinates.getJSONObject(countCoordinates).optJSONArray("representation");
                                    if (representation != null) {
                                        final List<VulnerableSoftware> vsList = parseVersionRanges(purl, representation);
                                        vulnerability.setVulnerableSoftware(vsList);
                                    }
                                }
                            }

                            // FIXME: We still use the ID as key for the Kafka topic, but the ID was previously populated
                            // by the persistence layer. For now, a hash code of source+vulnId works, but ultimately we
                            // should use another key.
                            vulnerability.setId(Objects.hash(vulnerability.getSource(), vulnerability.getVulnId()));
                            vulnCacheProducer.sendVulnCacheToKafka(vulnerability.getId(), vulnerability);
                            cacheResult = addVulnerabilityToCache(cacheResult, vulnerability.getId());

                            final var result = new VulnerablityResult();
                            result.setVulnerability(vulnerability);
                            result.setIdentity(AnalyzerIdentity.SNYK_ANALYZER);
                            vulnerabilityResultProducer.sendVulnResultToDT(component.getUuid(), result);
                        }
                    }
                }
            }
            updateComponentAnalysisCache(CacheType.VULNERABILITY, API_BASE_URL, Source.SNYK.name(), component.getPurl().toString(), new Date(), cacheResult);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<VulnerableSoftware> parseVersionRanges(final String purl, final JSONArray ranges) {

        ArrayList<VulnerableSoftware> vulnerableSoftwares = new ArrayList<>();
        if (purl == null) {
            LOGGER.debug("No PURL provided - skipping");
            return null;
        }

        final PackageURL packageURL;
        try {
            packageURL = new PackageURL(purl);
        } catch (MalformedPackageURLException ex) {
            LOGGER.debug("Invalid PURL  " + purl + " - skipping", ex);
            return null;
        }

        for (int i = 0; i < ranges.length(); i++) {

            String range = ranges.optString(i);
            String versionStartIncluding = null;
            String versionStartExcluding = null;
            String versionEndIncluding = null;
            String versionEndExcluding = null;
            final String[] parts;

            if (range.contains(",")) {
                parts = Arrays.stream(range.split(",")).map(String::trim).toArray(String[]::new);
            } else {
                parts = Arrays.stream(range.split(" ")).map(String::trim).toArray(String[]::new);
            }
            for (String part : parts) {
                if (part.startsWith(">=") || part.startsWith("[")) {
                    versionStartIncluding = part.replace(">=", "").replace("[", "").trim();
                } else if (part.startsWith(">") || part.startsWith("(")) {
                    versionStartExcluding = part.replace(">", "").replace("(", "").trim();
                } else if (part.startsWith("<=") || part.endsWith("]")) {
                    versionEndIncluding = part.replace("<=", "").replace("]", "").trim();
                } else if (part.startsWith("<") || part.endsWith(")")) {
                    versionEndExcluding = part.replace("<", "").replace(")", "").trim();
                } else if (part.startsWith("=")) {
                    versionStartIncluding = part.replace("=", "").trim();
                    versionEndIncluding = part.replace("=", "").trim();
                } else {
                    LOGGER.warn("Unable to determine version range " + part);
                }
            }

            VulnerableSoftware vs = new VulnerableSoftware();
            vs.setVulnerable(true);
            vs.setPurlType(packageURL.getType());
            vs.setPurlNamespace(packageURL.getNamespace());
            vs.setPurlName(packageURL.getName());
            vs.setVersion(packageURL.getVersion());
            vs.setVersionStartIncluding(versionStartIncluding);
            vs.setVersionStartExcluding(versionStartExcluding);
            vs.setVersionEndIncluding(versionEndIncluding);
            vs.setVersionEndExcluding(versionEndExcluding);
            vulnerableSoftwares.add(vs);
        }
        return vulnerableSoftwares;
    }


    private class SnykAnalysisTaskUtil implements Runnable {
        private final List<Component> paginatedList;
        private String apiToken;

        protected SnykAnalysisTaskUtil(List<Component> paginatedList, String apiToken) {
            this.paginatedList = paginatedList;
            this.apiToken = apiToken;
        }

        public void run() {
            for (final Component component : this.paginatedList) {

                try {

                    //---------- cache logic start------------------------------
                    if (component.getPurl() != null) {
                        if (!isCacheCurrent(Source.SNYK, API_BASE_URL, component.getPurl().toString())) {
                            LOGGER.info("Cache is not current");
                            LOGGER.info("Inside run. Printing component info now: " + component.getPurl() + " " + component.getName() + " " + component.getProject());
                            final UnirestInstance ui = UnirestFactory.getUnirestInstance();
                            String API_ENDPOINT = "/issues?version=2022-09-28";
                            final String snykUrl = API_BASE_URL + parsePurlToSnykUrlParam(component.getPurl()) + API_ENDPOINT;
                            final GetRequest request = ui.get(snykUrl)
                                    .header(HttpHeaders.AUTHORIZATION, this.apiToken);
                            final HttpResponse<JsonNode> jsonResponse = request.asJson();
                            if (jsonResponse.getStatus() == 200) {
                                handle(component, jsonResponse.getBody().getObject());
                            } else {
                                handleUnexpectedHttpResponse(LOGGER, API_BASE_URL, jsonResponse.getStatus(), jsonResponse.getStatusText());
                            }
                        } else {
                            LOGGER.info("Cache is current, apply analysis from cache");
                            applyAnalysisFromCache(Source.SNYK, API_BASE_URL, component.getPurl().toString(), component, getAnalyzerIdentity());
                        }
                    }

                    //cache logic end------------------------------------------
                } catch (UnirestException e) {
                    handleRequestException(LOGGER, e);
                }
            }

        }
    }
}

