package org.acme.persistence;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.acme.model.NotificationPublisher;
import org.acme.model.NotificationRule;
import org.acme.notification.publisher.Publisher;

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
                    .filter( n -> ! notificationRuleName.equals(n.getName()) )
                    .collect(Collectors.toList());
        }
        return filtered;
    }

    /**
     * Retrieves a NotificationPublisher by its class.
     * @param clazz The Class of the NotificationPublisher
     * @return a NotificationPublisher
     */
    public NotificationPublisher getDefaultNotificationPublisher(final Class<Publisher> clazz) {
        return getDefaultNotificationPublisher(clazz.getCanonicalName());
    }

    /**
     * Retrieves a NotificationPublisher by its class.
     * @param clazz The Class of the NotificationPublisher
     * @return a NotificationPublisher
     */
    private NotificationPublisher getDefaultNotificationPublisher(final String clazz) {
        List<NotificationPublisher> filtered;
        try (Stream<NotificationPublisher> notificationPublishers = NotificationPublisher.streamAll()) {
            filtered = notificationPublishers
                    .filter( n -> ! clazz.equals(n.getPublisherClass())
                                && n.isDefaultPublisher())
                    .collect(Collectors.toList());
        }
        return filtered.get(0);
    }

    /**
     * Creates a NotificationPublisher object.
     * @param name The name of the NotificationPublisher
     * @return a NotificationPublisher
     */
    public NotificationPublisher createNotificationPublisher(final String name, final String description,
                                                             final Class<Publisher> publisherClass, final String templateContent,
                                                             final String templateMimeType, final boolean defaultPublisher) {
        final NotificationPublisher publisher = new NotificationPublisher();
        publisher.setName(name);
        publisher.setDescription(description);
        publisher.setPublisherClass(publisherClass.getName());
        publisher.setTemplate(templateContent);
        publisher.setTemplateMimeType(templateMimeType);
        publisher.setDefaultPublisher(defaultPublisher);
        // all modifications are automatically persisted on transaction commit.
        publisher.persist();
        // TODO -> pm.getFetchPlan().addGroup(NotificationPublisher.FetchGroup.ALL.name());
        return NotificationPublisher.findById(publisher.getId());
    }

}
