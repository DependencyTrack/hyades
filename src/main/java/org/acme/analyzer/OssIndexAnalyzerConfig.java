package org.acme.analyzer;

import io.micrometer.core.instrument.binder.cache.JCacheMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.acme.client.ossindex.ComponentReport;
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
public class OssIndexAnalyzerConfig {

    @Produces
    @Named("ossIndexCache")
    public Cache<String, ComponentReport> cache(@ConfigProperty(name = "scanner.ossindex.cache.validity.period") final Duration validityPeriod,
                                                final PrometheusMeterRegistry meterRegistry) {
        final MutableConfiguration<String, ComponentReport> configuration = new MutableConfiguration<>();
        configuration.setExpiryPolicyFactory(() ->
                new CreatedExpiryPolicy(new javax.cache.expiry.Duration(TimeUnit.MILLISECONDS, validityPeriod.toMillis())));

        final Cache<String, ComponentReport> cache = Caching
                .getCachingProvider(com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider.class.getName())
                .getCacheManager()
                .createCache("ossindex", configuration);
        JCacheMetrics.monitor(meterRegistry, cache);
        return cache;
    }

}
