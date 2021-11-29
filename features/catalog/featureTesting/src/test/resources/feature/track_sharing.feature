Feature: Allow user to share tracks

  Scenario: Track is shared
    Given User has public audio tracks on device
    When User open audio browser screen
    And select to share first listed track
    Then App should show device sharing ui