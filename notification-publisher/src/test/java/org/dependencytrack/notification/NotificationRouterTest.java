package org.dependencytrack.notification;

import com.google.protobuf.Any;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.dependencytrack.notification.publisher.ConsolePublisher;
import org.dependencytrack.notification.publisher.PublisherTestUtil;
import org.dependencytrack.persistence.model.NotificationGroup;
import org.dependencytrack.persistence.model.NotificationLevel;
import org.dependencytrack.persistence.model.NotificationRule;
import org.dependencytrack.persistence.model.NotificationScope;
import org.dependencytrack.proto.notification.v1.BackReference;
import org.dependencytrack.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.BomProcessingFailedSubject;
import org.dependencytrack.proto.notification.v1.Component;
import org.dependencytrack.proto.notification.v1.Level;
import org.dependencytrack.proto.notification.v1.NewVulnerabilitySubject;
import org.dependencytrack.proto.notification.v1.NewVulnerableDependencySubject;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.proto.notification.v1.PolicyViolationAnalysisDecisionChangeSubject;
import org.dependencytrack.proto.notification.v1.PolicyViolationSubject;
import org.dependencytrack.proto.notification.v1.Project;
import org.dependencytrack.proto.notification.v1.VexConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.Vulnerability;
import org.dependencytrack.proto.notification.v1.VulnerabilityAnalysisDecisionChangeSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_CONSUMED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_PROCESSED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_BOM_PROCESSING_FAILED;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_NEW_VULNERABILITY;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_NEW_VULNERABLE_DEPENDENCY;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_POLICY_VIOLATION;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_PROJECT_AUDIT_CHANGE;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_VEX_CONSUMED;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_PORTFOLIO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@QuarkusTest
class NotificationRouterTest {

    @Inject
    EntityManager entityManager;

    @Inject
    NotificationRouter notificationRouter;

    private ConsolePublisher consolePublisherMock;

    @BeforeEach
    void setUp() {
        consolePublisherMock = Mockito.mock(ConsolePublisher.class);
        QuarkusMock.installMockForType(consolePublisherMock, ConsolePublisher.class);
    }

