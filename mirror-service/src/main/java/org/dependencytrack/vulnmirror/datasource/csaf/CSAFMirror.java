package org.dependencytrack.vulnmirror.datasource.csaf;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.dependencytrack.vulnmirror.datasource.AbstractDatasourceMirror;
import org.dependencytrack.vulnmirror.datasource.Datasource;
import org.dependencytrack.vulnmirror.state.MirrorStateStore;
import org.dependencytrack.vulnmirror.state.VulnerabilityDigestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class CSAFMirror extends AbstractDatasourceMirror<CSAFMirrorState> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSAFMirror.class);
    private static final String NOTIFICATION_TITLE = "CSAF Mirroring";

    private final CSAFConfig config;
    private final ExecutorService executorService;

    CSAFMirror(
        final CSAFConfig config,
        @ForCsafMirror final ExecutorService executorService,
        final MirrorStateStore mirrorStateStore,
        final VulnerabilityDigestStore vulnDigestStore,
        final Producer<String, byte[]> kafkaProducer
    ) {
        super(Datasource.CSAF, mirrorStateStore, vulnDigestStore, kafkaProducer, CSAFMirrorState.class);
        this.config = config;
        this.executorService = executorService;

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
                        "An error occurred mirroring configured CSAF sources, cause being: " + e + ". Check log for details.");
            }
        });
    }

    void mirrorInternal() throws Throwable {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mirrorInternal'");
    }
        
}
