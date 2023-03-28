package org.hyades.vulnmirror.datasource.github;

import io.github.jeremylong.ghsa.GitHubSecurityAdvisoryClient;
import io.github.jeremylong.ghsa.SecurityAdvisory;
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
import java.time.chrono.ChronoZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.hyades.proto.notification.v1.Level.LEVEL_ERROR;
import static org.hyades.proto.notification.v1.Level.LEVEL_INFORMATIONAL;

@ApplicationScoped
class GitHubMirror extends AbstractDatasourceMirror<GitHubMirrorState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubMirror.class);
    private static final String NOTIFICATION_TITLE = "GitHub Advisory Mirroring";

    final GitHubApiClientFactory apiClientFactory;
    private final ExecutorService executorService;

    GitHubMirror(final GitHubApiClientFactory apiClientFactory,
                 @Named("githubExecutorService") final ExecutorService executorService,
                 final MirrorStateStore mirrorStateStore,
                 final VulnerabilityDigestStore vulnDigestStore,
                 final Producer<String, byte[]> kafkaProducer) {
        super(Datasource.GITHUB, mirrorStateStore, vulnDigestStore, kafkaProducer, GitHubMirrorState.class);
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
    }

    @Override
    public boolean supportsDatasource(final Datasource datasource) {
        return datasource == Datasource.GITHUB;
    }

    @Override
    public Future<?> doMirror() {
        return executorService.submit(() -> {
            try {
                mirrorInternal();
                dispatchNotification(LEVEL_INFORMATIONAL, NOTIFICATION_TITLE,
                        "Mirroring of GitHub Advisories completed successfully.");
            } catch (Exception e) {
                LOGGER.error("An unexpected error occurred mirroring the contents of GitHub Advisories", e);
                dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                        "An error occurred mirroring the contents of GitHub Advisories. Check log for details.");
            }
        });
    }

    void mirrorInternal() throws Exception {
        LOGGER.info("Starting GitHub mirroring");
        final long lastModified = getState()
                .map(GitHubMirrorState::lastUpdatedEpochSeconds)
                .orElse(0L);

        try (final GitHubSecurityAdvisoryClient apiClient = apiClientFactory.create(lastModified)) {
            while (apiClient.hasNext()) {
                for (final SecurityAdvisory advisory : apiClient.next()) {
                    final Bom bov = Bom.newBuilder()
                            .addVulnerabilities(Vulnerability.newBuilder()
                                    .setId(advisory.getGhsaId())
                                    .setSource(Source.newBuilder().setName(Datasource.GITHUB.name())))
                            .build();

                    publishIfChanged(bov);
                }
            }

            // lastUpdated is null when nothing changed
            Optional.ofNullable(apiClient.getLastUpdated())
                    .map(ChronoZonedDateTime::toEpochSecond)
                    .ifPresent(epochSeconds -> updateState(new GitHubMirrorState(epochSeconds)));
            LOGGER.info("GitHub mirroring completed");
        }
    }

}