package org.acme.persistence;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.acme.model.NotificationRule;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NotificationHibernateManager {

    public PanacheQuery<NotificationRule> getAllNotificationRules() {
        return NotificationRule.findAll();
    }

    public List<NotificationRule> getNotificationRules(String notificationRuleName) {
        List<NotificationRule> filtered = null;
        try (Stream<NotificationRule> notificationRuleStream = NotificationRule.streamAll()) {
            filtered = notificationRuleStream
                    .filter( n -> ! notificationRuleName.equals(n) )
                    .collect(Collectors.toList());
        }
        return filtered;
    }
}
