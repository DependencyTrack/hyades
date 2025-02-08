Feature:
  Scenario: Without SYSTEM_CONFIGURATION Permissions Test-User Cannot See Administration Tab
    Given the user "test-user01" tries to log in to DependencyTrack
    Then the "administrationTab" tab should not be visible

  Scenario: With SYSTEM_CONFIGURATION Permissions Test-User Can See Administration Tab
    Given the user "test-user02" tries to log in to DependencyTrack
    When the user navigates to "administrationTab" page and verifies
    Then the "administrationTab" tab should be visible and active