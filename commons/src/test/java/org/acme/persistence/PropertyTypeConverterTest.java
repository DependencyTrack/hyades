package org.acme.persistence;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.common.IConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


@QuarkusTest
public class PropertyTypeConverterTest {

    @Test
    public void convertToDatastoreTest() {
        Assertions.assertNull((new PropertyTypeConverter().convertToDatabaseColumn(null)));
        Assertions.assertTrue((new PropertyTypeConverter().convertToDatabaseColumn(IConfigProperty.PropertyType.ENCRYPTEDSTRING).equals("ENCRYPTEDSTRING")));
        Assertions.assertTrue((new PropertyTypeConverter().convertToDatabaseColumn(IConfigProperty.PropertyType.BOOLEAN).equals("BOOLEAN")));
    }

    @Test
    public void convertToAttributeTest() {
        Assertions.assertEquals(new PropertyTypeConverter().convertToEntityAttribute("ENCRYPTEDSTRING"), IConfigProperty.PropertyType.ENCRYPTEDSTRING);
        Assertions.assertEquals(new PropertyTypeConverter().convertToEntityAttribute("BOOLEAN"), IConfigProperty.PropertyType.BOOLEAN);

    }

}
