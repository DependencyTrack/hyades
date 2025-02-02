@test
Feature:

  Scenario: The Admin User Suppresses A Policy Violation
    Given the admin user logs in to DependencyTrack
    When the dashboard should be visible
    Then the user navigates to "projectsTab" page
    Then the user tries to create a project with name "test-project01" and classifier "APPLICATION"
    Then the user receives project creation error and warn toast
