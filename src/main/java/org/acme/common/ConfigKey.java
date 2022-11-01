package org.acme.common;

import alpine.Config;

public enum ConfigKey implements Config.Key{

    GENERAL_BASE_URL("base.url", ""),
    SMTP_ENABLED("smtp.enabled", true),
    SMTP_FROM_ADDRESS("smtp.from.address", "xyz"),
    SMTP_SERVER_HOSTNAME("smtp.server.hostname", "xyz"),
    SMTP_SERVER_PORT("smtp.server.port", 1),
    SMTP_USERNAME("smtp.username", "xyz"),
    SMTP_PASSWORD("smtp.password", "xyz"),
    SMTP_SSLTLS("smtp.ssltls", true),
    SMTP_TRUSTCERT("smtp.trustcert", true),
    NOTIFICATION_APPLICATION_ID("notification.application.id", "notification");

    private final String propertyName;
    private final Object defaultValue;

    ConfigKey(final String propertyName, final Object defaultValue) {
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}