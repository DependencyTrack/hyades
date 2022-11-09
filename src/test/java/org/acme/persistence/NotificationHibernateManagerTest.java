package org.acme.persistence;

import com.google.inject.Inject;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.model.NotificationLevel;
import org.acme.model.NotificationRule;
import org.acme.notification.NotificationScope;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@QuarkusTest
class NotificationHibernateManagerTest {

    @Inject
    NotificationHibernateManager notificationHibernateManager = new NotificationHibernateManager();

    @Test
    @Transactional
    public void testNotificationRulePersistence() {

        NotificationRule notificationRule = new NotificationRule();
        notificationRule.setUuid(UUID.randomUUID());
        notificationRule.setName("test");
        notificationRule.setNotificationLevel(NotificationLevel.INFORMATIONAL);
        notificationRule.setScope(NotificationScope.SYSTEM);
        notificationRule.persist();
        List<NotificationRule> notificationRuleList = notificationHibernateManager.getNotificationRules("test");
        Assert.assertNotNull(notificationRuleList);
    }
}
