package org.acme.processor;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

/**
 * A {@link Processor} that re-keys input records to the ID of the partition they're in.
 */
public class PartitionIdReKeyProcessor<K, V> implements Processor<K, V, Integer, V> {

    private ProcessorContext<Integer, V> context;

    @Override
    public void init(final ProcessorContext<Integer, V> context) {
        this.context = context;
    }

    @Override
    public void process(final Record<K, V> record) {
        if (context.recordMetadata().isEmpty()) {
            throw new IllegalStateException("Record metadata not available for record (key: %s, timestamp: %d)"
                    .formatted(record.key(), record.timestamp()));
        }

        context.forward(new Record<>(context.recordMetadata().get().partition(), record.value(), record.timestamp()));
    }

}
