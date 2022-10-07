package org.acme.common;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "common")
public interface ApplicationProperty {
    String server();

    String configProducerAppName();

    boolean enableIdempotence();

    String acksConfig();

    int deliveryTimeout();

    String cacheProducerAppName();

    String primaryEventProducer();

    String vulnCacheProducer();

    String vulnerabilityResultProducerAppName();

    String topicComponentCache();

    String topicVulnCacheResult();

    String consumerOffset();

    String primaryApplicationName();

    String primaryTopic();

    String eventTopic();

    String topicVulnCache();

    String componentCacheStoreName();
    
    String vulnCacheStoreName();

    String consumerConfigAppId();

    String configTopicName();

    int batchWaitTime();
}
