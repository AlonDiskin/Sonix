Feature: Show track detail info

  Scenario: Track detail info shown
    Given User has public audio tracks on device
    When User open audio browser screen
    And User select to view track detail of first track
    Then App should show track detail for first track