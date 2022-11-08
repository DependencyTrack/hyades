package org.acme.persistence;

import com.google.inject.Inject;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.model.NotificationLevel;
import org.acme.model.NotificationRule;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;
import java.util.List;

@QuarkusTest
class NotificationHibernateManagerTest {

    @Inject
    NotificationHibernateManager notificationHibernateManager = new NotificationHibernateManager();

    @Test
    public void testNotificationRulePersistence() {

        NotificationRule notificationRule = new NotificationRule();
        notificationRule.setName("test");
        notificationRule.setNotificationLevel(NotificationLevel.INFORMATIONAL);
        notificationRule.persist();
        List<NotificationRule> notificationRuleList = notificationHibernateManager.getNotificationRules("test");
        Assert.assertNotNull(notificationRuleList);
    }
}
