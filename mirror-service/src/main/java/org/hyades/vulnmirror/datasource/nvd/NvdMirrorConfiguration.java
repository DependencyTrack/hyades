package org.hyades.vulnmirror.datasource.nvd;

import io.github.jeremylong.openvulnerability.client.nvd.NvdApiException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff;

class NvdMirrorConfiguration {
    @Produces
    @ApplicationScoped
    @Named("nvdExecutorService")
    ExecutorService executorService() {
        final var threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("hyades-mirror-nvd-%d")
                .build();

        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), threadFactory);
    }

    @Produces
    @ApplicationScoped
    @Named("nvdDurationTimer")
    Timer durationTimer(final MeterRegistry meterRegistry) {
        return Timer.builder("mirror.nvd.duration")
                .description("Duration of NVD mirroring operations")
                .register(meterRegistry);
    }

    @Produces
    @ApplicationScoped
    @Named("nvdMirrorRetry")
    Retry createRetry(NvdConfig config){
        final RetryRegistry retryRegistry = RetryRegistry.of(RetryConfig.custom().
                intervalFunction(ofExponentialBackoff(
                        Duration.ofSeconds(config.retryBackoffInitialDurationSeconds()),
                        config.retryBackoffMultiplier(), Duration.ofSeconds(config.retryMaxDuration())))
                .maxAttempts(config.retryMaxAttempts())
                .retryOnException(NvdApiException.class::isInstance)
                .retryOnResult(response -> false)
                .build());
        return retryRegistry.retry("nvdMirrorRetry");
    }
}
