Feature:
  # already logged in due to storageState in playwright config
  Scenario: The admin creates test users
    Given the admin user navigates to dashboard
    Then the admin user navigates to administration page
    And the admin user navigates to administration "access-management"
    And the admin user clicks on access-management "managedUsers"
    Then the admin user creates the following test users
      | username    |
      | test-user1  |
      | test-user2  |
      | test-user3  |
      | test-user4  |
      | test-user5  |
      | test-user6  |
      | test-user7  |
      | test-user8  |
      | test-user9  |
      | test-user10 |
      | test-user11 |
      | test-user12 |
      | test-user13 |
    Then the admin user provides "test-user1" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
    Then the admin user provides "test-user2" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | SYSTEM_CONFIGURATION  |
    Then the admin user provides "test-user3" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | ACCESS_MANAGEMENT     |
    Then the admin user provides "test-user4" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | BOM_UPLOAD            |
    Then the admin user provides "test-user5" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | POLICY_MANAGEMENT     |
    Then the admin user provides "test-user6" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | VIEW_POLICY_VIOLATION |
    Then the admin user provides "test-user7" with the following permissions
      | permission                |
      | VIEW_PORTFOLIO            |
      | POLICY_VIOLATION_ANALYSIS |
    Then the admin user provides "test-user8" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | TAG_MANAGEMENT        |
    Then the admin user provides "test-user9" with the following permissions
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
