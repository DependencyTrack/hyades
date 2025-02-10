Feature:
  Scenario: Without BOM_UPLOAD Permission The Test-User Cannot See The BOM Upload Button
    Given the user "test-user_VP_PERMS" tries to log in to DependencyTrack
    When the "dashboardTab" tab should be visible and active
    Then the user navigates to "projectsTab" page and verifies
    Then the user opens the project with the name "test-project03"
    Then the user navigates to project "components" tab
    And the upload-bom button is invisible

  Scenario: With BOM_UPLOAD Permissions The Test-User Can See The BOM Upload Button
    Given the user "test-user_VP_BU_PERMS" tries to log in to DependencyTrack
    When the "dashboardTab" tab should be visible and active
    Then the user navigates to "projectsTab" page and verifies
    Then the user opens the project with the name "test-project03"
    Then the user navigates to project "components" tab
    And the upload-bom button is visible
    # Upload could be tested here as well