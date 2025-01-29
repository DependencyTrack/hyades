Feature:
  Scenario: Create Project With Default BOM
    Given the admin user logs in to DependencyTrack
    Then the dashboard should be visible
    Then the user navigates to project page
    And the user creates projects with the following values
      | name           | classifier  | version | isLastVersion | team | parent | description | tag |
      | main-project01 | APPLICATION |         |               |      |        |             |     |
    Then the user opens the project with the name "test-project01"
    And the user navigates to project "components" tab
    And the user uploads default BOM