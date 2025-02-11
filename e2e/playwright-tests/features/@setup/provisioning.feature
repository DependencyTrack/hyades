@mode:serial
Feature:
  Scenario: Delete All Test Users Before Tests
    Given the admin user logs in to DependencyTrack and verifies
    When the user navigates to "administrationTab" page and verifies
    Then the user navigates to administration menu "accessManagement"
    And the user clicks on access-management submenu "managedUsers"
    Then the user deletes the following test users if they exist
      | username                      |
      | test-user0_PERMS              |
      | test-user_VP_PERMS            |
      | test-user_VP_SC_PERMS         |
      | test-user_VP_SC_AM_PERMS      |
      | test-user_VP_BU_PERMS         |
      | test-user_VP_PolicyM_PERMS    |
      | test-user_VP_VPV_PERMS        |
      | test-user_VP_PVA_PERMS        |
      | test-user_VP_TM_PERMS         |
      | test-user_VP_PortfolioM_PERMS |
      | test-user_VP_VV_PERMS         |
      | test-user_VP_VV_VA_PERMS      |
      | test-user_VP_VM_PERMS         |

  Scenario: Delete All Test Policies Before Tests
    Given the admin user logs in to DependencyTrack and verifies
    Then the user navigates to "policyManagementTab" page and verifies
    Then the user deletes the following test policies if they exist
      | policyName    |
      | test-policy01 |

  Scenario: Delete All Test Projects Before Tests
    Given the admin user logs in to DependencyTrack and verifies
    When the user navigates to "projectsTab" page and verifies
    Then the user deletes the following test projects if they exist
      | name           |
      | test-project01 |
      | test-project02 |
      | test-project03 |

  Scenario: Create Test Users
    Given the admin user logs in to DependencyTrack and verifies
    When the user navigates to "administrationTab" page and verifies
    Then the user navigates to administration menu "accessManagement"
    And the user clicks on access-management submenu "managedUsers"
    Then the user creates the following test users
      | username     |
      | test-user0_PERMS  |
      | test-user_VP_PERMS  |
      | test-user_VP_SC_PERMS  |
      | test-user_VP_SC_AM_PERMS  |
      | test-user_VP_BU_PERMS  |
      | test-user_VP_PolicyM_PERMS  |
      | test-user_VP_VPV_PERMS  |
      | test-user_VP_PVA_PERMS  |
      | test-user_VP_TM_PERMS  |
      | test-user_VP_PortfolioM_PERMS  |
      | test-user_VP_VV_PERMS  |
      | test-user_VP_VV_VA_PERMS  |
      | test-user_VP_VM_PERMS  |

  Scenario: Create Test Policies
    Given the admin user logs in to DependencyTrack and verifies
    When the user navigates to "policyManagementTab" page and verifies
    Then the user creates the following test policies
      | policyName    |
      | test-policy01 |
    And the user updates a policy with the following values
      | policyName       | newPolicyName | operator | violationState |
      | test-policy01    | test-policy01 | ANY      | FAIL           |
    And the user adds conditions to "test-policy01" with the following values
      | conditionSubject | conditionOperator | conditionInputValue |
      | AGE              | >                 | P1D                 |

  Scenario: Create Test Project With Default BOM
    Given the admin user logs in to DependencyTrack and verifies
    When the user navigates to "projectsTab" page and verifies
    Then the user creates projects with the following values
      | name           | classifier  | version | isLastVersion | team | parent | description | tag |
      | test-project01 | APPLICATION |         |               |      |        |             |     |
      | test-project02 | APPLICATION |         |               |      |        |             |     |
      | test-project03 | APPLICATION |         |               |      |        |             |     |
    Then the user opens the project with the name "test-project01"
    And the user navigates to project "components" tab
    And the user uploads default BOM
    Then the user navigates to "projectsTab" page and verifies
    Then the user opens the project with the name "test-project02"
    And the user navigates to project "components" tab
    And the user uploads default BOM

  Scenario: Provide Test Users With Respective Permissions
    Given the admin user logs in to DependencyTrack and verifies
    When the user navigates to "administrationTab" page and verifies
    Then the user navigates to administration menu "accessManagement"
    And the user clicks on access-management submenu "managedUsers"
    Then the user provides "test-user_VP_PERMS" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
    Then the user provides "test-user_VP_SC_PERMS" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | SYSTEM_CONFIGURATION  |
    Then the user provides "test-user_VP_SC_AM_PERMS" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | SYSTEM_CONFIGURATION  |
      | ACCESS_MANAGEMENT     |
    Then the user provides "test-user_VP_BU_PERMS" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | BOM_UPLOAD            |
    Then the user provides "test-user_VP_PolicyM_PERMS" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | POLICY_MANAGEMENT     |
    Then the user provides "test-user_VP_VPV_PERMS" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | VIEW_POLICY_VIOLATION |
    Then the user provides "test-user_VP_PVA_PERMS" with the following permissions
      | permission                |
      | VIEW_PORTFOLIO            |
      | POLICY_VIOLATION_ANALYSIS |
    Then the user provides "test-user_VP_TM_PERMS" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | TAG_MANAGEMENT        |
    Then the user provides "test-user_VP_PortfolioM_PERMS" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | PORTFOLIO_MANAGEMENT  |
    Then the user provides "test-user_VP_VV_PERMS" with the following permissions
      | permission            |
      | VIEW_PORTFOLIO        |
      | VIEW_VULNERABILITY    |
    Then the user provides "test-user_VP_VV_VA_PERMS" with the following permissions
      | permission             |
      | VIEW_PORTFOLIO         |
      | VIEW_VULNERABILITY     |
      | VULNERABILITY_ANALYSIS |
    Then the user provides "test-user_VP_VM_PERMS" with the following permissions
      | permission               |
      | VIEW_PORTFOLIO           |
      | VULNERABILITY_MANAGEMENT |
