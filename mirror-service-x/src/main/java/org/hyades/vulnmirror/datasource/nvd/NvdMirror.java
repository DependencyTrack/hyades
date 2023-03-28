package org.hyades.vulnmirror.datasource.nvd;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.producer.Producer;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Source;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.hyades.vulnmirror.datasource.AbstractDatasourceMirror;
import org.hyades.vulnmirror.datasource.Datasource;
import org.hyades.vulnmirror.state.MirrorStateStore;
import org.hyades.vulnmirror.state.VulnerabilityDigestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
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

    NvdMirror(final NvdApiClientFactory apiClientFactory,
              @Named("nvdExecutorService") final ExecutorService executorService,
              final MirrorStateStore mirrorStateStore,
              final VulnerabilityDigestStore vulnDigestStore,
              final Producer<String, byte[]> kafkaProducer,
              @Named("nvdDurationTimer") final Timer durationTimer) {
        super(Datasource.NVD, mirrorStateStore, vulnDigestStore, kafkaProducer, NvdMirrorState.class);
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
        this.durationTimer = durationTimer;
    }

    @Override
    public Future<?> doMirror() {
        return executorService.submit(() -> {
            try {
                mirrorInternal();
                dispatchNotification(LEVEL_INFORMATIONAL, NOTIFICATION_TITLE,
                        "Mirroring of the National Vulnerability Database completed successfully.");
            } catch (Exception e) {
                LOGGER.error("An unexpected error occurred mirroring the contents of the National Vulnerability Database", e);
                dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                        "An error occurred mirroring the contents of the National Vulnerability Database. Check log for details.");
            }
        });
    }

    void mirrorInternal() throws Exception {
        long lastModified = getState()
                .map(NvdMirrorState::lastModifiedEpochSeconds)
                .orElse(0L);

        LOGGER.info("Mirroring CVEs that were modified since {}", Instant.ofEpochSecond(lastModified));
        final Timer.Sample durationSample = Timer.start();

        try (final NvdCveApi apiClient = apiClientFactory.createApiClient(lastModified)) {
            while (apiClient.hasNext()) {
                final Collection<DefCveItem> cveItems = apiClient.next();
                if (cveItems == null) {
                    LOGGER.warn("foo");
                    continue;
                }

                for (final DefCveItem cveItem : cveItems) {
                    final Bom bov = Bom.newBuilder()
                            .addVulnerabilities(Vulnerability.newBuilder()
                                    .setId(cveItem.getCve().getId())
                                    .setSource(Source.newBuilder().setName(Datasource.NVD.name())))
                            .build();

                    publishIfChanged(bov);
                }
            }

            updateState(new NvdMirrorState(apiClient.getLastModifiedRequest()));
        } finally {
            final long durationNanos = durationSample.stop(durationTimer);
            LOGGER.info("Mirroring of CVEs completed in {}", Duration.ofNanos(durationNanos));
        }
    }

}
