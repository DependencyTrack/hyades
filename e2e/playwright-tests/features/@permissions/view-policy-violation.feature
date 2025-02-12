Feature:
  Scenario: Without VIEW_POLICY_VIOLATION Permission The User Cannot See The Policy Violations On Project Page
    Given the user "test-user_VP_PERMS" logs in to DependencyTrack and verifies
    Then the user navigates to "projectsTab" page and verifies
    Then the user opens the project with the name "test-project01"
    Then the project "policyViolations" tab should not be visible

  Scenario: With VIEW_POLICY_VIOLATION Permissions The User Can See The Policy Violations On Project Page
    Given the user "test-user_VP_VPV_PERMS" logs in to DependencyTrack and verifies
    Then the user navigates to "projectsTab" page and verifies
    Then the user opens the project with the name "test-project01"
    Then the project "policyViolations" tab should be visible
    Then the user verifies Policy Violations with the badge number of 238 total 0 info 0 warn 238 fail violations on current project
