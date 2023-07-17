package org.hyades.kstreams.exception;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.config.SmallRyeConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

@RegisterForReflection
public class DeserializationExceptionHandler implements org.apache.kafka.streams.errors.DeserializationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeserializationExceptionHandler.class);

    private final Duration exceptionThresholdInterval;
    private final int exceptionThresholdCount;
    private Instant firstExceptionOccurredAt;
    private int exceptionOccurrences;

    @SuppressWarnings("unused") // Called by Kafka Streams via reflection
    public DeserializationExceptionHandler() {
        ExceptionHandlerConfig exceptionHandlerConfig;
        try {
            final var config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
            exceptionHandlerConfig = config.getConfigMapping(ExceptionHandlerConfig.class);
        } catch (NoSuchElementException | IllegalStateException e) {
            // When running tests without @QuarkusTest, resolving of the ConfigMapping will not work.
            LOGGER.debug("State store config could not be resolved; Falling back to conservative defaults", e);
            exceptionHandlerConfig = null;
        }
        if (exceptionHandlerConfig != null) {
            this.exceptionThresholdInterval = exceptionHandlerConfig.thresholds().deserialization().interval();
            this.exceptionThresholdCount = exceptionHandlerConfig.thresholds().deserialization().count();
        } else {
            this.exceptionThresholdInterval = Duration.ofMinutes(30);
            this.exceptionThresholdCount = 5;
        }
    }

    DeserializationExceptionHandler(final Duration exceptionThresholdInterval, final int exceptionThresholdCount) {
        this.exceptionThresholdInterval = exceptionThresholdInterval;
        this.exceptionThresholdCount = exceptionThresholdCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Map<String, ?> configs) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DeserializationHandlerResponse handle(final ProcessorContext processorContext,
                                                              final ConsumerRecord<byte[], byte[]> record,
                                                              final Exception exception) {
        // TODO: Use KafkaEventDispatcher to send the record to a dead letter topic?
        final Instant now = Instant.now();
        if (firstExceptionOccurredAt == null) {
            firstExceptionOccurredAt = now;
            exceptionOccurrences = 1;
        } else {
            exceptionOccurrences++;
        }

        final Instant cutoff = firstExceptionOccurredAt.plus(exceptionThresholdInterval);
        if (now.isAfter(cutoff)) {
            firstExceptionOccurredAt = now;
            exceptionOccurrences = 1;
        }

        if (exceptionOccurrences >= exceptionThresholdCount) {
            LOGGER.error("""
                    Failed to deserialize record from topic %s (partition: %d, offset %d); \
                    Stopping to consume records, as the error was encountered %d times since %s, \
                    exceeding the configured threshold of %d occurrences in an interval of %s\
                    """
                    .formatted(record.topic(), record.partition(), record.offset(),
                            exceptionOccurrences, firstExceptionOccurredAt,
                            exceptionThresholdCount, exceptionThresholdInterval), exception);
            return DeserializationHandlerResponse.FAIL;
        }

        LOGGER.warn("""
                Failed to deserialize record from topic %s (partition: %d, offset: %d); \
                Skipping and continuing to consume records, as the configured threshold of \
                %d occurrences in an interval of %s has not been exceeded yet\
                """
                .formatted(record.topic(), record.partition(), record.offset(),
                        exceptionThresholdCount, exceptionThresholdInterval), exception);
        return DeserializationHandlerResponse.CONTINUE;
    }

}
