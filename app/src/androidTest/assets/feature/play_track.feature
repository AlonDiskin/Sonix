Feature: Play device audio track

  User journey that exercise his usage of app functionality
  to play an existing track from users device.

  Scenario: Play track
    Given user launch app from device home
    When he select to play first listed device track
    Then app should play track