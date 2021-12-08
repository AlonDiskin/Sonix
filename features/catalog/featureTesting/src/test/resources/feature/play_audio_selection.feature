Feature: Playing selected audio playlist

  Scenario: Play all tracks
    Given User has public audio tracks on device
    When User open device tracks browser screen
    And User select first track
    Then App player should play first track as part of all tracks play queue

