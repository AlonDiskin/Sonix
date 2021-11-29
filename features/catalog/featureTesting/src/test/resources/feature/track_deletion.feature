Feature: Provide deletion action on device tracks

  Scenario: Track is deleted from device
    Given User has public audio tracks on device
    When User open audio browser screen
    And User select to delete first and last listed tracks
    Then App should delete selected tracks from device
    And App should update shown listed tracks