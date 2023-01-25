package org.hyades.persistence;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(final UUID entityValue) {
        return ofNullable(entityValue)
                .map(entityUuid -> entityUuid.toString())
                .orElse(null);
    }

    @Override
    public UUID convertToEntityAttribute(final String databaseValue) {
        return ofNullable(databaseValue)
                .map(databaseUuid -> UUID.fromString(databaseUuid))
                .orElse(null);
    }
}
