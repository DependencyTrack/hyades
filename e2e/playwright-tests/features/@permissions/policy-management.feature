Feature:
  Scenario: Without POLICY_MANAGEMENT Permission The User Cannot See The Policy Management Tab
    Given the user "test-user_VP_PERMS" logs in to DependencyTrack
    Then the "policyManagementTab" tab should not be visible

  Scenario: Without POLICY_MANAGEMENT Permission The User Can See The Policy Management Tab
    Given the user "test-user_VP_PolicyM_PERMS" logs in to DependencyTrack
    When the user navigates to "policyManagementTab" page and verifies
    Then the create-policy button is visible
    And the policy "test-policy01" is visible
    Then the user navigates to policyManagement "licenceGroup" tab
    Then the create-licence-group button is visible
    And the licence-group "Copyleft" is visible
