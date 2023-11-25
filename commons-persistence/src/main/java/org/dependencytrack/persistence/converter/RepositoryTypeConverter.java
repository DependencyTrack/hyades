package org.dependencytrack.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.dependencytrack.persistence.model.RepositoryType;

import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class RepositoryTypeConverter implements AttributeConverter<RepositoryType, String> {

    @Override
    public String convertToDatabaseColumn(final RepositoryType entityValue) {
        return ofNullable(entityValue)
                .map(repositoryType -> repositoryType.toString())
                .orElse(null);
    }

    @Override
    public RepositoryType convertToEntityAttribute(final String databaseValue) {
        return ofNullable(databaseValue)
                .map(databaseRepositoryType -> RepositoryType.valueOf(databaseRepositoryType))
                .orElse(null);
    }
}

