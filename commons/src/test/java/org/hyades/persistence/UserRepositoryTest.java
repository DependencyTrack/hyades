package org.hyades.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class UserRepositoryTest {

    @Inject
    EntityManager entityManager;

    @Inject
    UserRepository repository;

    @Test
    @TestTransaction
    @SuppressWarnings("unchecked")
    void testFindEmailsByTeam() {
        final var teamIds = (List<BigInteger>) entityManager.createNativeQuery("""
                INSERT INTO "TEAM" ("NAME", "UUID") VALUES 
                    ('foo', 'ba38e779-e252-4033-8e76-156dc46cc7a6'),
                    ('bar', '507d8f3c-431d-47aa-929e-7647746d07a9')
                RETURNING "ID";
                """).getResultList();
        final BigInteger teamFooId = teamIds.get(0);
        final BigInteger teamBarId = teamIds.get(1);

        final var managedUserIds = (List<BigInteger>) entityManager.createNativeQuery("""
                INSERT INTO "MANAGEDUSER" ("EMAIL", "PASSWORD", "FORCE_PASSWORD_CHANGE", "LAST_PASSWORD_CHANGE", "NON_EXPIRY_PASSWORD", "SUSPENDED") VALUES 
                    ('foo@managed.example.com', 'foo', false, NOW(), true, false),
                    ('bar@managed.example.com', 'bar', false, NOW(), true, false),
                    ('baz@managed.example.com', 'baz', false, NOW(), true, false),
                    (NULL, 'qux', false, NOW(), true, false),
                    ('quux@example.com', 'quux', false, NOW(), true, false)
                RETURNING "ID";
                """).getResultList();
        final BigInteger managedUserFooId = managedUserIds.get(0);
        final BigInteger managedUserBarId = managedUserIds.get(1);
        final BigInteger managedUserQuxId = managedUserIds.get(3);
        final BigInteger managedUserQuuxId = managedUserIds.get(4);

        entityManager.createNativeQuery("""
                        INSERT INTO "MANAGEDUSERS_TEAMS" ("MANAGEDUSER_ID", "TEAM_ID") VALUES
                            (:userFooId, :teamFooId),
                            (:userBarId, :teamFooId),
                            (:userQuxId, :teamFooId),
                            (:userBarId, :teamBarId),
                            (:userQuuxId, :teamBarId);
                        """)
                .setParameter("userFooId", managedUserFooId)
                .setParameter("userBarId", managedUserBarId)
                .setParameter("userQuxId", managedUserQuxId)
                .setParameter("userQuuxId", managedUserQuuxId)
                .setParameter("teamFooId", teamFooId)
                .setParameter("teamBarId", teamBarId)
                .executeUpdate();

        final var ldapUserIds = (List<BigInteger>) entityManager.createNativeQuery("""
                INSERT INTO "LDAPUSER" ("EMAIL", "DN") VALUES 
                    ('foo@ldap.example.com', 'foo'),
                    ('bar@ldap.example.com', 'bar'),
                    ('baz@ldap.example.com', 'baz'),
                    (NULL, 'qux'),
                    ('quux@example.com', 'quux')
                RETURNING "ID";
                """).getResultList();
        final BigInteger ldapUserFooId = ldapUserIds.get(0);
        final BigInteger ldapUserBarId = ldapUserIds.get(1);
        final BigInteger ldapUserQuxId = ldapUserIds.get(3);
        final BigInteger ldapUserQuuxId = ldapUserIds.get(4);

        entityManager.createNativeQuery("""
                        INSERT INTO "LDAPUSERS_TEAMS" ("LDAPUSER_ID", "TEAM_ID") VALUES
                            (:userFooId, :teamFooId),
                            (:userBarId, :teamFooId),
                            (:userQuxId, :teamFooId),
                            (:userBarId, :teamBarId),
                            (:userQuuxId, :teamBarId);
                        """)
                .setParameter("userFooId", ldapUserFooId)
                .setParameter("userBarId", ldapUserBarId)
                .setParameter("userQuxId", ldapUserQuxId)
                .setParameter("userQuuxId", ldapUserQuuxId)
                .setParameter("teamFooId", teamFooId)
                .setParameter("teamBarId", teamBarId)
                .executeUpdate();

        final var oidcUserIds = (List<BigInteger>) entityManager.createNativeQuery("""
                INSERT INTO "OIDCUSER" ("EMAIL", "USERNAME") VALUES 
                    ('foo@oidc.example.com', 'foo'),
                    ('bar@oidc.example.com', 'bar'),
                    ('baz@oidc.example.com', 'baz'),
                    (NULL, 'qux'),
                    ('quux@example.com', 'quux')
                RETURNING "ID";
                """).getResultList();
        final BigInteger oidcUserFooId = oidcUserIds.get(0);
        final BigInteger oidcUserBarId = oidcUserIds.get(1);
        final BigInteger oidcUserQuxId = oidcUserIds.get(3);
        final BigInteger oidcUserQuuxId = oidcUserIds.get(4);

        entityManager.createNativeQuery("""
                        INSERT INTO "OIDCUSERS_TEAMS" ("OIDCUSERS_ID", "TEAM_ID") VALUES
                            (:userFooId, :teamFooId),
                            (:userBarId, :teamFooId),
                            (:userQuxId, :teamFooId),
                            (:userBarId, :teamBarId),
                            (:userQuuxId, :teamBarId);
                        """)
                .setParameter("userFooId", oidcUserFooId)
                .setParameter("userBarId", oidcUserBarId)
                .setParameter("userQuxId", oidcUserQuxId)
                .setParameter("userQuuxId", oidcUserQuuxId)
                .setParameter("teamFooId", teamFooId)
                .setParameter("teamBarId", teamBarId)
                .executeUpdate();

        assertThat(repository.findEmailsByTeam(teamFooId.longValue())).containsExactlyInAnyOrder(
                "foo@managed.example.com",
                "bar@managed.example.com",
                "foo@ldap.example.com",
                "bar@ldap.example.com",
                "foo@oidc.example.com",
                "bar@oidc.example.com"
        );

        assertThat(repository.findEmailsByTeam(teamBarId.longValue())).containsExactlyInAnyOrder(
                "bar@managed.example.com",
                "bar@ldap.example.com",
                "bar@oidc.example.com",
                "quux@example.com"
                // Results are de-duplicated, thus quux@example.com must not appear more than
                // once, despite multiple users having that email.
        );
    }

}