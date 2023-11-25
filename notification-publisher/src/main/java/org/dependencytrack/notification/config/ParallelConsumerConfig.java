package org.dependencytrack.notification.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "parallel-consumer")
public interface ParallelConsumerConfig {

    int maxConcurrency();

    RetryConfig retry();

}
