package org.dependencytrack.persistence.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CollectionIntegerConverterTest {

    @Test
    public void convertToDatastoreTest() {
        Assertions.assertTrue(new CollectionIntegerConverter().convertToDatabaseColumn(null) == null);
        Assertions.assertTrue(new CollectionIntegerConverter().convertToDatabaseColumn(List.of()).isEmpty());
        Assertions.assertTrue(new CollectionIntegerConverter().convertToDatabaseColumn(List.of(666)).equals("666"));
        Assertions.assertTrue(new CollectionIntegerConverter().convertToDatabaseColumn(List.of(666, 123)).equals("666,123"));
    }

    @Test
    public void convertToAttributeTest() {
        Assertions.assertTrue(new CollectionIntegerConverter().convertToEntityAttribute(null) == null);
        Assertions.assertTrue(new CollectionIntegerConverter().convertToEntityAttribute("")== null);
        Assertions.assertTrue(new CollectionIntegerConverter().convertToEntityAttribute(" ")== null);
        Assertions.assertTrue(new CollectionIntegerConverter().convertToEntityAttribute("666").contains(666));
    }

}