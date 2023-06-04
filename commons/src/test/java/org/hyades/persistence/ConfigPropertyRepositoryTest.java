package org.hyades.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.hyades.model.ConfigProperty;
import org.hyades.model.ConfigPropertyConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

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