package org.acme.processor;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * A {@link Processor} that batches record values.
 *
 * @param <K> Type of the record keys
 * @param <V> Type of the record values
 */
public class BatchProcessor<K, V> implements Processor<K, V, K, List<V>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessor.class);

    private final String stateStoreName;
    private final Duration punctuationInterval;
    private final int maxBatchSize;
    private ProcessorContext<K, List<V>> context;
    private KeyValueStore<K, List<V>> stateStore;
    private Cancellable punctuator;

    /**
     * Create a new {@link BatchProcessor} instance.
     * <p>
     * A batch may be forwarded downstream in any of the following scenarios:
     * <ol>
     *     <li>The number of accumulated values for a given key exceeds {@code maxBatchSize}</li>
     *     <li>{@code punctuationInterval} has elapsed</li>
     * </ol>
     * <p>
     * Note that {@code punctuationInterval} is based on wall clock time, so the punctuator will
     * be triggered regardless of new records arriving (stream time).
     *
     * @param stateStoreName      Name of the {@link KeyValueStore} to use
     * @param punctuationInterval Interval of the punctuation
     * @param maxBatchSize        Maximum batch size
     */
    public BatchProcessor(final String stateStoreName, final Duration punctuationInterval, final int maxBatchSize) {
        this.stateStoreName = stateStoreName;
        this.punctuationInterval = punctuationInterval;
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    public void init(final ProcessorContext<K, List<V>> context) {
        this.context = context;
        this.stateStore = context.getStateStore(stateStoreName);
        this.punctuator = context.schedule(punctuationInterval, PunctuationType.WALL_CLOCK_TIME, this::punctuate);
    }

    @Override
    public void process(final Record<K, V> record) {
        List<V> batch = stateStore.get(record.key());
        if (batch == null || batch.isEmpty()) {
            batch = List.of(record.value());
        } else {
            batch.add(record.value());
        }

        if (batch.size() >= maxBatchSize) {
            LOGGER.info("Forwarding batch (key: {}, components: {})", record.key(), batch.size());
            context.forward(new Record<>(record.key(), batch, context.currentSystemTimeMs()));
            stateStore.put(record.key(), null);
        } else {
            stateStore.put(record.key(), batch);
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Cancelling punctuator");
        punctuator.cancel();
    }

    private void punctuate(final long timestamp) {
        LOGGER.debug("Punctuator triggered (at {})", timestamp);
        try (final KeyValueIterator<K, List<V>> valueIterator = stateStore.all()) {
            while (valueIterator.hasNext()) {
                final KeyValue<K, List<V>> kv = valueIterator.next();
                if (kv.value != null && !kv.value.isEmpty()) {
                    LOGGER.info("Forwarding batch (key: {}, components: {})", kv.key, kv.value.size());
                    context.forward(new Record<>(kv.key, kv.value, context.currentSystemTimeMs()));
                    stateStore.put(kv.key, null);
                }
            }
        }
    }

}
