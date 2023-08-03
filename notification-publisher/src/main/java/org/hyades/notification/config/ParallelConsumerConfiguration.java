package org.hyades.notification.config;

import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hyades.notification.serialization.NotificationKafkaProtobufDeserializer;
import org.hyades.proto.notification.v1.Notification;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
class ParallelConsumerConfiguration {

    private final ParallelStreamProcessor<String, Notification> parallelConsumer;
    private final KafkaClientMetrics consumerMetrics;

    ParallelConsumerConfiguration(@Identifier("default-kafka-broker") final Map<String, Object> kafkaConfig,
                                  final ParallelConsumerConfig parallelConsumerConfig,
                                  final MeterRegistry meterRegistry) {
        final KafkaConsumer<String, Notification> consumer = createConsumer(kafkaConfig);
        this.consumerMetrics = new KafkaClientMetrics(consumer);
        this.consumerMetrics.bindTo(meterRegistry);
        this.parallelConsumer = createParallelConsumer(consumer, parallelConsumerConfig, meterRegistry);
    }

    void onStop(@Observes final ShutdownEvent event) {
        if (parallelConsumer != null) {
            parallelConsumer.closeDrainFirst(Duration.ofSeconds(30));
        }
        if (consumerMetrics != null) {
            consumerMetrics.close();
        }
    }

    @Produces
    @ApplicationScoped
    ParallelStreamProcessor<String, Notification> parallelConsumer() {
        return parallelConsumer;
    }

    private static ParallelStreamProcessor<String, Notification> createParallelConsumer(
            final KafkaConsumer<String, Notification> consumer,
            final ParallelConsumerConfig parallelConsumerConfig,
            final MeterRegistry meterRegistry) {
        final var parallelConsumerOptions = ParallelConsumerOptions.<String, Notification>builder()
                .consumer(consumer)
                .maxConcurrency(parallelConsumerConfig.maxConcurrency())
                .ordering(ProcessingOrder.KEY)
                .retryDelayProvider(recordCtx -> {
                    final long delayMillis = RetryConfig
                            .toIntervalFunction(parallelConsumerConfig.retry())
                            .apply(recordCtx.getNumberOfFailedAttempts());
                    return Duration.ofMillis(delayMillis);
                })
                .meterRegistry(meterRegistry)
                .build();

        final ParallelStreamProcessor<String, Notification> parallelConsumer = ParallelStreamProcessor
                .createEosStreamProcessor(parallelConsumerOptions);

        final Optional<String> optionalPrefix = ConfigProvider.getConfig()
                .getOptionalValue("api.topic.prefix", String.class)
                .map(Pattern::quote);
        final var topicPattern = Pattern.compile(optionalPrefix.orElse("") + "dtrack\\.notification\\..+");
        parallelConsumer.subscribe(topicPattern);

        return parallelConsumer;
    }

    private static KafkaConsumer<String, Notification> createConsumer(final Map<String, Object> kafkaConfig) {
        final var consumerConfig = new HashMap<String, Object>();

        for (final Map.Entry<String, Object> entry : kafkaConfig.entrySet()) {
            if (ConsumerConfig.configNames().contains(entry.getKey())) {
                consumerConfig.put(entry.getKey(), entry.getValue());
            }
        }

        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Parallel consumer will handle commits
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, NotificationKafkaProtobufDeserializer.class);

        return new KafkaConsumer<>(consumerConfig);
    }

}
