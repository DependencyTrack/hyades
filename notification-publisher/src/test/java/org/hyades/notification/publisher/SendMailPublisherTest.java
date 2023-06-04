package org.hyades.notification.publisher;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.hyades.model.Team;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class SendMailPublisherTest {

    @Inject
    EntityManager entityManager;

    @Inject
    SendMailPublisher publisher;

    @Test
    public void testSingleDestination() {
        JsonObject config = configWithDestination("john@doe.com");
        Assertions.assertArrayEquals(new String[]{"john@doe.com"}, SendMailPublisher.parseDestination(config));
    }

    @Test
    public void testNullDestination() {
        Assertions.assertArrayEquals(null, SendMailPublisher.parseDestination(Json.createObjectBuilder().build()));
    }

    @Test
    public void testMultipleDestinations() {
        JsonObject config = configWithDestination("john@doe.com,steve@jobs.org");
        Assertions.assertArrayEquals(new String[]{"john@doe.com", "steve@jobs.org"},
                SendMailPublisher.parseDestination(config));
    }

    @Test
    public void testEmptyDestinations() {
        JsonObject config = configWithDestination("");
        Assertions.assertArrayEquals(null, SendMailPublisher.parseDestination(config));
    }

    @Test
    @TestTransaction
    public void testSingleTeamAsDestination() {
        final JsonObject config = configWithDestination("");

        final var managedUserId = createManagedUser("managedUserTest", "managedUser@Test.com");
        final var ldapUserId = createLdapUser("ldapUserTest", "ldapUser@Test.com");
        final var oidcUserId = createOidcUser("oidcUserTest", "oidcUser@Test.com");
        final var team = createTeam("foo", List.of(managedUserId), List.of(ldapUserId), List.of(oidcUserId));

        assertThat(publisher.parseDestination(config, List.of(team)))
                .containsExactlyInAnyOrder(
                        "managedUser@Test.com",
                        "ldapUser@Test.com",
                        "oidcUser@Test.com"
                );
    }

    @Test
    @TestTransaction
    public void testMultipleTeamsAsDestination() {
        final JsonObject config = configWithDestination("");

        final var managedUserIdA = createManagedUser("managedUserTest", "managedUser@Test.com");
        final var ldapUserIdA = createLdapUser("ldapUserTest", "ldapUser@Test.com");
        final var oidcUserIdA = createOidcUser("oidcUserTest", "oidcUser@Test.com");
        final var teamA = createTeam("teamA", List.of(managedUserIdA), List.of(ldapUserIdA), List.of(oidcUserIdA));

        final var managedUserIdB = createManagedUser("anotherManagedUserTest", "anotherManagedUser@Test.com");
        final var ldapUserIdB = createLdapUser("anotherLdapUserTest", "anotherLdapUser@Test.com");
        final var oidcUserIdB = createOidcUser("anotherOidcUserTest", "anotherOidcUser@Test.com");
        final var teamB = createTeam("teamA", List.of(managedUserIdB), List.of(ldapUserIdB), List.of(oidcUserIdB));

        assertThat(publisher.parseDestination(config, List.of(teamA, teamB)))
                .containsExactlyInAnyOrder(
                        "managedUser@Test.com",
                        "ldapUser@Test.com",
                        "oidcUser@Test.com",
                        "anotherManagedUser@Test.com",
                        "anotherLdapUser@Test.com",
                        "anotherOidcUser@Test.com"
                );
    }

    @Test
    @TestTransaction
    public void testDuplicateTeamAsDestination() {
        final JsonObject config = configWithDestination("");

        final var managedUserIdA = createManagedUser("managedUserTest", "managedUser@Test.com");
        final var ldapUserIdA = createLdapUser("ldapUserTest", "ldapUser@Test.com");
        final var oidcUserIdA = createOidcUser("oidcUserTest", "oidcUser@Test.com");
        final var teamA = createTeam("teamA", List.of(managedUserIdA), List.of(ldapUserIdA), List.of(oidcUserIdA));

        final var managedUserIdB = createManagedUser("anotherManagedUserTest", "anotherManagedUser@Test.com");
        final var ldapUserIdB = createLdapUser("anotherLdapUserTest", "anotherLdapUser@Test.com");
        final var oidcUserIdB = createOidcUser("anotherOidcUserTest", "anotherOidcUser@Test.com");
        final var teamB = createTeam("teamA",
                List.of(managedUserIdB, managedUserIdA),
                List.of(ldapUserIdB, ldapUserIdA),
                List.of(oidcUserIdB, oidcUserIdA));

        assertThat(publisher.parseDestination(config, List.of(teamA, teamB)))
                .containsExactlyInAnyOrder(
                        "managedUser@Test.com",
                        "ldapUser@Test.com",
                        "oidcUser@Test.com",
                        "anotherManagedUser@Test.com",
                        "anotherLdapUser@Test.com",
                        "anotherOidcUser@Test.com"
                );
    }

    @Test
    @TestTransaction
    public void testDuplicateUserAsDestination() {
        final JsonObject config = configWithDestination("");

        final var managedUserId = createManagedUser("managedUserTest", "managedUser@Test.com");
        final var ldapUserId = createLdapUser("ldapUserTest", "ldapUser@Test.com");
        final var oidcUserId = createOidcUser("oidcUserTest", "oidcUser@Test.com");
        final var team = createTeam("foo", List.of(managedUserId), List.of(ldapUserId), List.of(oidcUserId));

        assertThat(publisher.parseDestination(config, List.of(team, team)))
                .containsExactlyInAnyOrder("managedUser@Test.com", "ldapUser@Test.com", "oidcUser@Test.com");
    }

    @Test
    @TestTransaction
    public void testEmptyTeamAsDestination() {
        final JsonObject config = configWithDestination("");

        final var team = new Team();
        team.setId(666);
        team.setName("foo");

        assertThat(publisher.parseDestination(config, List.of(team))).isNull();
    }

    @Test
    @TestTransaction
    public void testEmptyTeamsAsDestination() {
        final JsonObject config = configWithDestination("");

        assertThat(publisher.parseDestination(config, Collections.emptyList())).isNull();
    }

    @Test
    @TestTransaction
    public void testEmptyUserEmailsAsDestination() {
        final JsonObject config = configWithDestination("");

        final var managedUserId = createManagedUser("managedUserTest", null);
        final var ldapUserId = createLdapUser("ldapUserTest", null);
        final var oidcUserId = createOidcUser("oidcUserTest", null);
        final var team = createTeam("foo", List.of(managedUserId), List.of(ldapUserId), List.of(oidcUserId));

        assertThat(publisher.parseDestination(config, List.of(team))).isNull();
    }

    @Test
    @TestTransaction
    public void testConfigDestinationAndTeamAsDestination() {
        final JsonObject config = configWithDestination("john@doe.com,steve@jobs.org");

        final var managedUserId = createManagedUser("managedUserTest", "managedUser@Test.com");
        final var ldapUserId = createLdapUser("ldapUserTest", "ldapUser@Test.com");
        final var oidcUserId = createOidcUser("oidcUserTest", "john@doe.com");
        final var team = createTeam("foo", List.of(managedUserId), List.of(ldapUserId), List.of(oidcUserId));

        assertThat(publisher.parseDestination(config, List.of(team)))
                .containsExactlyInAnyOrder(
                        "john@doe.com",
                        "steve@jobs.org",
                        "managedUser@Test.com",
                        "ldapUser@Test.com"
                );
    }

    @Test
    @TestTransaction
    public void testNullConfigDestinationAndTeamsDestination() {
        final JsonObject config = Json.createObjectBuilder().build();

        final var managedUserId = createManagedUser("managedUserTest", "managedUser@Test.com");
        final var ldapUserId = createLdapUser("ldapUserTest", "ldapUser@Test.com");
        final var oidcUserId = createOidcUser("oidcUserTest", "john@doe.com");
        final var team = createTeam("foo", List.of(managedUserId), List.of(ldapUserId), List.of(oidcUserId));

        assertThat(publisher.parseDestination(config, List.of(team)))
                .containsExactlyInAnyOrder(
                        "managedUser@Test.com",
                        "ldapUser@Test.com",
                        "john@doe.com"
                );
    }

    @Test
    @TestTransaction
    public void testEmptyManagedUsersAsDestination() {
        final JsonObject config = configWithDestination("john@doe.com,steve@jobs.org");

        final var ldapUserId = createLdapUser("ldapUserTest", "ldapUser@Test.com");
        final var oidcUserId = createOidcUser("oidcUserTest", "oidcUser@Test.com");
        final var team = createTeam("foo", Collections.emptyList(), List.of(ldapUserId), List.of(oidcUserId));

        assertThat(publisher.parseDestination(config, List.of(team)))
                .containsExactlyInAnyOrder(
                        "john@doe.com",
                        "steve@jobs.org",
                        "ldapUser@Test.com",
                        "oidcUser@Test.com"
                );
    }

    @Test
    @TestTransaction
    public void testEmptyLdapUsersAsDestination() {
        final JsonObject config = configWithDestination("john@doe.com,steve@jobs.org");

        final var managedUserId = createManagedUser("managedUserTest", "managedUser@Test.com");
        final var oidcUserId = createOidcUser("oidcUserTest", "oidcUser@Test.com");
        final var team = createTeam("foo", List.of(managedUserId), Collections.emptyList(), List.of(oidcUserId));

        assertThat(publisher.parseDestination(config, List.of(team)))
                .containsExactlyInAnyOrder(
                        "john@doe.com",
                        "steve@jobs.org",
                        "managedUser@Test.com",
                        "oidcUser@Test.com"
                );
    }

    @Test
    @TestTransaction
    public void testEmptyOidcUsersAsDestination() {
        final JsonObject config = configWithDestination("john@doe.com,steve@jobs.org");

        final var managedUserId = createManagedUser("managedUserTest", "managedUser@Test.com");
        final var ldapUserId = createLdapUser("ldapUserTest", "ldapUser@Test.com");
        final var team = createTeam("foo", List.of(managedUserId), List.of(ldapUserId), Collections.emptyList());

        assertThat(publisher.parseDestination(config, List.of(team)))
                .containsExactlyInAnyOrder(
                        "john@doe.com",
                        "steve@jobs.org",
                        "managedUser@Test.com",
                        "ldapUser@Test.com"
                );
    }

    private BigInteger createManagedUser(final String username, final String email) {
        return (BigInteger) entityManager.createNativeQuery("""
                        INSERT INTO "MANAGEDUSER" ("USERNAME", "EMAIL", "PASSWORD", "FORCE_PASSWORD_CHANGE", "LAST_PASSWORD_CHANGE", "NON_EXPIRY_PASSWORD", "SUSPENDED") VALUES
                            (:username, :email, 'password', false, NOW(), true, false)
                        RETURNING "ID";
                        """)
                .setParameter("username", username)
                .setParameter("email", email)
                .getSingleResult();
    }

    private BigInteger createLdapUser(final String username, final String email) {
        return (BigInteger) entityManager.createNativeQuery("""
                        INSERT INTO "LDAPUSER" ("USERNAME", "EMAIL", "DN") VALUES
                            (:username, :email, :dn)
                        RETURNING "ID";
                        """)
                .setParameter("username", username)
                .setParameter("email", email)
                .setParameter("dn", UUID.randomUUID().toString())
                .getSingleResult();
    }

    private BigInteger createOidcUser(final String username, final String email) {
        return (BigInteger) entityManager.createNativeQuery("""
                        INSERT INTO "OIDCUSER" ("USERNAME", "EMAIL") VALUES
                            (:username, :email)
                        RETURNING "ID";
                        """)
                .setParameter("username", username)
                .setParameter("email", email)
                .getSingleResult();
    }

    private Team createTeam(final String name,
                            final Collection<BigInteger> managedUserIds,
                            final Collection<BigInteger> ldapUserIds,
                            final Collection<BigInteger> oidcUserIds) {
        final var teamId = (BigInteger) entityManager.createNativeQuery("""
                        INSERT INTO "TEAM" ("NAME", "UUID") VALUES 
                            (:name, :uuid)
                        RETURNING "ID";
                        """)
                .setParameter("name", name)
                .setParameter("uuid", UUID.randomUUID().toString())
                .getSingleResult();

        if (managedUserIds != null) {
            for (final BigInteger managedUserId : managedUserIds) {
                entityManager.createNativeQuery("""
                                INSERT INTO "MANAGEDUSERS_TEAMS" ("MANAGEDUSER_ID", "TEAM_ID") VALUES 
                                    (:userId, :teamId);
                                """)
                        .setParameter("userId", managedUserId)
                        .setParameter("teamId", teamId)
                        .executeUpdate();
            }
        }

        if (ldapUserIds != null) {
            for (final BigInteger ldapUserId : ldapUserIds) {
                entityManager.createNativeQuery("""
                                INSERT INTO "LDAPUSERS_TEAMS" ("LDAPUSER_ID", "TEAM_ID") VALUES 
                                    (:userId, :teamId);
                                """)
                        .setParameter("userId", ldapUserId)
                        .setParameter("teamId", teamId)
                        .executeUpdate();
            }
        }

        if (oidcUserIds != null) {
            for (final BigInteger oidcUserId : oidcUserIds) {
                entityManager.createNativeQuery("""
                                INSERT INTO "OIDCUSERS_TEAMS" ("OIDCUSERS_ID", "TEAM_ID") VALUES 
                                    (:userId, :teamId);
                                """)
                        .setParameter("userId", oidcUserId)
                        .setParameter("teamId", teamId)
                        .executeUpdate();
            }
        }

        final var team = new Team();
        team.setId(teamId.longValue());
        team.setName(name);
        return team;
    }

    private static JsonObject configWithDestination(final String destination) {
        return Json.createObjectBuilder().add("destination", destination).build();
    }

}
