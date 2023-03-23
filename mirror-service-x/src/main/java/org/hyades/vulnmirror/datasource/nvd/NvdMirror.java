package org.hyades.vulnmirror.datasource.nvd;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import org.apache.commons.codec.digest.DigestUtils;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Source;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
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
    private final Emitter<byte[]> bovEmitter;

    NvdMirror(final NvdApiClientFactory apiClientFactory,
              @Named("nvdExecutorService") final ExecutorService executorService,
              final VulnerabilityDigestStore vulnDigestStore,
              final MirrorStateStore stateStore,
              @Channel("vulnerabilities") final Emitter<byte[]> bovEmitter) {
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
        this.vulnDigestStore = vulnDigestStore;
        this.stateStore = stateStore;
        this.bovEmitter = bovEmitter;
    }

    public void doMirror() {
        executorService.submit(this::mirrorInternal);
    }

    @Override
    public boolean supportsDatasource(final Datasource datasource) {
        return datasource == Datasource.NVD;
    }

    private void mirrorInternal() {
        final NvdMirrorState state = stateStore.get(Datasource.NVD, NvdMirrorState.class);
        final long lastModified = Optional.ofNullable(state)
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
                        bovEmitter.send(KafkaRecord.of(eventId, serializedBov));
                    } else {
                        LOGGER.debug("{} did not change", eventId);
                    }
                }
            }

            stateStore.putAndWait(Datasource.NVD, new NvdMirrorState(apiClient.getLastModifiedRequest()));
            LOGGER.info("Completed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
