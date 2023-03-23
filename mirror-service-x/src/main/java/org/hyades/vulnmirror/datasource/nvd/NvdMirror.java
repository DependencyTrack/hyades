package org.hyades.vulnmirror.datasource.nvd;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
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
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
class NvdMirror implements DatasourceMirror {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdMirror.class);

    private final NvdApiClientFactory apiClientFactory;
    private final ExecutorService executorService;
    private final VulnerabilityDigestStore vulnDigestStore;
    private final MirrorStateStore stateStore;
    private final Producer<String, byte[]> bovProducer;

    NvdMirror(final NvdApiClientFactory apiClientFactory,
              @Named("nvdExecutorService") final ExecutorService executorService,
              final VulnerabilityDigestStore vulnDigestStore,
              final MirrorStateStore stateStore,
              final Producer<String, byte[]> bovProducer) {
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
        this.vulnDigestStore = vulnDigestStore;
        this.stateStore = stateStore;
        this.bovProducer = bovProducer;
    }

    public void doMirror() {
        executorService.submit(this::mirrorInternal);
    }

    @Override
    public boolean supportsDatasource(final Datasource datasource) {
        return datasource == Datasource.NVD;
    }

    private void mirrorInternal() {
        LOGGER.info("Starting NVD mirroring");
        final NvdMirrorState state = stateStore.get(Datasource.NVD, NvdMirrorState.class);
        long lastModified = Optional.ofNullable(state)
                .map(NvdMirrorState::lastModifiedEpochSeconds)
                .orElse(0L);

        try (final NvdCveApi apiClient = apiClientFactory.createApiClient(lastModified)) {
            while (apiClient.hasNext()) {
                for (final DefCveItem cveItem : apiClient.next()) {
                    final Bom bov = Bom.newBuilder()
                            .addVulnerabilities(Vulnerability.newBuilder()
                                    .setId(cveItem.getCve().getId())
                                    .setSource(Source.newBuilder().setName(Datasource.NVD.name())))
                            .build();

                    final String eventId = "%s/%s".formatted(Datasource.NVD, cveItem.getCve().getId());
                    final byte[] serializedBov = bov.toByteArray();
                    final byte[] bovDigest = DigestUtils.getSha256Digest().digest(serializedBov);
                    if (!Arrays.equals(vulnDigestStore.get(Datasource.NVD, cveItem.getCve().getId()), bovDigest)) {
                        LOGGER.debug("{} has changed", eventId);
                        bovProducer.send(new ProducerRecord<>(KafkaTopic.NEW_VULNERABILITY.getName(), eventId, serializedBov)).get();
                    } else {
                        LOGGER.debug("{} did not change", eventId);
                    }
                }
            }

            stateStore.putAndWait(Datasource.NVD, new NvdMirrorState(apiClient.getLastModifiedRequest()));
            LOGGER.info("NVD mirroring completed");
        } catch (Exception e) {
            LOGGER.error("NVD mirroring failed", e);
        }
    }

}
