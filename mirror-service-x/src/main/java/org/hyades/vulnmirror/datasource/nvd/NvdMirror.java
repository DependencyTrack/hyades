package org.hyades.vulnmirror.datasource.nvd;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
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
import java.util.concurrent.ExecutorService;

@ApplicationScoped
class NvdMirror extends AbstractDatasourceMirror<NvdMirrorState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdMirror.class);

    private final NvdApiClientFactory apiClientFactory;
    private final ExecutorService executorService;

    NvdMirror(final NvdApiClientFactory apiClientFactory,
              @Named("nvdExecutorService") final ExecutorService executorService,
              final MirrorStateStore mirrorStateStore,
              final VulnerabilityDigestStore vulnDigestStore,
              final Producer<String, byte[]> bovProducer) {
        super(Datasource.NVD, mirrorStateStore, vulnDigestStore, bovProducer, NvdMirrorState.class);
        this.apiClientFactory = apiClientFactory;
        this.executorService = executorService;
    }

    @Override
    public void doMirror() {
        executorService.submit(this::mirrorInternal);
    }

    private void mirrorInternal() {
        LOGGER.info("Starting NVD mirroring");
        long lastModified = getState()
                .map(NvdMirrorState::lastModifiedEpochSeconds)
                .orElse(0L);

        try (final NvdCveApi apiClient = apiClientFactory.createApiClient(lastModified)) {
            while (apiClient.hasNext()) {
                for (final DefCveItem cveItem : apiClient.next()) {
                    final Bom bov = NvdToCyclonedxParser.parse(cveItem.getCve());
                    publishIfChanged(bov);
                }
            }
            updateState(new NvdMirrorState(apiClient.getLastModifiedRequest()));
            LOGGER.info("NVD mirroring completed");
        } catch (Exception e) {
            LOGGER.error("NVD mirroring failed", e);
        }
    }

}
