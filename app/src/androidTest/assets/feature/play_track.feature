Feature: Play device audio track

  User journey that exercise his usage of app functionality
  to play an existing track from users device.

  Scenario: Play track
    Given User has public audio tracks on his device
    When User launch app from device home
    When User select to play first listed device track
    Then App should play track playback in app player