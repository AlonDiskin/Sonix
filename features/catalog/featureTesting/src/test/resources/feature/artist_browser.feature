Feature: Device tracks artists browser

  #Rule: List sorted artists

  @listing-sorted
  Scenario Outline: Artists listing sorted
    Given user has never sorted artists listing before
    And he open artist browser
    Then browser should list all artists by name,in ascending order
    When user sort artists by "<sorting>" in "<order>"
    Then browser should list artists accordingly
    Examples:
      | sorting | order      |
      | name    | descending |
      | date    | ascending  |
      | date    | descending |

  @last-sorting-saved
  Scenario Outline: Artists sorting saved
    Given user opened artists browser
    When he sort artists by "<sorting>" in "<order>"
    And he leaves app
    Then browser should save user selected sorting
    Examples:
      | sorting | order      |
      | name    | descending |
      | date    | ascending  |
      | date    | descending |