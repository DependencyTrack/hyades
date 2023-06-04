package org.hyades.persistence;

import org.hyades.common.IConfigProperty;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class PropertyTypeConverter implements AttributeConverter<IConfigProperty.PropertyType, String> {

    @Override
    public String convertToDatabaseColumn(final IConfigProperty.PropertyType entityValue) {
        return ofNullable(entityValue)
                .map(propertyType -> propertyType.toString())
                .orElse(null);
    }

    @Override
    public IConfigProperty.PropertyType convertToEntityAttribute(final String databaseValue) {
        return ofNullable(databaseValue)
                .map(databasePropertyType -> IConfigProperty.PropertyType.valueOf(databasePropertyType))
                .orElse(null);
    }
}

