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
import alpine.notification.Notification;
import alpine.notification.NotificationLevel;
import org.acme.consumer.CacheReader;
import org.acme.consumer.VulnCacheReader;
import org.acme.model.CacheKey;
import org.acme.model.Component;
import org.acme.model.ComponentAnalysisCache;
import org.acme.model.Vulnerability;
import org.acme.model.VulnerablityResult;
import org.acme.notification.NotificationConstants;
import org.acme.notification.NotificationGroup;
import org.acme.notification.NotificationScope;
import org.acme.producer.CacheProducer;
import org.acme.producer.VulnCacheProducer;
import org.acme.producer.VulnerabilityResultProducer;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

/**
 * A base class that has logic common or useful to all classes that extend it.
 *
 * @author Steve Springett
 * @since 3.0.0
 */

public abstract class BaseComponentAnalyzerTask implements ScanTask {
    private static final Logger LOGGER = Logger.getLogger(OssIndexAnalysisTask.class);
    @Inject
    CacheProducer producer;
    @Inject
    CacheReader cacheReader;
    @Inject
    VulnerabilityResultProducer vulnerabilityResultProducer;

    @Inject
    VulnCacheProducer vulnCacheProducer;


    @Inject
    VulnCacheReader vulnCacheReader;
    long cacheValidityPeriod;

    protected void handleUnexpectedHttpResponse(final Logger logger, String url, final int statusCode, final String statusText) {
        logger.error("HTTP Status : " + statusCode + " " + statusText);
        logger.error(" - Analyzer URL : " + url);
        Notification.dispatch(new Notification()
                .scope(NotificationScope.SYSTEM)
                .group(NotificationGroup.ANALYZER)
                .title(NotificationConstants.Title.ANALYZER_ERROR)
                .content("An error occurred while communicating with a vulnerability intelligence source. URL: " + url + " HTTP Status: " + statusCode + ". Check log for details.")
                .level(NotificationLevel.ERROR)
        );
    }

    protected void handleRequestException(final Logger logger, final Exception e) {
        logger.error("Request failure", e);
        Notification.dispatch(new Notification()
                .scope(NotificationScope.SYSTEM)
                .group(NotificationGroup.ANALYZER)
                .title(NotificationConstants.Title.ANALYZER_ERROR)
                .content("An error occurred while communicating with a vulnerability intelligence source. Check log for details. " + e.getMessage())
                .level(NotificationLevel.ERROR)
        );
    }

    protected boolean isCacheCurrent(Vulnerability.Source source, String targetHost, String target) {
        boolean isCacheCurrent = false;
        CacheKey key = new CacheKey();
        key.setAnalyzerType(source.name());
        key.setComponentPurl(target);
        ComponentAnalysisCache cac = cacheReader.getComponentCache(key);
        if (cac != null) {
            final Date now = new Date();
            if (now.getTime() > cac.getLastOccurrence().getTime()) {
                final long delta = now.getTime() - cac.getLastOccurrence().getTime();
                isCacheCurrent = delta <= cacheValidityPeriod;
            }
        }
        if (isCacheCurrent) {
            LOGGER.debug("Cache is current. Skipping analysis. (source: " + source + " / targetHost: " + targetHost + " / target: " + target);
        } else {
            LOGGER.debug("Cache is not current. Analysis should be performed (source: " + source + " / targetHost: " + targetHost + " / target: " + target);
        }
        return isCacheCurrent;

    }

    public JsonObject getJsonResult(String result) {
        if (result != null) {
            try (final StringReader sr = new StringReader(result);
                 final JsonReader jr = Json.createReader(sr)) {
                return jr.readObject();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    protected void applyAnalysisFromCache(Vulnerability.Source source, String targetHost, String target, Component component, AnalyzerIdentity analyzerIdentity) {
        CacheKey key = new CacheKey();
        key.setAnalyzerType(source.name());
        key.setComponentPurl(target);
        ComponentAnalysisCache cac = cacheReader.getComponentCache(key);
        if (cac != null) {
            final JsonObject jsonResult = getJsonResult(cac.getResult());

            if (jsonResult != null) {
                final JsonArray vulns = jsonResult.getJsonArray("vulnIds");
                if (vulns != null) {
                    if (vulns.isEmpty()) {
                        final var vulnerablityResult = new VulnerablityResult();
                        vulnerablityResult.setIdentity(analyzerIdentity);
                        vulnerablityResult.setVulnerability(null);
                        vulnerabilityResultProducer.sendVulnResultToDT(component.getUuid(), vulnerablityResult);
                    } else {
                        for (JsonNumber vulnId : vulns.getValuesAs(JsonNumber.class)) {
                            final Vulnerability vulnerability = vulnCacheReader.getVulnCache(vulnId.longValue());
                            /*final Component c = qm.getObjectByUuid(Component.class, component.getUuid());
                            if (c == null) continue;*/
                            if (vulnerability != null) {
                                //NotificationUtil.analyzeNotificationCriteria(qm, vulnerability, component);
                                final var vulnerablityResult = new VulnerablityResult();
                                vulnerablityResult.setIdentity(analyzerIdentity);
                                vulnerablityResult.setVulnerability(vulnerability);
                                vulnerabilityResultProducer.sendVulnResultToDT(component.getUuid(), vulnerablityResult);

                            }
                        }
                    }
                }
            }
        }
    }

    protected JsonObject addVulnerabilityToCache(final JsonObject result, final long vulnId) {
        if (result == null) {
            final JsonArray vulns = Json.createArrayBuilder().add(vulnId).build();
            return Json.createObjectBuilder().add("vulnIds", vulns).build();
        } else {
            final JsonArrayBuilder vulnsBuilder = Json.createArrayBuilder(result.getJsonArray("vulnIds"));
            final JsonArray vulns = vulnsBuilder.add(Json.createValue(vulnId)).build();
            return Json.createObjectBuilder(result).add("vulnIds", vulns).build();
        }
    }

    public synchronized void updateComponentAnalysisCache(ComponentAnalysisCache.CacheType cacheType, String targetHost, String targetType, String target, Date lastOccurrence, JsonObject result) {
        CacheKey key = new CacheKey();
        key.setAnalyzerType(targetType);
        key.setComponentPurl(target);
        ComponentAnalysisCache cac = cacheReader.getComponentCache(key);
        if (cac == null) {
            cac = new ComponentAnalysisCache();
            cac.setCacheType(cacheType);
            cac.setTargetHost(targetHost);
            cac.setTargetType(targetType);
            cac.setTarget(target);
        }
        cac.setLastOccurrence(lastOccurrence);
        String jsonObject = "";
        if (result != null) {
            try (final StringWriter sw = new StringWriter();
                 final JsonWriter jw = Json.createWriter(sw)) {
                jw.write(result);
                jsonObject = sw.toString();
            } catch (Exception e) {
                result = null;
            }
            cac.setResult(jsonObject);
        }

        LOGGER.info("Sending Data Index analysis complete");

        producer.sendVulnCacheToKafka(key, cac);

    }
}
