package org.hyades.vulnmirror.state;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;

/**
 * A {@link ContextualProcessor} for updating global state stores.
 * <p>
 * Note that this processor must not perform any transformations on the records,
 * it must simply insert or delete records from the store.
 * <p>
 * Refer to {@link StreamsBuilder#addGlobalStore(StoreBuilder, String, Consumed, ProcessorSupplier)} for details.
 *
 * @param <K> Type of the record key
 * @param <V> Type of the record value
 */
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
