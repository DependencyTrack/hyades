package org.acme.consumer;

import org.acme.model.Component;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * A {@link Transformer} that re-keys input records to the ID of the partition they're in.
 */
public class PartitionIdReKeyTransformer implements Transformer<String, Component, KeyValue<Integer, Component>> {

    private ProcessorContext context;

    @Override
    public void init(final ProcessorContext context) {
        this.context = context;
    }

    @Override
    public KeyValue<Integer, Component> transform(final String key, final Component value) {
        return KeyValue.pair(context.partition(), value);
    }

    @Override
    public void close() {
    }

}
