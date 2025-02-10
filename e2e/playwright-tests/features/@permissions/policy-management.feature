Feature:
  Scenario: Without POLICY_MANAGEMENT Permission The Test-User Cannot See The Policy Management Tab
    Given the user "test-user_VP_PERMS" tries to log in to DependencyTrack
    When the "dashboardTab" tab should be visible and active
    Then the "policyManagementTab" tab should not be visible

  Scenario: Without POLICY_MANAGEMENT Permission The Test-User Can See The Policy Management Tab
    Given the user "test-user_VP_PolicyM_PERMS" tries to log in to DependencyTrack
    When the "dashboardTab" tab should be visible and active
    Then the user navigates to "policyManagementTab" page and verifies
    # hier weiter
    # policies create button visible
    # policy "test-policy01" is visible
    # licence group create button visible