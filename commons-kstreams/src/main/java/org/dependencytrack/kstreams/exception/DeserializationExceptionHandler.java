package org.dependencytrack.kstreams.exception;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.dependencytrack.common.config.QuarkusConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;

@RegisterForReflection
public class DeserializationExceptionHandler extends AbstractThresholdBasedExceptionHandler
        implements org.apache.kafka.streams.errors.DeserializationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeserializationExceptionHandler.class);


    @SuppressWarnings("unused") // Called by Kafka Streams via reflection
    public DeserializationExceptionHandler() {
        super(QuarkusConfigUtil.getConfigMapping(ExceptionHandlerConfig.class)
                .map(ExceptionHandlerConfig::thresholds)
                .map(ExceptionHandlerConfig.ThresholdsConfig::deserialization)
                .orElse(null));
    }

    DeserializationExceptionHandler(final Clock clock, final Duration exceptionThresholdInterval, final int exceptionThresholdCount) {
        super(clock, exceptionThresholdInterval, exceptionThresholdCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Map<String, ?> configs) {
        // Configuration is done via Quarkus config.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DeserializationHandlerResponse handle(final ProcessorContext processorContext,
                                                              final ConsumerRecord<byte[], byte[]> record,
                                                              final Exception exception) {
        // TODO: Use KafkaEventDispatcher to send the record to a dead letter topic?
        if (exceedsThreshold()) {
            LOGGER.error("""
                    Failed to deserialize record from topic %s (partition: %d, offset %d); \
                    Stopping to consume records, as the error was encountered %d times since %s, \
                    exceeding the configured threshold of %d occurrences in an interval of %s\
                    """
                    .formatted(record.topic(), record.partition(), record.offset(),
                            exceptionOccurrences(), firstExceptionOccurredAt(),
                            exceptionThresholdCount(), exceptionThresholdInterval()), exception);
            return DeserializationHandlerResponse.FAIL;
        }

        LOGGER.warn("""
                Failed to deserialize record from topic %s (partition: %d, offset: %d); \
                Skipping and continuing to consume records, as the configured threshold of \
                %d occurrences in an interval of %s has not been exceeded yet\
                """
                .formatted(record.topic(), record.partition(), record.offset(),
                        exceptionThresholdCount(), exceptionThresholdInterval()), exception);
        return DeserializationHandlerResponse.CONTINUE;
    }

}
