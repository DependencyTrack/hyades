package org.acme.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.model.ConfigProperty;
import org.acme.model.ConfigPropertyConstants;
import org.acme.model.NotificationLevel;
import org.acme.model.NotificationRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;

@QuarkusTest
class ConfigPropertyRepositoryTest {

    @Inject
    EntityManager entityManager;

    @Inject
    ConfigPropertyRepository repository;

    @Test
    @TestTransaction
    public void configProperty() {
        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("ID", "DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    (1, 'Email address', 'email', 'STRING', 'smtp.from.address', 'abc@gmail.com');
                """).executeUpdate();

        final ConfigProperty config= repository
                .findByGroupAndName(ConfigPropertyConstants.EMAIL_SMTP_FROM_ADDR.getGroupName(), ConfigPropertyConstants.EMAIL_SMTP_FROM_ADDR.getPropertyName());
        Assertions.assertEquals("abc@gmail.com", config.getPropertyValue());
    }



}