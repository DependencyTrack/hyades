package org.dependencytrack.vulnmirror.datasource.github;

import io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClient;
import io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisory;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.kafka.clients.producer.Producer;
import org.cyclonedx.proto.v1_4.Bom;
import org.dependencytrack.vulnmirror.datasource.AbstractDatasourceMirror;
import org.dependencytrack.vulnmirror.datasource.Datasource;
import org.dependencytrack.vulnmirror.state.MirrorStateStore;
import org.dependencytrack.vulnmirror.state.VulnerabilityDigestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;

@ApplicationScoped
class GitHubMirror extends AbstractDatasourceMirror<GitHubMirrorState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubMirror.class);
    private static final String NOTIFICATION_TITLE = "GitHub Advisory Mirroring";

    final GitHubApiClientFactory apiClientFactory;
    private final ExecutorService executorService;
    private final Timer durationTimer;
    private final GitHubConfig config;

    GitHubMirror(final GitHubApiClientFactory apiClientFactory,
                 @Named("githubExecutorService") final ExecutorService executorService,
                 final MirrorStateStore mirrorStateStore,
                 final VulnerabilityDigestStore vulnDigestStore,
                 final Producer<String, byte[]> kafkaProducer,
                 @Named("githubDurationTimer") final Timer durationTimer, GitHubConfig config) {
        super(Datasource.GITHUB, mirrorStateStore, vulnDigestStore, kafkaProducer, GitHubMirrorState.class);
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
        this.durationTimer = durationTimer;
        this.config = config;
    }

    @Override
    public boolean supportsDatasource(final Datasource datasource) {
        return datasource == Datasource.GITHUB;
    }

    @Override
    public Future<?> doMirror(String ecosystem) {
        return executorService.submit(() -> {
            try {
                mirrorInternal();
                dispatchNotification(LEVEL_INFORMATIONAL, NOTIFICATION_TITLE,
                        "Mirroring of GitHub Advisories completed successfully.");
            } catch (InterruptedException e) {
                LOGGER.warn("Thread was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOGGER.error("An unexpected error occurred mirroring the contents of GitHub Advisories", e);
                dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                        "An error occurred mirroring the contents of GitHub Advisories. Check log for details.");
            }
        });
    }

    void mirrorInternal() throws Exception {
        final long lastModified = getState()
                .map(GitHubMirrorState::lastUpdatedEpochSeconds)
                .orElse(0L);

        LOGGER.info("Mirroring GitHub Advisories that were modified since {}", Instant.ofEpochSecond(lastModified));
        final Timer.Sample durationSample = Timer.start();

        try (final GitHubSecurityAdvisoryClient apiClient = apiClientFactory.create(lastModified)) {
            while (apiClient.hasNext()) {
                for (final SecurityAdvisory advisory : apiClient.next()) {
                    Bom bov =  GitHubAdvisoryToCdxParser.parse(advisory, this.config.aliasSyncEnabled());
                    publishIfChanged(bov);
                }
            }

            // lastUpdated is null when nothing changed
            Optional.ofNullable(apiClient.getLastUpdated())
                    .map(ChronoZonedDateTime::toEpochSecond)
                    .ifPresent(epochSeconds -> updateState(new GitHubMirrorState(epochSeconds)));
        } finally {
            final long durationNanos = durationSample.stop(durationTimer);
            LOGGER.info("Mirroring of GitHub Advisories completed in {}", Duration.ofNanos(durationNanos));
        }
    }

}
