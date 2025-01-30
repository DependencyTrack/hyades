@test
Feature:
  Scenario: Upload Default BOM To Recently Created Test Project
    Given the admin user logs in to DependencyTrack
    Then the dashboard should be visible
    Then the user navigates to project page
    Then the user opens the project with the name "test-project01"
    And the user navigates to project "components" tab
    And the user uploads default BOM

  Scenario: Validate Test Project With Uploaded BOM
    Given the admin user logs in to DependencyTrack
    Then the dashboard should be visible
    Then the user navigates to project page
    Then the user opens the project with the name "test-project01"
    Then the user verifies "components" with the badge number of 238 on current project
    Then the user verifies "services" with the badge number of 19 on current project
    Then the user verifies "dependencyGraph" with the badge number of 1 on current project
    Then the user verifies "exploitPredictions" with the badge number of 3 on current project
    Then the user verifies Audit Vulnerabilities with the badge number of 3 excluding and 3 including aliases on current project
    Then the user verifies Policy Violations with the badge number of 0 total 0 info 0 warn 0 fail violations on current project

  Scenario:

  @skip # Skipping as it is unnecessary to delete
  Scenario: Delete Project Afterwards
    Given the admin user logs in to DependencyTrack
    Then the dashboard should be visible
    Then the user navigates to project page
    And the user deletes the following test projects
      | name           |
      | main-project01 |
