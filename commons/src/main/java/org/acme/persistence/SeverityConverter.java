package org.acme.persistence;

import org.acme.model.Severity;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class SeverityConverter implements AttributeConverter<Severity, String> {

    @Override
    public String convertToDatabaseColumn(final Severity entityValue) {
        return ofNullable(entityValue)
                .map(propertyType -> propertyType.toString())
                .orElse(null);
    }

    @Override
    public Severity convertToEntityAttribute(final String databaseValue) {
        return ofNullable(databaseValue)
                .map(databasePropertyType -> Severity.valueOf(databasePropertyType))
                .orElse(null);
    }
}

