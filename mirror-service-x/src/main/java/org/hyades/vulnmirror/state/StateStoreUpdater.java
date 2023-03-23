package org.hyades.vulnmirror.state;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

public class StateStoreUpdater<K, V> extends ContextualProcessor<K, V, Void, Void> {

    private final String storeName;
    private KeyValueStore<K, V> store;

    public StateStoreUpdater(final String storeName) {
        this.storeName = storeName;
    }

    @Override
    public void init(final ProcessorContext<Void, Void> context) {
        super.init(context);

        store = context().getStateStore(storeName);
    }

    @Override
    public void process(final Record<K, V> record) {
        if (record.value() == null) {
            store.delete(record.key());
        } else {
            store.put(record.key(), record.value());
        }
    }

}
