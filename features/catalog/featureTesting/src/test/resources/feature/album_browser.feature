Feature: Device tracks albums browser

  #Rule: List sorted albums

  @listing-sorted
  Scenario Outline: Albums listing sorted
    Given user has never sorted albums listing before
    And he open album browser
    Then browser should list all albums by album name,in ascending order
    When user sort albums by "<sorting>" in "<order>"
    Then browser should list albums accordingly
    Examples:
      | sorting | order      |
      | name    | descending |
      | artist  | ascending  |
      | artist  | descending |

  @last-sorting-saved
  Scenario Outline: Albums sorting saved
    Given user opened albums browser
    When he sort albums by "<sorting>" in "<order>"
    And he leaves app
    Then browser should save user selected sorting
    Examples:
      | sorting | order      |
      | name    | descending |
      | artist  | ascending  |
      | artist  | descending |


