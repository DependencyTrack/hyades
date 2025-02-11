Feature:
  Scenario: Without BOM_UPLOAD Permission The User Cannot See The BOM Upload Button
    Given the user "test-user_VP_PERMS" logs in to DependencyTrack
    When the user navigates to "projectsTab" page and verifies
    Then the user opens the project with the name "test-project01"
    Then the user navigates to project "components" tab and verifies
    And the upload-bom button is invisible

  Scenario: With BOM_UPLOAD Permissions The User Can See The BOM Upload Button
    Given the user "test-user_VP_BU_PERMS" logs in to DependencyTrack
    When the user navigates to "projectsTab" page and verifies
    Then the user opens the project with the name "test-project01"
    Then the user navigates to project "components" tab and verifies
    And the upload-bom button is visible
    # Upload could be tested here as well
