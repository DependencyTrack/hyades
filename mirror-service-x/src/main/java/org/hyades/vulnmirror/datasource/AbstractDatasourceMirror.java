package org.hyades.vulnmirror.datasource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.cyclonedx.proto.v1_4.Bom;
import org.hyades.common.KafkaTopic;
import org.hyades.vulnmirror.state.MirrorStateStore;
import org.hyades.vulnmirror.state.VulnerabilityDigestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public abstract class AbstractDatasourceMirror<T> implements DatasourceMirror {

    private Datasource datasource;
    public MirrorStateStore mirrorStateStore;
    private VulnerabilityDigestStore vulnDigestStore;
    private Producer<String, byte[]> bovProducer;
    private Class<T> stateClass;
    private Logger logger;

    /**
     * Non-private no-args constructor required by Quarkus.
     * <p>
     * DO NOT USE, use {@link #AbstractDatasourceMirror(Datasource, MirrorStateStore, VulnerabilityDigestStore, Producer, Class)} instead.
     *
     * @see <a href="https://github.com/quarkusio/quarkus/issues/22669">Quarkus Issue #22669</a>
     */
    @SuppressWarnings("unused")
    protected AbstractDatasourceMirror() {
    }

    protected AbstractDatasourceMirror(final Datasource datasource,
                                       final MirrorStateStore mirrorStateStore,
                                       final VulnerabilityDigestStore vulnDigestStore,
                                       final Producer<String, byte[]> bovProducer,
                                       final Class<T> stateClass) {
        this.datasource = datasource;
        this.mirrorStateStore = mirrorStateStore;
        this.vulnDigestStore = vulnDigestStore;
        this.bovProducer = bovProducer;
        this.stateClass = stateClass;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public boolean supportsDatasource(final Datasource datasource) {
        return this.datasource == datasource;
    }

    protected Optional<T> getState() {
        return Optional.ofNullable(mirrorStateStore.get(datasource, stateClass));
    }

    protected void updateState(final T state) {
        mirrorStateStore.putAndWait(datasource, state);
    }

    /**
     * Publish a Bill of Vulnerability encoded vulnerability to Kafka, if it changed since
     * the last time it was published.
     *
     * @param bov The Bill of Vulnerabilities to publish
     * @throws ExecutionException   When waiting for the broker acknowledgment failed
     * @throws InterruptedException When waiting for the broker acknowledgment failed
     */
    protected void publishIfChanged(final Bom bov) throws ExecutionException, InterruptedException {
        if (bov.getVulnerabilitiesCount() != 1) {
            throw new IllegalArgumentException("BOV must contain exactly one vulnerability");
        }

        final String vulnId = bov.getVulnerabilitiesList().get(0).getId();
        if (StringUtils.trimToNull(vulnId) == null) {
            throw new IllegalArgumentException("Vulnerability must have an ID");
        }

        // TODO: Maybe perform some more validation here?

        final String recordKey = "%s/%s".formatted(datasource, vulnId);
        final byte[] serializedBov = bov.toByteArray();
        final byte[] bovDigest = DigestUtils.getSha256Digest().digest(serializedBov);

        if (!Arrays.equals(vulnDigestStore.get(Datasource.NVD, vulnId), bovDigest)) {
            // TODO: Change back to debug; info is used for demonstration purposes only.
            logger.info("{} has changed", recordKey);
            bovProducer.send(new ProducerRecord<>(
                    KafkaTopic.NEW_VULNERABILITY.getName(), recordKey, serializedBov)).get();
        } else {
            // TODO: Change back to debug; info is used for demonstration purposes only.
            logger.info("{} did not change", recordKey);
        }
    }

}
