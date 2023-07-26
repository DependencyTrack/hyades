package org.hyades.kstreams.exception;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.time.Duration;

@ConfigMapping(prefix = "kafka-streams.exception")
public interface ExceptionHandlerConfig {

    ThresholdsConfig thresholds();

    interface ThresholdsConfig {

        ThresholdConfig deserialization();

        ThresholdConfig processing();

        ThresholdConfig production();

    }

    interface ThresholdConfig {

        @WithDefault("5")
        int count();

        @WithDefault("PT1H")
        Duration interval();

    }

}
