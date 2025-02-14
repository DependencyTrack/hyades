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
package org.dependencytrack.vulnmirror.datasource.csaf;

import io.github.csaf.sbom.retrieval.CsafLoader;
import io.github.csaf.sbom.retrieval.RetrievedProvider;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.producer.Producer;
import org.cyclonedx.proto.v1_6.Bom;
import org.dependencytrack.vulnmirror.datasource.AbstractDatasourceMirror;
import org.dependencytrack.vulnmirror.datasource.Datasource;
import org.dependencytrack.vulnmirror.state.MirrorStateStore;
import org.dependencytrack.vulnmirror.state.VulnerabilityDigestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;

@ApplicationScoped
public class CsafMirror extends AbstractDatasourceMirror<CsafMirrorState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsafMirror.class);
    private static final String NOTIFICATION_TITLE = "CSAF Mirroring";

    private final CsafConfig config;
    private final CsafLoader csafLoader;
    private final ExecutorService executorService;
    private final Timer durationTimer;

    CsafMirror(
            final CsafConfig config,
            @ForCsafMirror final ExecutorService executorService,
            final MirrorStateStore mirrorStateStore,
            final VulnerabilityDigestStore vulnDigestStore,
            final Producer<String, byte[]> kafkaProducer,
            @ForCsafMirror final CsafLoader csafLoader,
            @ForCsafMirror final Timer durationTimer) {
        super(Datasource.CSAF, mirrorStateStore, vulnDigestStore, kafkaProducer, CsafMirrorState.class);
        this.config = config;
        this.executorService = executorService;
        this.csafLoader = csafLoader;
        this.durationTimer = durationTimer;
    }

    @Override
    public boolean supportsDatasource(final Datasource datasource) {
        return datasource == Datasource.CSAF;
    }

    @Override
    public Future<?> doMirror() {
        if (!config.enabled().orElse(false)) {
            LOGGER.warn("Mirroring of the {} datasource was requested, but it is not enabled", Datasource.CSAF);
            return completedFuture(null);
        }

        return executorService.submit(() -> {
            try {
                mirrorInternal();
                dispatchNotification(LEVEL_INFORMATIONAL, NOTIFICATION_TITLE,
                        "Mirroring of configured CSAF sources completed successfully.");
            } catch (InterruptedException e) {
                LOGGER.warn("Thread was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                LOGGER.error("An unexpected error occurred mirroring configured CSAF sources", e);
                dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                        "An error occurred mirroring configured CSAF sources, cause being: " + e
                                + ". Check log for details.");
            }
        });
    }

    @Transactional
    void mirrorInternal() throws Throwable, Exception {
        long lastModified = getState()
                .map(CsafMirrorState::lastModifiedEpochSeconds)
                .orElse(0L);
        LOGGER.info("Mirroring CSAF-Vulnerabilities that were modified since {}", Instant.ofEpochSecond(lastModified));
        final Timer.Sample durationSample = Timer.start();
        // TODO retrieve configured documents
        // TODO filter by latest timestamp per doc?

        final var provider = RetrievedProvider.fromAsync("sick.com").get();
        final var documentStream = provider.streamDocuments();
        documentStream.forEach((document) -> {
            if (document.isSuccess()) {
                var csaf = document.getOrNull().getJson();
                var vulns = csaf.getVulnerabilities();
                for (int idx = 0; vulns != null && idx < vulns.size(); idx++) {
                    var vuln = vulns.get(idx);
                    LOGGER.info("Processing vulnerability {}", vuln.getTitle());

                    final Bom bov = CsafToCdxParser.parse(vuln, csaf.getDocument(), idx);
                    try {
                        publishIfChanged(bov);
                    } catch (ExecutionException e) {
                        LOGGER.error("Error while publishing document", e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } else {
                LOGGER.error("Error while processing document", document.exceptionOrNull());
            }
        });

        // lastUpdated is null when nothing changed
        // Optional.ofNullable(apiClient.getLastUpdated())
        // .map(ChronoZonedDateTime::toEpochSecond)
        // .ifPresent(epochSeconds -> updateState(new NvdMirrorState(epochSeconds)));
        // } finally {
        final long durationNanos = durationSample.stop(durationTimer);
        LOGGER.info("Mirroring of CSAF vulnerabilities completed in {}", Duration.ofNanos(durationNanos));
    }

    // TODO remove debug code
    public static void main(String[] args) {

        try {
            //var docstring = RetrievedDocument.Companion.fromJson(inputString);
            //var docstream = RetrievedDocument.Companion.fromJson(inputStream);
            

            final var loader = new CsafLoader();
            // final var provider = RetrievedProvider.fromAsync("wid.cert-bund.de").get();
            final var provider = RetrievedProvider.fromAsync("intevation.de").get();
            final var documentStream = provider.streamDocuments();
            

            documentStream.forEach((document) -> {

                if (document.isSuccess()) {
                    //var sourceUrl = document.getOrNull().getJson();
                    //System.out.println(sourceUrl);
                    var csaf = document.getOrNull().getJson();
                    csaf.getVulnerabilities().forEach((vuln) -> {
                        System.out.println(vuln);


                    });
                } else {
                    // System.out.println(document);
                }
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("hello");
    }

}
