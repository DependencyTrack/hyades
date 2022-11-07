package org.acme.analyzer;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.time.Duration;

@Dependentpom
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

}
