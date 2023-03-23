package org.hyades.vulnmirror.datasource.github;

import io.github.jeremylong.ghsa.GitHubSecurityAdvisoryClient;
import io.github.jeremylong.ghsa.SecurityAdvisory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Source;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.hyades.common.KafkaTopic;
import org.hyades.vulnmirror.datasource.Datasource;
import org.hyades.vulnmirror.datasource.DatasourceMirror;
import org.hyades.vulnmirror.state.MirrorStateStore;
import org.hyades.vulnmirror.state.VulnerabilityDigestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
class GitHubMirror implements DatasourceMirror {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubMirror.class);

    final GitHubApiClientFactory apiClientFactory;
    private final ExecutorService executorService;
    private final VulnerabilityDigestStore vulnDigestStore;
    private final MirrorStateStore stateStore;
    private final Producer<String, byte[]> bovProducer;

    GitHubMirror(final GitHubApiClientFactory apiClientFactory,
                 @Named("githubExecutorService") final ExecutorService executorService,
                 final VulnerabilityDigestStore vulnDigestStore,
                 final MirrorStateStore stateStore,
                 final Producer<String, byte[]> bovProducer) {
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
        this.vulnDigestStore = vulnDigestStore;
        this.stateStore = stateStore;
        this.bovProducer = bovProducer;
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
        final GitHubMirrorState state = stateStore.get(Datasource.GITHUB, GitHubMirrorState.class);
        final long lastModified = Optional.ofNullable(state)
                .map(GitHubMirrorState::lastUpdatedEpochSeconds)
                .orElse(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1).toEpochSecond());

        try (final GitHubSecurityAdvisoryClient apiClient = apiClientFactory.create(lastModified)) {
            if (apiClient.hasNext()) {
                for (final SecurityAdvisory advisory : apiClient.next()) {
                    final Bom bov = Bom.newBuilder()
                            .addVulnerabilities(Vulnerability.newBuilder()
                                    .setId(advisory.getGhsaId())
                                    .setSource(Source.newBuilder().setName(Datasource.GITHUB.name())))
                            .build();

                    final String eventId = "%s/%s".formatted(Datasource.GITHUB, advisory.getGhsaId());
                    final byte[] serializedBov = bov.toByteArray();
                    final byte[] bovDigest = DigestUtils.getSha256Digest().digest(serializedBov);
                    if (!Arrays.equals(vulnDigestStore.get(Datasource.GITHUB, advisory.getGhsaId()), bovDigest)) {
                        LOGGER.info("{} has changed", eventId);
                        bovProducer.send(new ProducerRecord<>(KafkaTopic.NEW_VULNERABILITY.getName(), eventId, serializedBov));
                    } else {
                        LOGGER.info("{} did not change", eventId);
                    }
                }
            }

            stateStore.putAndWait(Datasource.GITHUB, new GitHubMirrorState(apiClient.getLastUpdated().toEpochSecond()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
