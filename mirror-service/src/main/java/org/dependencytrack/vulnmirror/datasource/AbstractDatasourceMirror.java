/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.vulnmirror.datasource;

import com.google.protobuf.Timestamp;
import io.github.jeremylong.openvulnerability.client.epss.EpssItem;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.cyclonedx.proto.v1_4.Bom;
import org.dependencytrack.common.KafkaTopic;
import org.dependencytrack.proto.notification.v1.Group;
import org.dependencytrack.proto.notification.v1.Level;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.proto.notification.v1.Scope;
import org.dependencytrack.vulnmirror.state.MirrorStateStore;
import org.dependencytrack.vulnmirror.state.VulnerabilityDigestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public abstract class AbstractDatasourceMirror<T> implements DatasourceMirror {

    private Datasource datasource;
    public MirrorStateStore mirrorStateStore;
    private VulnerabilityDigestStore vulnDigestStore;
    private Producer<String, byte[]> kafkaProducer;
    private Class<T> stateClass;
    private Logger logger;

    /**
     * Non-private, no-args constructor required by Quarkus.
     * <p>
     * DO NOT USE, use {@link #AbstractDatasourceMirror(Datasource, MirrorStateStore, VulnerabilityDigestStore, Producer, Class)} instead.
     *
     * @see <a href="https://github.com/quarkusio/quarkus/issues/22669">Quarkus Issue #22669</a>
     */
    @SuppressWarnings("unused")
    protected AbstractDatasourceMirror() {
    }

    /**
     * @param datasource       The {@link Datasource} supported by this mirror
     * @param mirrorStateStore The state store to use for persisting state
     * @param vulnDigestStore  The state store to use for querying vulnerability digests
     * @param kafkaProducer    The Kafka {@link Producer} to use for publishing events
     * @param stateClass       Class of the state object
     */
    protected AbstractDatasourceMirror(final Datasource datasource,
                                       final MirrorStateStore mirrorStateStore,
                                       final VulnerabilityDigestStore vulnDigestStore,
                                       final Producer<String, byte[]> kafkaProducer,
                                       final Class<T> stateClass) {
        this.datasource = datasource;
        this.mirrorStateStore = mirrorStateStore;
        this.vulnDigestStore = vulnDigestStore;
        this.kafkaProducer = kafkaProducer;
        this.stateClass = stateClass;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * {@inheritDoc}
     */
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

        if (!Arrays.equals(vulnDigestStore.get(datasource, vulnId), bovDigest)) {
            logger.debug("{} has changed", recordKey);
            kafkaProducer.send(new ProducerRecord<>(
                    KafkaTopic.NEW_VULNERABILITY.getName(), recordKey, serializedBov)).get();
        } else {
            logger.debug("{} did not change", recordKey);
        }
    }

    /**
     * Publish EPSS items to Kafka with cveId as the key.
     *
     * @param epssItems List of EpssItems
     */
    protected void publishEpss(final List<EpssItem> epssItems) throws ExecutionException, InterruptedException {
        if (epssItems.isEmpty()) {
            throw new IllegalArgumentException("List must contain exactly one EPSS item");
        }
        for (EpssItem epssItem : epssItems) {
            final var serializedEpss = org.dependencytrack.proto.mirror.v1.EpssItem.newBuilder()
                    .setCve(epssItem.getCve())
                    .setPercentile(epssItem.getPercentile())
                    .setEpss(epssItem.getEpss()).build();
            kafkaProducer.send(new ProducerRecord<>(
                    KafkaTopic.VULNERABILITY_MIRROR_EPSS.getName(), epssItem.getCve(), serializedEpss.toByteArray())).get();
        }
    }

    /**
     * Publish a {@link Notification} of group {@link Group#GROUP_DATASOURCE_MIRRORING} to Kafka.
     *
     * @param level   The {@link Level} of the {@link Notification}
     * @param title   The title of the {@link Notification}
     * @param content The content of the {@link Notification}
     */
    protected void dispatchNotification(final Level level, final String title, final String content) {
        kafkaProducer.send(new ProducerRecord<>(
                KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), null,
                Notification.newBuilder()
                        .setScope(Scope.SCOPE_SYSTEM)
                        .setGroup(Group.GROUP_DATASOURCE_MIRRORING)
                        .setLevel(level)
                        .setTitle(title)
                        .setContent(content)
                        .setTimestamp(Timestamp.newBuilder()
                                .setSeconds(Instant.now().getEpochSecond()))
                        .build()
                        .toByteArray()));
    }

}