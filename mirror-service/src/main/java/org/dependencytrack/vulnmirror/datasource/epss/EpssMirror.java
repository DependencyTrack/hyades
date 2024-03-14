package org.dependencytrack.vulnmirror.datasource.epss;

import io.github.jeremylong.openvulnerability.client.epss.EpssDataFeed;
import io.github.jeremylong.openvulnerability.client.epss.EpssItem;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.producer.Producer;
import org.dependencytrack.vulnmirror.datasource.AbstractDatasourceMirror;
import org.dependencytrack.vulnmirror.datasource.Datasource;
import org.dependencytrack.vulnmirror.state.MirrorStateStore;
import org.dependencytrack.vulnmirror.state.VulnerabilityDigestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;

@ApplicationScoped
class EpssMirror extends AbstractDatasourceMirror<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpssMirror.class);
    private static final String NOTIFICATION_TITLE = "EPSS Mirroring";
    private final ExecutorService executorService;
    final EpssClientFactory epssClientFactory;
    private final Timer durationTimer;

    EpssMirror(@ForEpssMirror final ExecutorService executorService,
               final MirrorStateStore mirrorStateStore,
               final VulnerabilityDigestStore vulnDigestStore,
               final Producer<String, byte[]> kafkaProducer,
               @ForEpssMirror final Timer durationTimer,
               final EpssClientFactory epssClientFactory) {
        super(Datasource.EPSS, mirrorStateStore, vulnDigestStore, kafkaProducer, Void.class);
        this.executorService = executorService;
        this.durationTimer = durationTimer;
        this.epssClientFactory = epssClientFactory;
    }

    @Override
    public boolean supportsDatasource(final Datasource datasource) {
        return datasource == Datasource.EPSS;
    }

    @Override
    public Future<?> doMirror(String ecosystem) {
        return executorService.submit(() -> {
            try {
                performMirror();
                dispatchNotification(LEVEL_INFORMATIONAL, NOTIFICATION_TITLE,
                        "Mirroring of the Exploit Prediction Scoring System (EPSS) completed successfully.");
            } catch (InterruptedException e) {
                LOGGER.warn("Thread was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                LOGGER.error("An unexpected error occurred mirroring the contents of the Exploit Prediction Scoring System (EPSS)", e);
                dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                        "An error occurred mirroring the contents of the Exploit Prediction Scoring System (EPSS), cause being: " + e + ". Check log for details.");
            }
        });
    }

    void performMirror() throws Exception {
        final Timer.Sample durationSample = Timer.start();
        try {
            final EpssDataFeed epssClient = epssClientFactory.create(null);
            List<EpssItem> epssItems = epssClient.download();
            publishEpss(epssItems);
        } finally {
            final long durationNanos = durationSample.stop(durationTimer);
            LOGGER.info("Mirroring of EPSS data completed in {}", Duration.ofNanos(durationNanos));
        }
    }
}