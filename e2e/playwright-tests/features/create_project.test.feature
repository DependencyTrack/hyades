@skip
Feature:
  Scenario: Create Project with BOM
    Given the admin user logs in to DependencyTrack
    Then the dashboard should be visible
    Then the user navigates to project page
    And the user creates projects with the following values
      | name           | classifier  | version | isLastVersion | team | parent | description | tag |
      | test-project02 | Application | ""      | ""            | ""   | ""     | ""          | ""  |