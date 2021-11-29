Feature: Engage catalog track options.

  User journey that exercise his usage of app functionality
  to engage in the different actions he can perform with catalog tracks.

  Scenario: User views track detail and delete it
    Given User has public audio tracks on his device
    When User launch app from device home
    When User select to view the detail of the first catalog track
    Then App should show track detail to user
    When Use select to share track
    Then App should show device sharing menu
    When User select to delete track
    Then App should delete track from device
    And Update tracks listing accordingly