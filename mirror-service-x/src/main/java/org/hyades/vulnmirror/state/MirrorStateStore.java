package org.hyades.vulnmirror.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.hyades.common.KafkaTopic;
import org.hyades.vulnmirror.datasource.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.time.Duration;

@ApplicationScoped
public class MirrorStateStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(MirrorStateStore.class);

    private final Producer<String, byte[]> stateProducer;
    private final ObjectMapper objectMapper;

    MirrorStateStore(final Producer<String, byte[]> stateProducer,
                     final ObjectMapper objectMapper) {
        this.stateProducer = stateProducer;
        this.objectMapper = objectMapper;
    }

    public <T> void put(final Datasource datasource, final T state) {
        stateProducer.send(new ProducerRecord<>(KafkaTopic.VULNERABILITY_MIRROR_STATE.getName(),
                datasource.name(), serialize(state)));
    }

    /**
     * Publish an event to the store's changelog topic, <em>and wait for the local state store to become consistent</em>.
     * <p>
     * Will wait up to 30 seconds. This is a blocking operation.
     *
     * @param datasource The {@link Datasource} to update the state for
     * @param state      The state to update
     * @param <T>        Type of the state
     * @see #put(Datasource, Object)
     */
    public <T> void putAndWait(final Datasource datasource, final T state) {
        put(datasource, state);
        Failsafe
                .with(RetryPolicy.builder()
                        .handleResultIf(result -> {
                            // TODO: Change back to debug; info is used for demonstration purposes only.
                            LOGGER.info("Waiting for state to become consistent (want: {}; got: {})", state, result);
                            return !state.equals(result);
                        })
                        .withDelay(Duration.ofMillis(100))
                        .withMaxDuration(Duration.ofSeconds(30))
                        .withMaxRetries(-1) // Unlimited
                        .build())
                .get(() -> get(datasource, state.getClass()));
    }

    public <T> T get(final Datasource datasource, final Class<T> clazz) {
        final ReadOnlyKeyValueStore<String, byte[]> store = StateStores.keyValueStore(StateStores.MIRROR_STATES);
        final byte[] stateBytes = store.get(datasource.name());
        if (stateBytes == null) {
            return null;
        }

        return deserialize(stateBytes, clazz);
    }

    private <T> byte[] serialize(final T state) {
        try {
            return objectMapper.writeValueAsBytes(state);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    private <T> T deserialize(final byte[] bytes, final Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

}
