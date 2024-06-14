/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.notification.config;

import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.internals.AsyncKafkaConsumer;
import org.apache.kafka.clients.consumer.internals.ConsumerCoordinator;
import org.apache.kafka.clients.consumer.internals.LegacyKafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.dependencytrack.notification.serialization.NotificationKafkaProtobufDeserializer;
import org.dependencytrack.proto.notification.v1.Notification;
import org.eclipse.microprofile.config.ConfigProvider;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
@RegisterForReflection(targets = {
        // parallel-consumer uses reflection with these classes
        // to determine whether auto-commit is enabled.
        // https://github.com/confluentinc/parallel-consumer/pull/762
        KafkaConsumer.class,
        AsyncKafkaConsumer.class,
        LegacyKafkaConsumer.class,
        ConsumerCoordinator.class
})
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
                .getOptionalValue("kafka.topic.prefix", String.class)
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
