package org.dependencytrack.kstreams.processor;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.dependencytrack.kstreams.util.FixedKeyRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link ContextualProcessor} that tracks timestamps of processed records,
 * and emits tombstone records for keys that haven't been seen for a configurable time frame.
 * <p>
 * The primary use case enabled by this processor is the implementation of key-based TTL
 * policies for {@link KTable} entries.
 *
 * @param <K> Key type of incoming and outgoing records
 * @param <V> Value type of incoming and outgoing records
 * @see <a href="https://developer.confluent.io/tutorials/schedule-ktable-ttl/kstreams.html">Related Confluent Tutorial</a>
 */
public class TombstoneEmittingProcessor<K, V> extends ContextualFixedKeyProcessor<K, V, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TombstoneEmittingProcessor.class);

    private final String storeName;
    private final Duration checkInterval;
    private final Duration maxLifetime;
    private final Function<K, V> tombstoneSupplier;
    private KeyValueStore<K, Long> store;
    private Cancellable punctuator;

    TombstoneEmittingProcessor(final String storeName, final Duration checkInterval, final Duration maxLifetime,
                               final Function<K, V> tombstoneSupplier) {
        this.storeName = storeName;
        this.checkInterval = checkInterval;
        this.maxLifetime = maxLifetime;
        this.tombstoneSupplier = tombstoneSupplier;
    }

    @Override
    public void init(final FixedKeyProcessorContext<K, V> context) {
        super.init(context);

        store = context().getStateStore(storeName);
        punctuator = context().schedule(checkInterval, PunctuationType.STREAM_TIME, this::punctuate);
    }

    @Override
    public void process(final FixedKeyRecord<K, V> record) {
        if (record.value() == null) {
            store.delete(record.key());
        } else {
            store.put(record.key(), record.timestamp());
        }

        context().forward(record);
    }

    @Override
    public void close() {
        Optional.ofNullable(punctuator).ifPresent(Cancellable::cancel);
    }

    private void punctuate(final long timestamp) {
        final Instant cutoffTimestamp = Instant.ofEpochMilli(timestamp).minus(maxLifetime);

        try (final KeyValueIterator<K, Long> all = store.all()) {
            while (all.hasNext()) {
                final KeyValue<K, Long> record = all.next();
                if (record.value != null && Instant.ofEpochMilli(record.value).isBefore(cutoffTimestamp)) {
                    LOGGER.debug("Sending tombstone for key {}", record.key);
                    context().forward(FixedKeyRecordFactory.create(
                            record.key, tombstoneSupplier.apply(record.key), context().currentStreamTimeMs(), null));
                    store.delete(record.key);
                }
            }
        }
    }

}
