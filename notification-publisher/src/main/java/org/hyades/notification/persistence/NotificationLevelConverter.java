package org.hyades.notification.persistence;

import org.hyades.notification.model.NotificationLevel;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class NotificationLevelConverter implements AttributeConverter<NotificationLevel, String> {

    @Override
    public String convertToDatabaseColumn(final NotificationLevel entityValue) {
        return ofNullable(entityValue)
                .map(notificationLevel -> notificationLevel.toString())
                .orElse(null);
    }

    @Override
    public NotificationLevel convertToEntityAttribute(final String databaseValue) {
        return ofNullable(databaseValue)
                .map(databaseNotificationLevel -> NotificationLevel.valueOf(databaseNotificationLevel))
                .orElse(null);
    }
}

