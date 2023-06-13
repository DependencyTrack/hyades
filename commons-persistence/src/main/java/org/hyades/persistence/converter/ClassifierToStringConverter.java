package org.hyades.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hyades.persistence.model.Classifier;
import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class ClassifierToStringConverter implements AttributeConverter<Classifier, String> {

    @Override
    public String convertToDatabaseColumn(final Classifier entityValue) {
        return ofNullable(entityValue)
                .map(classifier -> classifier.toString())
                .orElse(null);
    }

    @Override
    public Classifier convertToEntityAttribute(final String databaseValue) {
        return ofNullable(databaseValue)
                .map(databaseClassifier -> Classifier.valueOf(databaseClassifier))
                .orElse(null);
    }
}

