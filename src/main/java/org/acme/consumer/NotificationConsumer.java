package org.acme.consumer;

import alpine.Config;
import alpine.common.logging.Logger;
import alpine.common.metrics.Metrics;
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.runtime.StartupEvent;
import org.acme.RequirementsVerifier;
import org.acme.common.ApplicationProperty;
import org.acme.common.ConfigKey;
import org.acme.common.KafkaTopic;
import org.acme.model.Notification;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class NotificationConsumer  {

    private static final Logger LOGGER = Logger.getLogger(NotificationConsumer.class);

    private static KafkaStreams STREAMS;
    private static KafkaStreamsMetrics STREAMS_METRICS;
    @Inject
    ApplicationProperty applicationProperty;

    void onStart(@Observes StartupEvent event){
        LOGGER.info("Initializing Notification Kafka streams Consumer");
        if (RequirementsVerifier.failedValidation()) {
            LOGGER.warn("System requirements not satisfied, skipping");
            return;
        }
        final var properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, Config.getInstance().getProperty(ConfigKey.NOTIFICATION_APPLICATION_ID));
        properties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, applicationProperty.consumerOffset());
        Collection<String> topics = new ArrayList<>();
        topics.add(KafkaTopic.ANALYZER_NOTIFICATION.getName());
        topics.add(KafkaTopic.BOM_CONSUMED_NOTIFICATION.getName());
        topics.add(KafkaTopic.BOM_PROCESSED_NOTIFICATION.getName());
        topics.add(KafkaTopic.CONFIGURATION_NOTIFICATION.getName());
        topics.add(KafkaTopic.DATASOURCE_MIRRORING_NOTIFICATION.getName());
        topics.add(KafkaTopic.FILE_SYSTEM_NOTIFICATION.getName());
        topics.add(KafkaTopic.INDEXING_SERVICE_NOTIFICATION.getName());
        topics.add(KafkaTopic.INTEGRATION_NOTIFICATION.getName());
        topics.add(KafkaTopic.NEW_VULNERABILITY_NOTIFICATION.getName());
        topics.add(KafkaTopic.NEW_VULNERABLE_DEPENDENCY_NOTIFICATION.getName());
        topics.add(KafkaTopic.POLICY_VIOLATION_NOTIFICATION.getName());
        topics.add(KafkaTopic.PROJECT_AUDIT_CHANGE_NOTIFICATION.getName());
        topics.add(KafkaTopic.REPOSITORY_NOTIFICATION.getName());
        topics.add(KafkaTopic.VEX_CONSUMED_NOTIFICATION.getName());
        topics.add(KafkaTopic.VEX_PROCESSED_NOTIFICATION.getName());
        final var streamsBuilder = new StreamsBuilder();
        final var notificationSerde = new ObjectMapperSerde<>(Notification.class);
        KStream<String, Notification> kStreams = streamsBuilder.stream(topics,
                        Consumed.with(Serdes.String(), notificationSerde));
        kStreams.foreach(new ForeachAction<String, Notification>() {
            @Override
            public void apply(String s, Notification eventData) {
                System.out.println("notification recd");
                Notification.dispatch(eventData);
            }
        });

        STREAMS = new KafkaStreams(streamsBuilder.build(), new StreamsConfig(properties));

        if (Config.getInstance().getPropertyAsBoolean(Config.AlpineKey.METRICS_ENABLED)) {
            LOGGER.info("Registering Kafka streams metrics");
            STREAMS_METRICS = new KafkaStreamsMetrics(STREAMS);
            STREAMS_METRICS.bindTo(Metrics.getRegistry());
        }

        STREAMS.start();
    }



}
