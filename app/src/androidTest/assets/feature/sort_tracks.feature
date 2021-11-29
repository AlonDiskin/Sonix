Feature: Sort the shown device tracks

  User journey that exercise his usage of app functionality
  to view sorted listing of audio track from user device.

  Scenario: User sort tracks
    Given User has not changed tracks sorting
    And User has public audio tracks on his device
    When User launch app from device home
    Then App should show tracks sorted by date in descending order
    And User select tracks ordering as ascending
    And User relaunch app
    Then App should show tracks catalog sorted by date in ascending order
