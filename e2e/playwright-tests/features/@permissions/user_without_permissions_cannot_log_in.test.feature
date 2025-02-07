Feature:
  Scenario: The Test-User Tries To Log In With Wrong Credentials
    Given the user "test-user00" tries to log in to DependencyTrack with password "wrongPassword"
    Then the user receives login credentials error toast
    And the user sees wrong log in credentials modal content popup

  Scenario: The Test-User Without Any Permissions Cannot Log In
    Given the user "test-user00" tries to log in to DependencyTrack
    Then the user receives login error toast