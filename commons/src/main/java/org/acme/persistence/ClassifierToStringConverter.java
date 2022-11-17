package org.acme.persistence;

import org.acme.model.Classifier;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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

