@mode:serial
Feature:
  Scenario: Cleanup Test Users Before Tests
    Given the admin user logs in to DependencyTrack
    Then the dashboard should be visible
    Then the admin user navigates to administration page
    And the admin user navigates to administration "accessManagement"
    And the admin user clicks on access-management "managedUsers"
    Then the admin user deletes the following test users
      | username     |
      | test-user01  |
      | test-user02  |
      | test-user03  |
      | test-user04  |
      | test-user05  |
      | test-user06  |
      | test-user07  |
      | test-user08  |
      | test-user09  |
      | test-user10  |
      | test-user11  |
      | test-user12  |
      | test-user13  |

  Scenario: Create Test Users
    Given the admin user logs in to DependencyTrack
    Then the dashboard should be visible
    Then the admin user navigates to administration page
    And the admin user navigates to administration "accessManagement"
    And the admin user clicks on access-management "managedUsers"
    Then the admin user creates the following test users
      | username     |
      | test-user01  |
      | test-user02  |
      | test-user03  |
      | test-user04  |
      | test-user05  |
      | test-user06  |
      | test-user07  |
      | test-user08  |
      | test-user09  |
      | test-user10  |
      | test-user11  |
      | test-user12  |
      | test-user13  |

  Scenario: Provide Test Users With Respective Permissions
    Given the admin user logs in to DependencyTrack
    Then the dashboard should be visible
    Then the admin user navigates to administration page
    And the admin user navigates to administration "accessManagement"
    And the admin user clicks on access-management "managedUsers"
    Then the admin user provides "test-user01" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
    Then the admin user provides "test-user02" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | SYSTEM_CONFIGURATION  |
    Then the admin user provides "test-user03" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | ACCESS_MANAGEMENT     |
    Then the admin user provides "test-user04" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | BOM_UPLOAD            |
    Then the admin user provides "test-user05" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | POLICY_MANAGEMENT     |
    Then the admin user provides "test-user06" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | VIEW_POLICY_VIOLATION |
    Then the admin user provides "test-user07" with the following permissions
      | permission                |
      | VIEW_PORTFOLIO            |
      | POLICY_VIOLATION_ANALYSIS |
    Then the admin user provides "test-user08" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | TAG_MANAGEMENT        |
    Then the admin user provides "test-user09" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | PORTFOLIO_MANAGEMENT  |
    Then the admin user provides "test-user10" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | VIEW_VULNERABILITY    |
    Then the admin user provides "test-user11" with the following permissions
      | permission             |
      | VIEW_PORTFOLIO         |
      | VIEW_VULNERABILITY     |
      | VULNERABILITY_ANALYSIS |
    Then the admin user provides "test-user12" with the following permissions
      | permission               |
      | VIEW_PORTFOLIO           |
      | VULNERABILITY_MANAGEMENT |
    Then the admin user provides "test-user13" with the following permissions
      | permission               |
      | VIEW_PORTFOLIO           |
      | PORTFOLIO_ACCESS_CONTROL |
