package org.acme.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.model.NotificationLevel;
import org.acme.model.NotificationRule;
import org.acme.notification.NotificationScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@QuarkusTest
class NotificationRuleRepositoryTest {

    @Inject
    NotificationRuleRepository repository;

    @Test
    @TestTransaction
    public void testRuleLevelEqual() {
        createRule(NotificationScope.PORTFOLIO, NotificationLevel.WARNING);

        final List<NotificationRule> rules = repository
                .findByScopeAndForLevel(NotificationScope.PORTFOLIO, NotificationLevel.WARNING);
        Assertions.assertEquals(1, rules.size());
    }

    @Test
    @TestTransaction
    public void testRuleLevelBelow() {
        createRule(NotificationScope.PORTFOLIO, NotificationLevel.WARNING);

        final List<NotificationRule> rules = repository
                .findByScopeAndForLevel(NotificationScope.PORTFOLIO, NotificationLevel.ERROR);
        Assertions.assertEquals(1, rules.size());
    }

    @Test
    @TestTransaction
    public void testRuleLevelAbove() {
        createRule(NotificationScope.PORTFOLIO, NotificationLevel.WARNING);

        final List<NotificationRule> rules = repository
                .findByScopeAndForLevel(NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL);
        Assertions.assertEquals(0, rules.size());
    }

    private void createRule(final NotificationScope scope, final NotificationLevel level) {
        final var rule = new NotificationRule();
        rule.setUuid(UUID.randomUUID());
        rule.setName("Test Rule");
        rule.setScope(scope);
        rule.setNotificationLevel(level);
        //rule.setNotifyOn(Set.of(NotificationGroup.NEW_VULNERABILITY));
        repository.persist(rule);
    }

}