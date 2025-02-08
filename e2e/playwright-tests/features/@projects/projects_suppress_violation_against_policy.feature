Feature:

  Scenario: The Admin User Suppresses A Policy Violation
    Given the admin user logs in to DependencyTrack
    When the "dashboardTab" tab should be visible and active
    Then the user navigates to "projectsTab" page and verifies
    And the "projectsTab" tab should be visible and active
    And the user opens the project with the name "test-project02"
    And the user navigates to project "policyViolations" tab
    Then the user opens the policy violation of Component "JUnitParams"
    Then the user comments the current policy violation with "this is a comment"
    And the audit trail should contain "this is a comment"
    And the user sets the analysis to "Rejected"
    And the audit trail should contain "NOT_SET → REJECTED"
    Then the user suppresses the current policy violation
    And the audit trail should contain "SUPPRESSED"
    And the policy violation of the component "JUnitParams" will not appear in search result