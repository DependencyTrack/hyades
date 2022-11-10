package org.acme.analyzer;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.binder.cache.JCacheMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.acme.client.snyk.Issue;
import org.acme.client.snyk.Page;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Dependent
public class SnykAnalyzerConfig {

    @Produces
    @Named("snykRateLimiterRegistry")
    public RateLimiterRegistry rateLimiterRegistry(@ConfigProperty(name = "scanner.snyk.ratelimit.timeout.duration", defaultValue = "60") final long timeoutDuration,
                                                   @ConfigProperty(name = "scanner.snyk.ratelimit.limit.for.period", defaultValue = "1500") final int limitForPeriod,
                                                   @ConfigProperty(name = "scanner.snyk.ratelimit.limit.refresh.period", defaultValue = "60") final long limitRefreshPeriod) {
        final RateLimiterConfig config = RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(timeoutDuration))
                .limitRefreshPeriod(Duration.ofSeconds(limitRefreshPeriod))
                .limitForPeriod(limitForPeriod)
                .build();
        return RateLimiterRegistry.of(config);
    }

    @Produces
    @Named("snykRateLimiter")
    public RateLimiter rateLimiter(@Named("snykRateLimiterRegistry") final RateLimiterRegistry rateLimiterRegistry) {
        return rateLimiterRegistry.rateLimiter("snyk");
    }

    @Produces
    @Named("snykCache")
    public Cache<String, Page<Issue>> cache(@ConfigProperty(name = "scanner.snyk.cache.validity.period") final Duration validityPeriod,
                                            final PrometheusMeterRegistry meterRegistry) {
        final var configuration = new MutableConfiguration<String, Page<Issue>>()
                .setStatisticsEnabled(true)
                .setExpiryPolicyFactory(() ->
                        new CreatedExpiryPolicy(new javax.cache.expiry.Duration(TimeUnit.MILLISECONDS, validityPeriod.toMillis())));

        final Cache<String, Page<Issue>> cache = Caching
                .getCachingProvider(com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider.class.getName())
                .getCacheManager()
                .createCache("snyk", configuration);
        JCacheMetrics.monitor(meterRegistry, cache);
        return cache;
    }

}
