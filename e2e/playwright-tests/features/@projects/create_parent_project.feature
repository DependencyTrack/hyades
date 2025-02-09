Feature:
  Scenario: The Admin User Creates A Parent And A Child Project
    Given the admin user logs in to DependencyTrack
    When the "dashboardTab" tab should be visible and active
    Then the user navigates to "projectsTab" page and verifies
    Then the user creates projects with the following values
      | name             | classifier  | version | isLastVersion | team | parent           | description | tag |
      | parent-project01 | APPLICATION |         |               |      |                  |             |     |
      | child-project01  | APPLICATION |         |               |      | parent-project01 |             |     |
    Then the project "parent-project01" should be a parent project and contain "child-project01" as child project

  Scenario: The Admin User Deletes Respective Projects
    Given the admin user logs in to DependencyTrack
    When the "dashboardTab" tab should be visible and active
    Then the user navigates to "projectsTab" page and verifies
    Then the user deletes the following test projects
      | name             |
      | parent-project01 |
