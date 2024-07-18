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
package org.dependencytrack.vulnmirror.datasource.nvd;

import io.github.jeremylong.openvulnerability.client.nvd.DefCveItem;
import io.github.jeremylong.openvulnerability.client.nvd.NvdCveClient;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
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
import java.time.chrono.ChronoZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;

@ApplicationScoped
class NvdMirror extends AbstractDatasourceMirror<NvdMirrorState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdMirror.class);
    private static final String NOTIFICATION_TITLE = "NVD Mirroring";

    private final NvdConfig config;
    private final NvdApiClientFactory apiClientFactory;
    private final ExecutorService executorService;
    private final Timer durationTimer;

    NvdMirror(final NvdConfig config,
              final NvdApiClientFactory apiClientFactory,
              @ForNvdMirror final ExecutorService executorService,
              final MirrorStateStore mirrorStateStore,
              final VulnerabilityDigestStore vulnDigestStore,
              final Producer<String, byte[]> kafkaProducer,
              @ForNvdMirror final Timer durationTimer) {
        super(Datasource.NVD, mirrorStateStore, vulnDigestStore, kafkaProducer, NvdMirrorState.class);
        this.config = config;
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
        this.durationTimer = durationTimer;
    }

    @Override
    public Future<?> doMirror() {
        if (!config.enabled().orElse(false)) {
            LOGGER.warn("Mirroring of the {} datasource was requested, but it is not enabled", Datasource.NVD);
            return completedFuture(null);
        }

        return executorService.submit(() -> {
            try {
                mirrorInternal();
                dispatchNotification(LEVEL_INFORMATIONAL, NOTIFICATION_TITLE,
                        "Mirroring of the National Vulnerability Database completed successfully.");
            } catch (InterruptedException e) {
                LOGGER.warn("Thread was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                LOGGER.error("An unexpected error occurred mirroring the contents of the National Vulnerability Database", e);
                dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                        "An error occurred mirroring the contents of the National Vulnerability Database, cause being: " + e + ". Check log for details.");
            }
        });
    }

    void mirrorInternal() throws Throwable {
        long lastModified = getState()
                .map(NvdMirrorState::lastModifiedEpochSeconds)
                .orElse(0L);

        LOGGER.info("Mirroring CVEs that were modified since {}", Instant.ofEpochSecond(lastModified));
        final Timer.Sample durationSample = Timer.start();

        try (final NvdCveClient apiClient = apiClientFactory.createApiClient(lastModified)) {
            while (apiClient.hasNext()) {
                final Collection<DefCveItem> cveItems = apiClient.next();
                if (cveItems == null) {
                    LOGGER.warn("No cve item in response from Nvd. Skipping to next item");
                    continue;
                }
                for (final DefCveItem cveItem : cveItems) {
                    final Bom bov = NvdToCyclonedxParser.parse(cveItem);
                    publishIfChanged(bov);
                }
            }

            // lastUpdated is null when nothing changed
            Optional.ofNullable(apiClient.getLastUpdated())
                    .map(ChronoZonedDateTime::toEpochSecond)
                    .ifPresent(epochSeconds -> updateState(new NvdMirrorState(epochSeconds)));
        } finally {
            final long durationNanos = durationSample.stop(durationTimer);
            LOGGER.info("Mirroring of CVEs completed in {}", Duration.ofNanos(durationNanos));
        }
    }

}