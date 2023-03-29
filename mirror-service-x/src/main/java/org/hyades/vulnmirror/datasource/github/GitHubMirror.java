package org.hyades.vulnmirror.datasource.github;

import io.github.jeremylong.ghsa.GitHubSecurityAdvisoryClient;
import io.github.jeremylong.ghsa.SecurityAdvisory;
import org.apache.kafka.clients.producer.Producer;
import org.cyclonedx.proto.v1_4.Bom;
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

@ApplicationScoped
class GitHubMirror extends AbstractDatasourceMirror<GitHubMirrorState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubMirror.class);

    final GitHubApiClientFactory apiClientFactory;
    private final ExecutorService executorService;

    GitHubMirror(final GitHubApiClientFactory apiClientFactory,
                 @Named("githubExecutorService") final ExecutorService executorService,
                 final MirrorStateStore mirrorStateStore,
                 final VulnerabilityDigestStore vulnDigestStore,
                 final Producer<String, byte[]> bovProducer) {
        super(Datasource.GITHUB, mirrorStateStore, vulnDigestStore, bovProducer, GitHubMirrorState.class);
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
    }

    @Override
    public boolean supportsDatasource(final Datasource datasource) {
        return datasource == Datasource.GITHUB;
    }

    @Override
    public void doMirror() {
        executorService.submit(this::mirrorInternal);
    }

    private void mirrorInternal() {
        LOGGER.info("Starting GitHub mirroring");
        final long lastModified = getState()
                .map(GitHubMirrorState::lastUpdatedEpochSeconds)
                .orElse(0L);

        try (final GitHubSecurityAdvisoryClient apiClient = apiClientFactory.create(lastModified)) {
            while (apiClient.hasNext()) {
                for (final SecurityAdvisory advisory : apiClient.next()) {
                    Bom bov =  GithubToCyclonedxParser.parse(advisory);
                    publishIfChanged(bov);
                }
            }

            // lastUpdated is null when nothing changed
            Optional.ofNullable(apiClient.getLastUpdated())
                    .map(ChronoZonedDateTime::toEpochSecond)
                    .ifPresent(epochSeconds -> updateState(new GitHubMirrorState(epochSeconds)));
            LOGGER.info("GitHub mirroring completed");
        } catch (Exception e) {
            LOGGER.error("GitHub mirroring failed", e);
        }
    }

}
