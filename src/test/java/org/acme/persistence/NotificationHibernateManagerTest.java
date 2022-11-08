package org.acme.persistence;

import org.acme.model.NotificationRule;
import org.junit.jupiter.api.Test;

public class NotificationHibernateManagerTest {

    @Test
    public void testNotificationRulePersistence() {

        NotificationRule notificationRule = new NotificationRule();
        notificationRule.setName("test");
        notificationRule.setNotificationLevel(NotificationLevel.);
    }
}
