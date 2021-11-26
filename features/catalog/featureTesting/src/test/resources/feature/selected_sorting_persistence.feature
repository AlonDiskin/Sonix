Feature: Persist user tracks sorting selection

  Scenario Outline: Sorting selection persisted
    Given User has public audio tracks on device
    When User open audio browser screen
    And User select other "<sorting>" and "<order>"
    And Relaunch browser screen
    Then Tracks listing should shown sorted by "<sorting>" in "<order>" order
    Examples:
      | sorting     | order      |
      | date        | ascending  |
      | date        | descending |
      | artist name | ascending  |
      | artist name | descending |