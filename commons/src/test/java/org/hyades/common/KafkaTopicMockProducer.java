package org.hyades.common;

import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.Config;
import org.hyades.config.KafkaTopicConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class KafkaTopicMockProducer {

    @Inject
    Config config;

    @Produces
    @ApplicationScoped
    @io.quarkus.test.Mock
    KafkaTopicConfig kafkaTopicConfig() {
        return config.unwrap(SmallRyeConfig.class).getConfigMapping(KafkaTopicConfig.class);
    }
}
