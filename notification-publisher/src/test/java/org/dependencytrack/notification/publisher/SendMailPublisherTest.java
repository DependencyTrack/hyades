/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.notification.publisher;

import com.google.protobuf.util.Timestamps;
import io.quarkiverse.mailpit.test.InjectMailbox;
import io.quarkiverse.mailpit.test.Mailbox;
import io.quarkiverse.mailpit.test.WithMailbox;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.dependencytrack.notification.NotificationConstants;
import org.dependencytrack.persistence.model.Team;
import org.dependencytrack.proto.notification.v1.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_ANALYZER;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_SYSTEM;

@QuarkusTest
@WithMailbox
@TestProfile(SendMailPublisherTest.TestProfile.class)
public class SendMailPublisherTest extends AbstractPublisherTest<SendMailPublisher> {

    public static class TestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.ofEntries(
                    Map.entry("dtrack.general.base.url", "https://example.com"),
                    Map.entry("dtrack.email.smtp.enabled", "true"),
                    Map.entry("dtrack.email.smtp.server.hostname", "localhost"),
                    Map.entry("dtrack.email.smtp.server.port", "${mailpit.smtp.port}"),
                    Map.entry("dtrack.email.smtp.from.address", "dtrack@example.com"),
                    Map.entry("dtrack.email.subject.prefix", "[Dependency-Track]")
            );
        }

    }

    @InjectMailbox
    Mailbox mailbox;

    @AfterEach
    void afterEach() {
        mailbox.clear();
    }

    @Override
    JsonObjectBuilder extraConfig() {
        return super.extraConfig()
                .add(Publisher.CONFIG_DESTINATION, "recipient@example.com");
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomConsumedNotification() throws Exception {
        super.testInformWithBomConsumedNotification();

        assertThat(mailbox.findFirst("recipient@example.com")).satisfies(message -> {
            assertThat(message.getFrom()).isNotNull();
            assertThat(message.getFrom().getAddress()).isEqualTo("dtrack@example.com");
            assertThat(message.getSubject()).isEqualTo("[Dependency-Track] Bill of Materials Consumed");
            assertThat(message.getText()).isEqualToIgnoringNewLines("""
                    Bill of Materials Consumed
                                        
                    --------------------------------------------------------------------------------
                                        
                    Project:           projectName
                    Version:           projectVersion
                    Description:       projectDescription
                    Project URL:       https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95
                                        
                    --------------------------------------------------------------------------------
                                        
                    A CycloneDX BOM was consumed and will be processed
                                        
                    --------------------------------------------------------------------------------
                                        
                    1970-01-01T18:31:06.000Z
                    """);
        });
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotification() throws Exception {
        super.testInformWithBomProcessingFailedNotification();

        assertThat(mailbox.findFirst("recipient@example.com")).satisfies(message -> {
            assertThat(message.getFrom()).isNotNull();
            assertThat(message.getFrom().getAddress()).isEqualTo("dtrack@example.com");
            assertThat(message.getSubject()).isEqualTo("[Dependency-Track] Bill of Materials Processing Failed");
            assertThat(message.getText()).isEqualToIgnoringNewLines("""
                    Bill of Materials Processing Failed
                                        
                    --------------------------------------------------------------------------------
                                        
                    Project:           projectName
                    Version:           projectVersion
                    Description:       projectDescription
                    Project URL:       https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95
                                        
                    --------------------------------------------------------------------------------
                                        
                    Cause:
                    cause
                                        
                    --------------------------------------------------------------------------------
                                        
                    An error occurred while processing a BOM
                                        
                    --------------------------------------------------------------------------------
                                        
                    1970-01-01T18:31:06.000Z
                    """);
        });
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject() throws Exception {
        super.testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject();

        assertThat(mailbox.findFirst("recipient@example.com")).satisfies(message -> {
            assertThat(message.getFrom()).isNotNull();
            assertThat(message.getFrom().getAddress()).isEqualTo("dtrack@example.com");
            assertThat(message.getSubject()).isEqualTo("[Dependency-Track] Bill of Materials Processing Failed");
            assertThat(message.getText()).isEqualToIgnoringNewLines("""
                    Bill of Materials Processing Failed
                                        
                    --------------------------------------------------------------------------------
                                        
                    Project:           projectName
                    Version:           projectVersion
                    Description:       projectDescription
                    Project URL:       https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95
                                        
                    --------------------------------------------------------------------------------
                                        
                    Cause:
                    cause
                                        
                    --------------------------------------------------------------------------------
                                        
                    An error occurred while processing a BOM
                                        
                    --------------------------------------------------------------------------------
                                        
                    1970-01-01T18:31:06.000Z
                    """);
        });
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithBomValidationFailedNotificationSubject() throws Exception {
        super.testInformWithBomValidationFailedNotificationSubject();

        assertThat(mailbox.findFirst("recipient@example.com")).satisfies(message -> {
            assertThat(message.getFrom()).isNotNull();
            assertThat(message.getFrom().getAddress()).isEqualTo("dtrack@example.com");
            assertThat(message.getSubject()).isEqualTo("[Dependency-Track] Bill of Materials Validation Failed");
            assertThat(message.getText()).isEqualToIgnoringNewLines("""
                    Bill of Materials Validation Failed
                                        
                    --------------------------------------------------------------------------------
                                        
                    Project:           projectName
                    Version:           projectVersion
                    Description:       projectDescription
                    Project URL:       https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95
                                        
                    --------------------------------------------------------------------------------
                                        
                    Errors:
                    cause 1
                    cause 2
                                        
                    --------------------------------------------------------------------------------
                                        
                    An error occurred while validating a BOM
                                        
                    --------------------------------------------------------------------------------
                                        
                    1970-01-01T18:31:06.000Z
                    """);
        });
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithDataSourceMirroringNotification() throws Exception {
        super.testInformWithDataSourceMirroringNotification();

        assertThat(mailbox.findFirst("recipient@example.com")).satisfies(message -> {
            assertThat(message.getFrom()).isNotNull();
            assertThat(message.getFrom().getAddress()).isEqualTo("dtrack@example.com");
            assertThat(message.getSubject()).isEqualTo("[Dependency-Track] GitHub Advisory Mirroring");
            assertThat(message.getText()).isEqualToIgnoringNewLines("""
                    GitHub Advisory Mirroring
                                               
                    --------------------------------------------------------------------------------
                                        
                    Level:     LEVEL_ERROR
                    Scope:     SCOPE_SYSTEM
                    Group:     GROUP_DATASOURCE_MIRRORING
                                        
                    --------------------------------------------------------------------------------
                                        
                    An error occurred mirroring the contents of GitHub Advisories. Check log for details.
                                        
                    --------------------------------------------------------------------------------
                                        
                    1970-01-01T18:31:06.000Z
                    """);
        });
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithNewVulnerabilityNotification() throws Exception {
        super.testInformWithNewVulnerabilityNotification();

        assertThat(mailbox.findFirst("recipient@example.com")).satisfies(message -> {
            assertThat(message.getFrom()).isNotNull();
            assertThat(message.getFrom().getAddress()).isEqualTo("dtrack@example.com");
            assertThat(message.getSubject()).isEqualTo("[Dependency-Track] New Vulnerability Identified");
            assertThat(message.getText()).isEqualToIgnoringNewLines("""
                    New Vulnerability Identified
                                        
                    --------------------------------------------------------------------------------
                                        
                    Vulnerability ID:  INT-001
                    Vulnerability URL: https://example.com/vulnerability/?source=INTERNAL&vulnId=INT-001
                    Severity:          MEDIUM
                    Source:            INTERNAL
                    Component:         componentName : componentVersion
                    Component URL:     https://example.com/component/?uuid=94f87321-a5d1-4c2f-b2fe-95165debebc6
                    Project:           projectName
                    Version:           projectVersion
                    Description:       projectDescription
                    Project URL:       https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95
                                        
                    --------------------------------------------------------------------------------
                                        
                    Other affected projects: https://example.com/vulnerabilities/INTERNAL/INT-001/affectedProjects
                                        
                    --------------------------------------------------------------------------------
                                        

                                        
                    --------------------------------------------------------------------------------

                    1970-01-01T18:31:06.000Z
                    """);
        });
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithProjectAuditChangeNotification() throws Exception {
        super.testInformWithProjectAuditChangeNotification();

        assertThat(mailbox.findFirst("recipient@example.com")).satisfies(message -> {
            assertThat(message.getFrom()).isNotNull();
            assertThat(message.getFrom().getAddress()).isEqualTo("dtrack@example.com");
            assertThat(message.getSubject()).isEqualTo("[Dependency-Track] Analysis Decision: Finding Suppressed");
            assertThat(message.getText()).isEqualToIgnoringNewLines("""
                    Analysis Decision: Finding Suppressed
                                        
                    --------------------------------------------------------------------------------
                                        
                    Analysis Type:  Project Analysis
                                        
                    Analysis State:    FALSE_POSITIVE
                    Suppressed:        true
                    Vulnerability ID:  INT-001
                    Vulnerability URL: https://example.com/vulnerability/?source=INTERNAL&vulnId=INT-001
                    Severity:          MEDIUM
                    Source:            INTERNAL
                                        
                    Component:         componentName : componentVersion
                    Component URL:     https://example.com/component/?uuid=94f87321-a5d1-4c2f-b2fe-95165debebc6
                    Project:           pkg:maven/org.acme/projectName@projectVersion
                    Description:       projectDescription
                    Project URL:       https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95
                                        
                    --------------------------------------------------------------------------------
                                        
                                        
                                        
                    --------------------------------------------------------------------------------
                                        
                    1970-01-01T18:31:06.000Z
                    """);
        });
    }

    @Test
    @Override
    @TestTransaction
    void testInformWithTemplateInclude() throws Exception {
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_SYSTEM)
                .setGroup(GROUP_ANALYZER)
                .setTitle(NotificationConstants.Title.NOTIFICATION_TEST)
                .setLevel(LEVEL_ERROR)
                .setTimestamp(Timestamps.fromSeconds(66666))
                .build();

        final JsonObject config = Json.createObjectBuilder(createConfig())
                .add(Publisher.CONFIG_TEMPLATE_KEY, "{% include '/some/path' %}")
                .build();

        // NB: In contrast to other publishers, SendMailPublisher catches and logs
        // failures during template evaluation. Instead of expecting an exception
        // being thrown, we verify that no email was sent.
        assertThatNoException()
                .isThrownBy(() -> publisherInstance.inform(createPublishContext(notification), notification, config));

        assertThat(mailbox.findFirst("recipient@example.com")).isNull();
    }

    @Test
    void testSingleDestination() {
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
        final var team = createTeam("foo", List.of(managedUserId, ldapUserId, oidcUserId));

        assertThat(publisherInstance.parseDestination(config, List.of(team)))
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
        final var teamA = createTeam("teamA", List.of(managedUserIdA, ldapUserIdA, oidcUserIdA));

        final var managedUserIdB = createManagedUser("anotherManagedUserTest", "anotherManagedUser@Test.com");
        final var ldapUserIdB = createLdapUser("anotherLdapUserTest", "anotherLdapUser@Test.com");
        final var oidcUserIdB = createOidcUser("anotherOidcUserTest", "anotherOidcUser@Test.com");
        final var teamB = createTeam("teamB", List.of(managedUserIdB, ldapUserIdB, oidcUserIdB));

        assertThat(publisherInstance.parseDestination(config, List.of(teamA, teamB)))
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
        final var teamA = createTeam("teamA", List.of(managedUserIdA, ldapUserIdA, oidcUserIdA));

        final var managedUserIdB = createManagedUser("anotherManagedUserTest", "anotherManagedUser@Test.com");
        final var ldapUserIdB = createLdapUser("anotherLdapUserTest", "anotherLdapUser@Test.com");
        final var oidcUserIdB = createOidcUser("anotherOidcUserTest", "anotherOidcUser@Test.com");
        final var teamB = createTeam("teamB",
                List.of(managedUserIdB, managedUserIdA, ldapUserIdB, ldapUserIdA, oidcUserIdB, oidcUserIdA));

        assertThat(publisherInstance.parseDestination(config, List.of(teamA, teamB)))
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
        final var team = createTeam("foo", List.of(managedUserId, ldapUserId, oidcUserId));

        assertThat(publisherInstance.parseDestination(config, List.of(team, team)))
                .containsExactlyInAnyOrder("managedUser@Test.com", "ldapUser@Test.com", "oidcUser@Test.com");
    }

    @Test
    @TestTransaction
    public void testEmptyTeamAsDestination() {
        final JsonObject config = configWithDestination("");

        final var team = new Team();
        team.setId(666);
        team.setName("foo");

        assertThat(publisherInstance.parseDestination(config, List.of(team))).isNull();
    }

    @Test
    @TestTransaction
    public void testEmptyTeamsAsDestination() {
        final JsonObject config = configWithDestination("");

        assertThat(publisherInstance.parseDestination(config, Collections.emptyList())).isNull();
    }

    @Test
    @TestTransaction
    public void testEmptyUserEmailsAsDestination() {
        final JsonObject config = configWithDestination("");

        final var managedUserId = createManagedUser("managedUserTest", null);
        final var ldapUserId = createLdapUser("ldapUserTest", null);
        final var oidcUserId = createOidcUser("oidcUserTest", null);
        final var team = createTeam("foo", List.of(managedUserId, ldapUserId, oidcUserId));

        assertThat(publisherInstance.parseDestination(config, List.of(team))).isNull();
    }

    @Test
    @TestTransaction
    public void testConfigDestinationAndTeamAsDestination() {
        final JsonObject config = configWithDestination("john@doe.com,steve@jobs.org");

        final var managedUserId = createManagedUser("managedUserTest", "managedUser@Test.com");
        final var ldapUserId = createLdapUser("ldapUserTest", "ldapUser@Test.com");
        final var oidcUserId = createOidcUser("oidcUserTest", "john@doe.com");
        final var team = createTeam("foo", List.of(managedUserId, ldapUserId, oidcUserId));

        assertThat(publisherInstance.parseDestination(config, List.of(team)))
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
        final var team = createTeam("foo", List.of(managedUserId, ldapUserId, oidcUserId));

        assertThat(publisherInstance.parseDestination(config, List.of(team)))
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
        final var team = createTeam("foo", List.of(ldapUserId, oidcUserId));

        assertThat(publisherInstance.parseDestination(config, List.of(team)))
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
        final var team = createTeam("foo", List.of(managedUserId, oidcUserId));

        assertThat(publisherInstance.parseDestination(config, List.of(team)))
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
        final var team = createTeam("foo", List.of(managedUserId, ldapUserId));

        assertThat(publisherInstance.parseDestination(config, List.of(team)))
                .containsExactlyInAnyOrder(
                        "john@doe.com",
                        "steve@jobs.org",
                        "managedUser@Test.com",
                        "ldapUser@Test.com"
                );
    }

    @Test
    @Override
    @TestTransaction
    public void testInformWithNewVulnerableDependencyNotification() throws Exception {
        super.testInformWithNewVulnerableDependencyNotification();

        assertThat(mailbox.findFirst("recipient@example.com")).satisfies(message -> {
            assertThat(message.getFrom()).isNotNull();
            assertThat(message.getFrom().getAddress()).isEqualTo("dtrack@example.com");
            assertThat(message.getSubject()).isEqualTo("[Dependency-Track] Vulnerable Dependency Introduced");
            assertThat(message.getText()).isEqualToIgnoringNewLines("""
                    Vulnerable Dependency Introduced
                                        
                    --------------------------------------------------------------------------------
                                        
                    Project:           pkg:maven/org.acme/projectName@projectVersion
                    Project URL:       https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95
                    Component:         componentName : componentVersion
                    Component URL:     https://example.com/component/?uuid=94f87321-a5d1-4c2f-b2fe-95165debebc6
                                        
                    Vulnerabilities
                                        
                    Vulnerability ID:  INT-001
                    Vulnerability URL: https://example.com/vulnerability/?source=INTERNAL&vulnId=INT-001
                    Severity:          MEDIUM
                    Source:            INTERNAL
                    Description:
                    vulnerabilityDescription
                                        
                                        
                                        
                    --------------------------------------------------------------------------------
                                        
                                        
                                        
                    --------------------------------------------------------------------------------
                                        
                    1970-01-01T18:31:06.000Z
                    """);
        });
    }

    private Long createManagedUser(final String username, final String email) {
        return (Long) entityManager.createNativeQuery("""
                        INSERT INTO "USER" ("TYPE", "USERNAME", "EMAIL", "PASSWORD", "FORCE_PASSWORD_CHANGE", "LAST_PASSWORD_CHANGE", "NON_EXPIRY_PASSWORD", "SUSPENDED") VALUES
                            ('MANAGED', :username, :email, 'password', FALSE, NOW(), TRUE, FALSE)
                        RETURNING "ID";
                        """)
                .setParameter("username", username)
                .setParameter("email", email)
                .getSingleResult();
    }

    private Long createLdapUser(final String username, final String email) {
        return (Long) entityManager.createNativeQuery("""
                        INSERT INTO "USER" ("TYPE", "USERNAME", "EMAIL", "DN") VALUES
                            ('LDAP', :username, :email, :dn)
                        RETURNING "ID";
                        """)
                .setParameter("username", username)
                .setParameter("email", email)
                .setParameter("dn", UUID.randomUUID().toString())
                .getSingleResult();
    }

    private Long createOidcUser(final String username, final String email) {
        return (Long) entityManager.createNativeQuery("""
                        INSERT INTO "USER" ("TYPE", "USERNAME", "EMAIL") VALUES
                            ('OIDC', :username, :email)
                        RETURNING "ID";
                        """)
                .setParameter("username", username)
                .setParameter("email", email)
                .getSingleResult();
    }

    private Team createTeam(final String name, final Collection<Long> userIds) {
        final var teamId = (Long) entityManager.createNativeQuery("""
                        INSERT INTO "TEAM" ("NAME", "UUID") VALUES
                            (:name, :uuid)
                        RETURNING "ID";
                        """)
                .setParameter("name", name)
                .setParameter("uuid", UUID.randomUUID().toString())
                .getSingleResult();

        if (userIds != null) {
            for (final Long managedUserId : userIds) {
                entityManager.createNativeQuery("""
                                INSERT INTO "USERS_TEAMS" ("USER_ID", "TEAM_ID") VALUES
                                    (:userId, :teamId);
                                """)
                        .setParameter("userId", managedUserId)
                        .setParameter("teamId", teamId)
                        .executeUpdate();
            }
        }

        final var team = new Team();
        team.setId(teamId);
        team.setName(name);
        return team;
    }

    private static JsonObject configWithDestination(final String destination) {
        return Json.createObjectBuilder().add("destination", destination).build();
    }
}
