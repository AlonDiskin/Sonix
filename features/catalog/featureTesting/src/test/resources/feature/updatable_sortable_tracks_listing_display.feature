Feature: Show updatable sortable tracks listing as part
  of app audio catalog.

  Scenario Outline: Device track listed according to sorting
    Given User has public audio tracks on device
    When User open audio browser screen
    Then Device public tracks should be listed by date, in descending order
    When User select other "<sorting>" and "<order>"
    Then Tracks listing should be sorted by "<sorting>" in order "<order>"
    When First listed track deleted from device
    Then Tracks listing should be updated accordingly
    Examples:
      | order      | sorting     |
      | ascending  | date        |
      | descending | artist name |
      | ascending  | artist name |