    @Test
    @TestTransaction
    void testResolveRulesWithNullNotification() throws Exception {
        Assertions.assertThat(notificationRouter.resolveRules(null, null)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesWithInvalidNotification() throws Exception {
        Assertions.assertThat(notificationRouter.resolveRules(null, Notification.newBuilder().build())).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesWithNoRules() throws Exception {
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setLevel(LEVEL_INFORMATIONAL)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .build();
        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesWithValidMatchingRule() throws Exception {
        final Long publisherId = createConsolePublisher();
        // Creates a new rule and defines when the rule should be triggered (notifyOn)
        createRule("Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABILITY, publisherId);
        // Creates a new notification
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder().build()))
                .build();
        // Ok, let's test this
        final List<NotificationRule> rules = notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification);
        assertThat(rules).satisfiesExactly(
                rule -> assertThat(rule.getName()).isEqualTo("Test Rule")
        );
    }

    @Test
    @TestTransaction
    void testResolveRulesWithValidMatchingProjectLimitRule() throws Exception {
        final Long publisherId = createConsolePublisher();
        // Creates a new rule and defines when the rule should be triggered (notifyOn)
        final Long ruleId = createRule("Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABILITY, publisherId);
        // Creates a project which will later be matched on
        final UUID projectUuid = UUID.randomUUID();
        final Long projectId = createProject("Test Project", "1.0", true, projectUuid);
        addProjectToRule(projectId, ruleId);
        // Creates a new notification
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuid.toString()))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setAffectedProjectsReference(BackReference.newBuilder()
                                .setApiUri("foo")
                                .setFrontendUri("bar"))
                        .addAffectedProjects(Project.newBuilder()
                                .setUuid(projectUuid.toString()))
                        .build()))
                .build();
        // Ok, let's test this
        final List<NotificationRule> rules = notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification);
        assertThat(rules).satisfiesExactly(
                rule -> assertThat(rule.getName()).isEqualTo("Test Rule")
        );
    }

    @Test
    @TestTransaction
    void testResolveRulesWithValidNonMatchingProjectLimitRule() throws Exception {
        final Long publisherId = createConsolePublisher();
        // Creates a new rule and defines when the rule should be triggered (notifyOn)
        final Long ruleId = createRule("Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABILITY, publisherId);
        // Creates a project which will later be matched on
        final UUID projectUuid = UUID.randomUUID();
        final Long projectId = createProject("Test Project", "1.0", true, projectUuid);
        addProjectToRule(projectId, ruleId);
        // Creates a new notification
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setAffectedProjectsReference(BackReference.newBuilder()
                                .setApiUri("foo")
                                .setFrontendUri("bar"))
                        .addAffectedProjects(Project.newBuilder()
                                .setUuid(projectUuid.toString()))
                        .build()))
                .build();
        // Ok, let's test this
        final List<NotificationRule> rules = notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification);
        assertThat(rules).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesWithValidNonMatchingRule() throws Exception {
        final Long publisherId = createConsolePublisher();
        // Creates a new rule and defines when the rule should be triggered (notifyOn)
        final Long ruleId = createRule("Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.PROJECT_AUDIT_CHANGE, publisherId);
        // Creates a project which will later be matched on
        final UUID projectUuid = UUID.randomUUID();
        final Long projectId = createProject("Test Project", "1.0", true, projectUuid);
        addProjectToRule(projectId, ruleId);
        // Creates a new notification
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuid.toString()))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setAffectedProjectsReference(BackReference.newBuilder()
                                .setApiUri("foo")
                                .setFrontendUri("bar"))
                        .addAffectedProjects(Project.newBuilder()
                                .setUuid(projectUuid.toString()))
                        .build()))
                .build();
        // Ok, let's test this
        final List<NotificationRule> rules = notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification);
        assertThat(rules).isEmpty();
    }

    @TestTransaction
    @ParameterizedTest
    @CsvSource(value = {
            "WARNING, LEVEL_WARNING, true", // Levels are equal
            "WARNING, LEVEL_ERROR, true", // Rule level is below
            "WARNING, LEVEL_INFORMATIONAL, false" // Rule level is above
    })
    void testResolveRulesLevels(final NotificationLevel ruleLevel, final Level notificationLevel,
                                final boolean expectedMatch) throws Exception {
        final Long publisherId = createConsolePublisher();
        createRule("Test Levels Rule",
                NotificationScope.PORTFOLIO, ruleLevel,
                NotificationGroup.BOM_PROCESSED, publisherId);

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_BOM_PROCESSED)
                .setLevel(notificationLevel)
                .build();

        if (expectedMatch) {
            Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification)).satisfiesExactly(
                    rule -> Assertions.assertThat(rule.getName()).isEqualTo("Test Levels Rule")
            );
        } else {
            Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification)).isEmpty();
        }
    }

    @Test
    @TestTransaction
    void testResolveRulesWithDisabledRule() throws Exception {
        final Long publisherId = createConsolePublisher();
        final Long ruleId = createRule("Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABILITY, publisherId);
        disableRule(ruleId);

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesLimitedToProjectForNewVulnerabilityNotification() throws Exception {
        final UUID projectUuidA = UUID.randomUUID();
        final Long projectIdA = createProject("Project A", "1.0", true, projectUuidA);

        final UUID projectUuidB = UUID.randomUUID();
        createProject("Project B", "2.0", true, projectUuidB);

        final Long publisherId = createConsolePublisher();
        final Long ruleId = createRule("Limit To Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABILITY, publisherId);
        addProjectToRule(projectIdA, ruleId);

        final var notificationProjectA = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidA.toString()))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectA), notificationProjectA)).satisfiesExactly(
                rule -> Assertions.assertThat(rule.getName()).isEqualTo("Limit To Test Rule")
        );

        final var notificationProjectB = Notification.newBuilder(notificationProjectA)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidB.toString()))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectB), notificationProjectB)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesLimitedToProjectForNewVulnerableDependencyNotification() throws Exception {
        final UUID projectUuidA = UUID.randomUUID();
        final Long projectIdA = createProject("Project A", "1.0", true, projectUuidA);

        final UUID projectUuidB = UUID.randomUUID();
        createProject("Project B", "2.0", true, projectUuidB);

        final Long publisherId = createConsolePublisher();
        final Long ruleId = createRule("Limit To Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABLE_DEPENDENCY, publisherId);
        addProjectToRule(projectIdA, ruleId);

        final var notificationProjectA = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABLE_DEPENDENCY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerableDependencySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidA.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectA), notificationProjectA)).satisfiesExactly(
                rule -> Assertions.assertThat(rule.getName()).isEqualTo("Limit To Test Rule")
        );

        final var notificationProjectB = Notification.newBuilder(notificationProjectA)
                .setSubject(Any.pack(NewVulnerableDependencySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidB.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectB), notificationProjectB)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesLimitedToProjectForBomConsumedOrProcessedNotification() throws Exception {
        final UUID projectUuidA = UUID.randomUUID();
        final Long projectIdA = createProject("Project A", "1.0", true, projectUuidA);

        final UUID projectUuidB = UUID.randomUUID();
        createProject("Project B", "2.0", true, projectUuidB);

        final Long publisherId = createConsolePublisher();
        final Long ruleId = createRule("Limit To Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.BOM_CONSUMED, publisherId);
        addProjectToRule(projectIdA, ruleId);

        final var notificationProjectA = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_BOM_CONSUMED)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(BomConsumedOrProcessedSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidA.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectA), notificationProjectA)).satisfiesExactly(
                rule -> Assertions.assertThat(rule.getName()).isEqualTo("Limit To Test Rule")
        );

        final var notificationProjectB = Notification.newBuilder(notificationProjectA)
                .setSubject(Any.pack(BomConsumedOrProcessedSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidB.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectB), notificationProjectB)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesLimitedToProjectForVexConsumedOrProcessedNotification() throws Exception {
        final UUID projectUuidA = UUID.randomUUID();
        final Long projectIdA = createProject("Project A", "1.0", true, projectUuidA);

        final UUID projectUuidB = UUID.randomUUID();
        createProject("Project B", "2.0", true, projectUuidB);

        final Long publisherId = createConsolePublisher();
        final Long ruleId = createRule("Limit To Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.VEX_CONSUMED, publisherId);
        addProjectToRule(projectIdA, ruleId);

        final var notificationProjectA = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_VEX_CONSUMED)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(VexConsumedOrProcessedSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidA.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectA), notificationProjectA)).satisfiesExactly(
                rule -> Assertions.assertThat(rule.getName()).isEqualTo("Limit To Test Rule")
        );

        final var notificationProjectB = Notification.newBuilder(notificationProjectA)
                .setSubject(Any.pack(VexConsumedOrProcessedSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidB.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectB), notificationProjectB)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesLimitedToProjectForPolicyViolationNotification() throws Exception {
        final UUID projectUuidA = UUID.randomUUID();
        final Long projectIdA = createProject("Project A", "1.0", true, projectUuidA);

        final UUID projectUuidB = UUID.randomUUID();
        createProject("Project B", "2.0", true, projectUuidB);

        final Long publisherId = createConsolePublisher();
        final Long ruleId = createRule("Limit To Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.POLICY_VIOLATION, publisherId);
        addProjectToRule(projectIdA, ruleId);

        final var notificationProjectA = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_POLICY_VIOLATION)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(PolicyViolationSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidA.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectA), notificationProjectA)).satisfiesExactly(
                rule -> Assertions.assertThat(rule.getName()).isEqualTo("Limit To Test Rule")
        );

        final var notificationProjectB = Notification.newBuilder(notificationProjectA)
                .setSubject(Any.pack(PolicyViolationSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidB.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectB), notificationProjectB)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesLimitedToProjectForVulnerabilityAnalysisDecisionChangeNotification() throws Exception {
        final UUID projectUuidA = UUID.randomUUID();
        final Long projectIdA = createProject("Project A", "1.0", true, projectUuidA);

        final UUID projectUuidB = UUID.randomUUID();
        createProject("Project B", "2.0", true, projectUuidB);

        final Long publisherId = createConsolePublisher();
        final Long ruleId = createRule("Limit To Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.PROJECT_AUDIT_CHANGE, publisherId);
        addProjectToRule(projectIdA, ruleId);

        final var notificationProjectA = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_PROJECT_AUDIT_CHANGE)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(VulnerabilityAnalysisDecisionChangeSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidA.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectA), notificationProjectA)).satisfiesExactly(
                rule -> Assertions.assertThat(rule.getName()).isEqualTo("Limit To Test Rule")
        );

        final var notificationProjectB = Notification.newBuilder(notificationProjectA)
                .setSubject(Any.pack(VulnerabilityAnalysisDecisionChangeSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidB.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectB), notificationProjectB)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesLimitedToProjectForPolicyViolationAnalysisDecisionChangeNotification() throws Exception {
        final UUID projectUuidA = UUID.randomUUID();
        final Long projectIdA = createProject("Project A", "1.0", true, projectUuidA);

        final UUID projectUuidB = UUID.randomUUID();
        createProject("Project B", "2.0", true, projectUuidB);

        final Long publisherId = createConsolePublisher();
        final Long ruleId = createRule("Limit To Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.PROJECT_AUDIT_CHANGE, publisherId);
        addProjectToRule(projectIdA, ruleId);

        final var notificationProjectA = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_PROJECT_AUDIT_CHANGE)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(PolicyViolationAnalysisDecisionChangeSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidA.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectA), notificationProjectA)).satisfiesExactly(
                rule -> Assertions.assertThat(rule.getName()).isEqualTo("Limit To Test Rule")
        );

        final var notificationProjectB = Notification.newBuilder(notificationProjectA)
                .setSubject(Any.pack(PolicyViolationAnalysisDecisionChangeSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidB.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectB), notificationProjectB)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesLimitedToProjectForBomProcessingFailedNotification() throws Exception {
        final UUID projectUuidA = UUID.randomUUID();
        final Long projectIdA = createProject("Project A", "1.0", true, projectUuidA);

        final UUID projectUuidB = UUID.randomUUID();
        createProject("Project B", "2.0", true, projectUuidB);

        final Long publisherId = createConsolePublisher();
        final Long ruleId = createRule("Limit To Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.BOM_PROCESSING_FAILED, publisherId);
        addProjectToRule(projectIdA, ruleId);

        final var notificationProjectA = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_BOM_PROCESSING_FAILED)
                .setLevel(LEVEL_ERROR)
                .setSubject(Any.pack(BomProcessingFailedSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidA.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectA), notificationProjectA)).satisfiesExactly(
                rule -> Assertions.assertThat(rule.getName()).isEqualTo("Limit To Test Rule")
        );

        final var notificationProjectB = Notification.newBuilder(notificationProjectA)
                .setSubject(Any.pack(BomProcessingFailedSubject.newBuilder()
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuidB.toString()))
                        .build()))
                .build();

        Assertions.assertThat(notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notificationProjectB), notificationProjectB)).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesWithAffectedChild() throws Exception {
        final Long publisherId = createConsolePublisher();
        // Creates a new rule and defines when the rule should be triggered (notifyOn)
        final Long ruleId = createRule("Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABILITY, publisherId);
        setNotifyChildren(ruleId, true);
        // Creates a project which will later be matched on
        final UUID grandParentUuid = UUID.randomUUID();
        final Long grandParentProjectId = createProject("Test Project Grandparent", "1.0", true, grandParentUuid);
        final UUID parentUuid = UUID.randomUUID();
        final Long parentProjectId = createProject("Test Project Parent", "1.0", true, parentUuid);
        setProjectParent(parentProjectId, grandParentProjectId);
        final UUID childUuid = UUID.randomUUID();
        final Long childProjectId = createProject("Test Project Child", "1.0", true, childUuid);
        setProjectParent(childProjectId, parentProjectId);
        final UUID grandChildUuid = UUID.randomUUID();
        final Long grandChildProjectId = createProject("Test Project Grandchild", "1.0", true, grandChildUuid);
        setProjectParent(grandChildProjectId, childProjectId);
        addProjectToRule(grandParentProjectId, ruleId);
        // Creates a new notification
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(grandChildUuid.toString()))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setAffectedProjectsReference(BackReference.newBuilder()
                                .setApiUri("foo")
                                .setFrontendUri("bar"))
                        .addAffectedProjects(Project.newBuilder()
                                .setUuid(grandChildUuid.toString()))
                        .build()))
                .build();
        // Ok, let's test this
        final List<NotificationRule> rules = notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification);
        assertThat(rules).satisfiesExactly(
                rule -> assertThat(rule.getName()).isEqualTo("Test Rule")
        );
    }

    @Test
    @TestTransaction
    void testResolveRulesWithAffectedChildAndNotifyChildrenDisabled() throws Exception {
        final Long publisherId = createConsolePublisher();
        // Creates a new rule and defines when the rule should be triggered (notifyOn)
        final Long ruleId = createRule("Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABILITY, publisherId);
        setNotifyChildren(ruleId, false);
        // Creates a project which will later be matched on
        final UUID grandParentUuid = UUID.randomUUID();
        final Long grandParentProjectId = createProject("Test Project Grandparent", "1.0", true, grandParentUuid);
        final UUID parentUuid = UUID.randomUUID();
        final Long parentProjectId = createProject("Test Project Parent", "1.0", true, parentUuid);
        setProjectParent(parentProjectId, grandParentProjectId);
        final UUID childUuid = UUID.randomUUID();
        final Long childProjectId = createProject("Test Project Child", "1.0", true, childUuid);
        setProjectParent(childProjectId, parentProjectId);
        final UUID grandChildUuid = UUID.randomUUID();
        final Long grandChildProjectId = createProject("Test Project Grandchild", "1.0", true, grandChildUuid);
        setProjectParent(grandChildProjectId, childProjectId);
        addProjectToRule(grandParentProjectId, ruleId);
        // Creates a new notification
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(grandChildUuid.toString()))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setAffectedProjectsReference(BackReference.newBuilder()
                                .setApiUri("foo")
                                .setFrontendUri("bar"))
                        .addAffectedProjects(Project.newBuilder()
                                .setUuid(grandChildUuid.toString()))
                        .build()))
                .build();
        // Ok, let's test this
        final List<NotificationRule> rules = notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification);
        assertThat(rules).isEmpty();
    }

    @Test
    @TestTransaction
    void testResolveRulesWithAffectedChildAndInactiveChild() throws Exception {
        final Long publisherId = createConsolePublisher();
        // Creates a new rule and defines when the rule should be triggered (notifyOn)
        final Long ruleId = createRule("Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABILITY, publisherId);
        setNotifyChildren(ruleId, true);
        // Creates a project which will later be matched on
        final UUID grandParentUuid = UUID.randomUUID();
        final Long grandParentProjectId = createProject("Test Project Grandparent", "1.0", true, grandParentUuid);
        final UUID parentUuid = UUID.randomUUID();
        final Long parentProjectId = createProject("Test Project Parent", "1.0", true, parentUuid);
        setProjectParent(parentProjectId, grandParentProjectId);
        final UUID childUuid = UUID.randomUUID();
        final Long childProjectId = createProject("Test Project Child", "1.0", true, childUuid);
        setProjectParent(childProjectId, parentProjectId);
        final UUID grandChildUuid = UUID.randomUUID();
        final Long grandChildProjectId = createProject("Test Project Grandchild", "1.0", false, grandChildUuid);
        setProjectParent(grandChildProjectId, childProjectId);
        addProjectToRule(grandParentProjectId, ruleId);
        // Creates a new notification
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(grandChildUuid.toString()))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setAffectedProjectsReference(BackReference.newBuilder()
                                .setApiUri("foo")
                                .setFrontendUri("bar"))
                        .addAffectedProjects(Project.newBuilder()
                                .setUuid(grandChildUuid.toString()))
                        .build()))
                .build();
        // Ok, let's test this
        final List<NotificationRule> rules = notificationRouter.resolveRules(PublisherTestUtil.createPublisherContext(notification), notification);
        assertThat(rules).isEmpty();
    }

    @Test
    @TestTransaction
    void testInformWithValidMatchingRule() throws Exception {
        final Long publisherId = createConsolePublisher();
        // Creates a new rule and defines when the rule should be triggered (notifyOn)
        createRule("Test Rule",
                NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL,
                NotificationGroup.NEW_VULNERABILITY, publisherId);
        // Creates a project which will later be matched on
        final UUID projectUuid = UUID.randomUUID();
        createProject("Test Project", "1.0", true, projectUuid);
        // Creates a new notification
        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setSubject(Any.pack(NewVulnerabilitySubject.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setProject(Project.newBuilder()
                                .setUuid(projectUuid.toString()))
                        .setVulnerability(Vulnerability.newBuilder()
                                .setUuid(UUID.randomUUID().toString()))
                        .setAffectedProjectsReference(BackReference.newBuilder()
                                .setApiUri("foo")
                                .setFrontendUri("bar"))
                        .addAffectedProjects(Project.newBuilder()
                                .setUuid(projectUuid.toString()))
                        .build()))
                .build();
        // Ok, let's test this
        notificationRouter.inform(PublisherTestUtil.createPublisherContext(notification), notification);
        verify(consolePublisherMock).inform(any(), eq(notification), any());
    }

    private Long createConsolePublisher() {
        return (Long) entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONPUBLISHER" ("DEFAULT_PUBLISHER", "NAME", "PUBLISHER_CLASS", "TEMPLATE", "TEMPLATE_MIME_TYPE", "UUID") VALUES
                    (true, 'foo', 'org.dependencytrack.notification.publisher.ConsolePublisher', 'template','text/plain', '1781db56-51a8-462a-858c-6030a2341dfc')
                RETURNING "ID";
                """).getSingleResult();
    }

    private Long createRule(final String name, final NotificationScope scope, final NotificationLevel level,
                            final NotificationGroup group, final Long publisherId) {
        return (Long) entityManager.createNativeQuery("""            
                        INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "PUBLISHER", "NOTIFY_ON", "NOTIFY_CHILDREN", "LOG_SUCCESSFUL_PUBLISH", "NOTIFICATION_LEVEL", "SCOPE", "UUID") VALUES
                            (true, :name, :publisherId, :notifyOn, false, true, :level, :scope, '6b1fee41-4178-4a23-9d1b-e9df79de8e62')
                        RETURNING "ID";
                        """)
                .setParameter("name", name)
                .setParameter("publisherId", publisherId)
                .setParameter("notifyOn", group.name())
                .setParameter("level", level.name())
                .setParameter("scope", scope.name())
                .getSingleResult();
    }

    private void setNotifyChildren(final Long ruleId, final boolean notifyChildren) {
        entityManager.createNativeQuery("""
                        UPDATE "NOTIFICATIONRULE" SET "NOTIFY_CHILDREN" = :notifyChildren WHERE "ID" = :id
                        """)
                .setParameter("id", ruleId)
                .setParameter("notifyChildren", notifyChildren)
                .executeUpdate();
    }

    private void disableRule(final Long ruleId) {
        entityManager.createNativeQuery("""
                        UPDATE "NOTIFICATIONRULE" SET "ENABLED" = false WHERE "ID" = :ruleId
                        """)
                .setParameter("ruleId", ruleId)
                .executeUpdate();
    }

    private Long createProject(final String name, final String version, final boolean active, final UUID uuid) {
        return (Long) entityManager.createNativeQuery("""
                        INSERT INTO "PROJECT" ("NAME", "VERSION", "ACTIVE", "UUID") VALUES
                            (:name, :version, :active, :uuid)
                        RETURNING "ID";
                        """)
                .setParameter("name", name)
                .setParameter("version", version)
                .setParameter("active", active)
                .setParameter("uuid", uuid.toString())
                .getSingleResult();
    }

    private void setProjectParent(final Long childId, final Long parentId) {
        entityManager.createNativeQuery("""
                        UPDATE "PROJECT" SET "PARENT_PROJECT_ID" = :parentId WHERE "ID" = :id
                        """)
                .setParameter("parentId", parentId)
                .setParameter("id", childId)
                .executeUpdate();
    }

    private void addProjectToRule(final Long projectId, final Long ruleId) {
        entityManager.createNativeQuery("""
                        INSERT INTO "NOTIFICATIONRULE_PROJECTS" ("PROJECT_ID", "NOTIFICATIONRULE_ID") VALUES
                            (:projectId, :ruleId);
                        """)
                .setParameter("projectId", projectId)
                .setParameter("ruleId", ruleId)
                .executeUpdate();
    }

}