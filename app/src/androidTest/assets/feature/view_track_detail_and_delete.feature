Feature: View track detail and delete it from device

  User journey that exercise his usage of app functionality
  to view a detail account of an existing audio track from user device,
  and then delete it.

  Scenario: User views track detail and delete it
    Given User has public audio tracks on his device
    When User launch app from device home
    Then App should show all device tracks
    When User select to see the detail of the first shown track
    Then App should show track detail to user
    When User select to delete tracks
    Then App should delete track from device
    And Update tracks listing accordingly