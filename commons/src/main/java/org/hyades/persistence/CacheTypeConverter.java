package org.hyades.persistence;

import org.hyades.model.ComponentAnalysisCache;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class CacheTypeConverter implements AttributeConverter<ComponentAnalysisCache.CacheType, String> {

    @Override
    public String convertToDatabaseColumn(final ComponentAnalysisCache.CacheType entityValue) {
        return ofNullable(entityValue)
                .map(cacheType -> cacheType.toString())
                .orElse(null);
    }

    @Override
    public ComponentAnalysisCache.CacheType convertToEntityAttribute(final String databaseValue) {
        return ofNullable(databaseValue)
                .map(databaseCacheType -> ComponentAnalysisCache.CacheType.valueOf(databaseCacheType))
                .orElse(null);
    }
}

