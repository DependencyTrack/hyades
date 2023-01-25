package org.hyades.persistence;

import org.hyades.commonnotification.NotificationScope;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class NotificationScopeConverter implements AttributeConverter<NotificationScope, String> {

    @Override
    public String convertToDatabaseColumn(final NotificationScope entityValue) {
        return ofNullable(entityValue)
                .map(notificationScope -> notificationScope.toString())
                .orElse(null);
    }

    @Override
    public NotificationScope convertToEntityAttribute(final String databaseValue) {
        return ofNullable(databaseValue)
                .map(databaseNotificationScope -> NotificationScope.valueOf(databaseNotificationScope))
                .orElse(null);
    }
}

