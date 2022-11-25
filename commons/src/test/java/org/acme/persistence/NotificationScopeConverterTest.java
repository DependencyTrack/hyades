package org.acme.persistence;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.commonnotification.NotificationScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


@QuarkusTest
public class NotificationScopeConverterTest {

    @Test
    public void convertToDatastoreTest() {
        Assertions.assertNull((new NotificationScopeConverter().convertToDatabaseColumn(null)));
        Assertions.assertTrue((new NotificationScopeConverter().convertToDatabaseColumn(NotificationScope.PORTFOLIO).equals("PORTFOLIO")));
        Assertions.assertTrue((new NotificationScopeConverter().convertToDatabaseColumn(NotificationScope.SYSTEM).equals("SYSTEM")));
    }

    @Test
    public void convertToAttributeTest() {
        Assertions.assertEquals(new NotificationScopeConverter().convertToEntityAttribute("PORTFOLIO"), NotificationScope.PORTFOLIO);
        Assertions.assertEquals(new NotificationScopeConverter().convertToEntityAttribute("SYSTEM"), NotificationScope.SYSTEM);

    }

}
