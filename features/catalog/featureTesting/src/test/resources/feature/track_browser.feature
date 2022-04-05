Feature: Users device tracks browser

  #Rule: Tracks shown according to sorting

  @list-tracks
  Scenario Outline: Device track listed according to sorting
    Given user opened audio browser screen
    Then device public tracks should be listed by date, in descending order
    When user select other "<sorting>" and "<order>"
    Then tracks listing should be sorted by "<sorting>" in order "<order>"
    Examples:
      | order      | sorting     |
      | ascending  | date        |
      | descending | artist name |
      | ascending  | artist name |

  #Rule: Allow user to share tracks

  @share-track
  Scenario: Track is shared
    Given user open audio browser screen
    When he select to share first listed track
    Then app should show device sharing ui

  #Rule: Allow users to delete tracks

  @delete-track
  Scenario: Track is deleted from device
    Given user has public audio tracks on device
    When user open audio browser screen
    And user select to delete first and last listed tracks
    Then app should delete selected tracks from device
    And app should update shown listed tracks

  #Rule: Persist user tracks sorting selection

  @persist-sorting
  Scenario Outline: Sorting selection persisted
    Given user opened tracks browser
    When he select other "<sorting>" and "<order>"
    And leave app
    Then tracks listing should shown sorted by "<sorting>" in "<order>" order
    Examples:
      | sorting     | order      |
      | date        | ascending  |
      | date        | descending |
      | artist name | ascending  |
      | artist name | descending |

  #Rule: Provide track detail info

  @track-detail
  Scenario: Track detail info shown
    Given user open audio browser screen
    When user select to view track detail of first track
    Then app should show track detail for first track

  #Rule: Playing selected audio playlist

  @play-selected
  Scenario: Play selected track
    Given user has public audio tracks on device
    When user open device tracks browser screen
    And user select first track
    Then app player should play first track as part of all tracks play queue