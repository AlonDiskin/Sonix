Feature: Users device tracks browser

  #Rule: Tracks shown according to sorting

  @list-tracks
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

  #Rule: Allow user to share tracks

  @share-track
  Scenario: Track is shared
    Given User has public audio tracks on device
    When User open audio browser screen
    And select to share first listed track
    Then App should show device sharing ui

  #Rule: Allow users to delete tracks

  @delete-track
  Scenario: Track is deleted from device
    Given User has public audio tracks on device
    When User open audio browser screen
    And User select to delete first and last listed tracks
    Then App should delete selected tracks from device
    And App should update shown listed tracks

  #Rule: Persist user tracks sorting selection

  @persist-sorting
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

  #Rule: Provide track detail info

  @track-detail
  Scenario: Track detail info shown
    Given User has public audio tracks on device
    When User open audio browser screen
    And User select to view track detail of first track
    Then App should show track detail for first track

  #Rule: Playing selected audio playlist

  @play-selected
  Scenario: Play selected track
    Given User has public audio tracks on device
    When User open device tracks browser screen
    And User select first track
    Then App player should play first track as part of all tracks play queue