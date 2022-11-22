package org.acme.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@QuarkusTest
class ManagedUserRepositoryTest {

    @Inject
    EntityManager entityManager;

    @Inject
    ManagedUserRepository repository;


    @Test
    @TestTransaction
    @SuppressWarnings("unchecked")
    void testFindEmailsByTeam() {
        final List<Integer> teamIds = (List<Integer>) entityManager.createNativeQuery("""
                INSERT INTO "TEAM" ("NAME", "UUID") VALUES 
                    ('foo', 'ba38e779-e252-4033-8e76-156dc46cc7a6'),
                    ('bar', '507d8f3c-431d-47aa-929e-7647746d07a9')
                RETURNING "ID";
                """).getResultList();
        final int teamFooId = teamIds.get(0);
        final int teamBarId = teamIds.get(1);

        final List<Integer> userIds = (List<Integer>) entityManager.createNativeQuery("""
                INSERT INTO "MANAGEDUSER" ("EMAIL", "PASSWORD", "FORCE_PASSWORD_CHANGE", "LAST_PASSWORD_CHANGE", "NON_EXPIRY_PASSWORD", "SUSPENDED") VALUES 
                    ('foo@example.com', 'foo', false, NOW(), true, false),
                    ('bar@example.com', 'bar', false, NOW(), true, false),
                    ('baz@example.com', 'baz', false, NOW(), true, false),
                    (NULL, 'qux', false, NOW(), true, false)
                RETURNING "ID";
                """).getResultList();
        final int userFooId = userIds.get(0);
        final int userBarId = userIds.get(1);
        final int userQuxId = userIds.get(3);

        entityManager.createNativeQuery("""
                        INSERT INTO "MANAGEDUSERS_TEAMS" ("MANAGEDUSER_ID", "TEAM_ID") VALUES
                            (:userFooId, :teamFooId),
                            (:userBarId, :teamFooId),
                            (:userQuxId, :teamFooId),
                            (:userBarId, :teamBarId);
                        """)
                .setParameter("userFooId", userFooId)
                .setParameter("userBarId", userBarId)
                .setParameter("userQuxId", userQuxId)
                .setParameter("teamFooId", teamFooId)
                .setParameter("teamBarId", teamBarId)
                .executeUpdate();

        final List<String> users = repository.findEmailsByTeam(teamFooId);
        Assertions.assertEquals(2, users.size());
        Assertions.assertEquals("foo@example.com", users.get(0));
        Assertions.assertEquals("bar@example.com", users.get(1));
    }

}