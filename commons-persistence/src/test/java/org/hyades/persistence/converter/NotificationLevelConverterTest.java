package org.hyades.persistence.converter;

import org.hyades.persistence.model.NotificationLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NotificationLevelConverterTest {

    @Test
    public void convertToDatastoreTest() {
        Assertions.assertNull((new NotificationLevelConverter().convertToDatabaseColumn(null)));
        Assertions.assertTrue((new NotificationLevelConverter().convertToDatabaseColumn(NotificationLevel.ERROR).equals("ERROR")));
        Assertions.assertTrue((new NotificationLevelConverter().convertToDatabaseColumn(NotificationLevel.INFORMATIONAL).equals("INFORMATIONAL")));
    }

    @Test
    public void convertToAttributeTest() {
        Assertions.assertEquals(new NotificationLevelConverter().convertToEntityAttribute("ERROR"), NotificationLevel.ERROR);
        Assertions.assertEquals(new NotificationLevelConverter().convertToEntityAttribute("INFORMATIONAL"), NotificationLevel.INFORMATIONAL);

    }

}
