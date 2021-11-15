Feature: Browse device public audio tracks

  #Rule: Show updatable sortable tracks listing

  @tracks-listed
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


  #Rule: Persist users sorting selection

  @selected-sorting-persisted
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
