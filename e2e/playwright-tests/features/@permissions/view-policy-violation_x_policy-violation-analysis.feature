Feature: VIEW_PORTFOLIO x VIEW_POLICY_VIOLATION x POLICY_VIOLATION_ANALYSIS
  Scenario: With VIEW_POLICY_VIOLATION But Without POLICY_VIOLATION_ANALYSIS Permission The User Cannot Edit The Policy Violations On Project Page
    Given the user "test-user_VP_VPV_PERMS" logs in to DependencyTrack and verifies
    Then the user navigates to "projects" page and verifies
    Then the user opens the project with the name "test-project01"
    Then the user navigates to project "policyViolations" tab and verifies
    Then the user expands the first violation on policy violation project tab
    Then the user verifies read access on the policy violation audit view on policy violations project tab

  Scenario: With VIEW_POLICY_VIOLATION x POLICY_VIOLATION_ANALYSIS Permission The User Can Edit The Policy Violations On Project Page
    Given the user "test-user_VP_VPV_PVA_PERMS" logs in to DependencyTrack and verifies
    Then the user navigates to "projects" page and verifies
    Then the user opens the project with the name "test-project01"
    Then the user navigates to project "policyViolations" tab and verifies
    Then the user expands the first violation on policy violation project tab
    Then the user verifies write access on the policy violation audit view on policy violations project tab