package org.hyades.config;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "api.topic")
public interface KafkaTopicConfig {

    Optional<String> prefix();
}
