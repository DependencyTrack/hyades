package org.acme.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.model.Team;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@QuarkusTest
class TeamRepositoryTest {

    @Inject
    EntityManager entityManager;

    @Inject
    TeamRepository repository;

    @Test
    @TestTransaction
    void testFindByNotificationRule() {
        entityManager.createNativeQuery("""
                INSERT INTO "TEAM" ("NAME", "UUID") VALUES 
                    ('foo', 'fa26b29f-e106-4d62-b1a3-2073b63c9dd0'),
                    ('bar', 'c18d0094-f161-4581-96fa-bfc7e413c78d'),
                    ('baz', '6db9c0cb-9c84-440a-89a8-9bbed5d028d9');
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "NOTIFY_CHILDREN", "SCOPE", "UUID") VALUES
                    (true, 'foo', false, 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62'),
                    (true, 'bar', false, 'PORTFOLIO', 'ee74dc70-cd8e-41df-ae6a-1093d5f7b608');
                INSERT INTO "NOTIFICATIONRULE_TEAMS" ("NOTIFICATIONRULE_ID", "TEAM_ID") VALUES
                    (1, 1), 
                    (1, 2);
                """).executeUpdate();

        final List<Team> teams = repository.findByNotificationRule(1);
        Assertions.assertEquals(2, teams.size());
        Assertions.assertEquals("foo", teams.get(0).getName());
        Assertions.assertEquals("bar", teams.get(1).getName());

        Assertions.assertEquals(0, repository.findByNotificationRule(2).size());
    }

}