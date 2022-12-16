package org.acme.cache;

import com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
class CacheConfig {
    
    @Produces
    CacheManager cacheManager() {
        return Caching
                .getCachingProvider(CaffeineCachingProvider.class.getName())
                .getCacheManager();
    }

}
