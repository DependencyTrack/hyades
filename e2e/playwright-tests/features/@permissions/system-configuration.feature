Feature:
  Scenario: Without SYSTEM_CONFIGURATION Permissions User Cannot See Administration Tab
    Given the user "test-user_VP_PERMS" logs in to DependencyTrack and verifies
    Then the "administrationTab" tab should not be visible

  Scenario: With SYSTEM_CONFIGURATION Permissions User Can See Administration Tab
    Given the user "test-user_VP_SC_PERMS" logs in to DependencyTrack and verifies
    When the user navigates to "administrationTab" page and verifies
