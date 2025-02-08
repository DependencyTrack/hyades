Feature:
  Scenario: With SYSTEM_CONFIGURATION But Without ACCESS_MANAGEMENT Permissions The Test-User Cannot See Access Management Menu
    Given the user "test-user02" tries to log in to DependencyTrack
    When the user navigates to "administrationTab" page and verifies
    Then the "accessManagement" menu should not be visible

  Scenario: With SYSTEM_CONFIGURATION x ACCESS_MANAGEMENT Permissions The Test-User Can See Administration Tab
    Given the user "test-user03" tries to log in to DependencyTrack
    When the user navigates to "administrationTab" page and verifies
    Then the "accessManagement" menu should be visible
    Then the user navigates to administration menu "accessManagement"
    And the accessManagement submenu should be visible
