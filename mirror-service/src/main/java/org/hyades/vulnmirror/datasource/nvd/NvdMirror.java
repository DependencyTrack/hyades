package org.hyades.vulnmirror.datasource.nvd;

import io.github.jeremylong.openvulnerability.client.nvd.NvdCveClient;
import io.github.jeremylong.openvulnerability.client.nvd.DefCveItem;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.producer.Producer;
import org.cyclonedx.proto.v1_4.Bom;
import org.hyades.vulnmirror.datasource.AbstractDatasourceMirror;
import org.hyades.vulnmirror.datasource.Datasource;
import org.hyades.vulnmirror.state.MirrorStateStore;
import org.hyades.vulnmirror.state.VulnerabilityDigestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.time.Duration;
import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.hyades.proto.notification.v1.Level.LEVEL_ERROR;
import static org.hyades.proto.notification.v1.Level.LEVEL_INFORMATIONAL;

@ApplicationScoped
class NvdMirror extends AbstractDatasourceMirror<NvdMirrorState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdMirror.class);
    private static final String NOTIFICATION_TITLE = "NVD Mirroring";

    private final NvdApiClientFactory apiClientFactory;

    private final ExecutorService executorService;
    private final Timer durationTimer;

    private final Retry retry;


    NvdMirror(final NvdApiClientFactory apiClientFactory,
              @Named("nvdExecutorService") final ExecutorService executorService,
              final MirrorStateStore mirrorStateStore,
              final VulnerabilityDigestStore vulnDigestStore,
              final Producer<String, byte[]> kafkaProducer,
              @Named("nvdDurationTimer") final Timer durationTimer, @Named("nvdMirrorRetry") final Retry retry) {
        super(Datasource.NVD, mirrorStateStore, vulnDigestStore, kafkaProducer, NvdMirrorState.class);
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
        this.durationTimer = durationTimer;
        this.retry = retry;
    }

    @Override
    public Future<?> doMirror(String ecosystem) {
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
                final Collection<DefCveItem> cveItems = this.retry.executeCheckedSupplier(apiClient::next);
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