package org.acme.common;

import io.smallrye.config.ConfigMapping;
import org.apache.kafka.common.protocol.types.Field;

@ConfigMapping(prefix = "poc")
public interface ApplicationProperty {
    String server();

    String analysisTopic();

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

    String topicVulnCache();

    String componentCacheStoreName();

    String vulnCacheStoreName();

    String consumerConfigAppId();

    String configTopicName();

    int batchWaitTime();

    String primaryApplicationName();

    String primaryTopic();

    String ossApplicationName();

    String ossStoreName();

    int timeDifference();

    int gracePeriod();

    String snykApplicationName();

    String snykStoreName();

    int retries();

    int consumerBatchSizeSnyk();

    int consumerBatchSizeOss();

   /* boolean smtpEnabled();
    String smtpFromAddress();
    String smtpServerHostname();

    int smtpServerPort();

    String smtpUsername();
    String smtpPassword();
    boolean smtpSsltls();
    boolean smptTrustcert();*/

}
