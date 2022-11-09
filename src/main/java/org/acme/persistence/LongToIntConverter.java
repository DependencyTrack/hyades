package org.acme.persistence;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class LongToIntConverter implements AttributeConverter<Long, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Long entityValue) {
        return ofNullable(entityValue)
                .map(longType -> longType.intValue())
                .orElse(null);
    }

    @Override
    public Long convertToEntityAttribute(final Integer databaseValue) {
        return ofNullable(databaseValue)
                .map(intType -> Long.valueOf(intType))
                .orElse(null);
    }
}

