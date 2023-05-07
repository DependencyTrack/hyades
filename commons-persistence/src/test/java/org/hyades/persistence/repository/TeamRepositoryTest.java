package org.hyades.persistence.repository;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.hyades.persistence.model.Team;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.List;

@QuarkusTest
class TeamRepositoryTest {

    @Inject
    EntityManager entityManager;

    @Inject
    TeamRepository repository;

    @Test
    @TestTransaction
    @SuppressWarnings("unchecked")
    void testFindByNotificationRule() {
        final var teamIds = (List<BigInteger>) entityManager.createNativeQuery("""
                INSERT INTO "TEAM" ("NAME", "UUID") VALUES 
                    ('foo', 'fa26b29f-e106-4d62-b1a3-2073b63c9dd0'),
                    ('bar', 'c18d0094-f161-4581-96fa-bfc7e413c78d'),
                    ('baz', '6db9c0cb-9c84-440a-89a8-9bbed5d028d9')
                RETURNING "ID";
                """).getResultList();
        final BigInteger teamFooId = teamIds.get(0);
        final BigInteger teamBarId = teamIds.get(1);

        final var ruleIds = (List<BigInteger>) entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "NOTIFY_CHILDREN", "SCOPE", "UUID") VALUES
                    (true, 'foo', false, 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62'),
                    (true, 'bar', false, 'PORTFOLIO', 'ee74dc70-cd8e-41df-ae6a-1093d5f7b608')
                RETURNING "ID";
                """).getResultList();
        final BigInteger ruleFooId = ruleIds.get(0);

        entityManager.createNativeQuery("""                            
                        INSERT INTO "NOTIFICATIONRULE_TEAMS" ("NOTIFICATIONRULE_ID", "TEAM_ID") VALUES
                            (:ruleFooId, :teamFooId), 
                            (:ruleFooId, :teamBarId);
                        """)
                .setParameter("ruleFooId", ruleFooId)
                .setParameter("teamFooId", teamFooId)
                .setParameter("teamBarId", teamBarId)
                .executeUpdate();

        final List<Team> teams = repository.findByNotificationRule(ruleFooId.longValue());
        Assertions.assertEquals(2, teams.size());
        Assertions.assertEquals("foo", teams.get(0).getName());
        Assertions.assertEquals("bar", teams.get(1).getName());

        Assertions.assertEquals(0, repository.findByNotificationRule(2).size());
    }

}