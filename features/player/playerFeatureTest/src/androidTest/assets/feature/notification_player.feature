Feature: Status bar notification player

  #Rule: Support playback play/pause and skip prev/next operations with track detail

  @playback-controlled
  Scenario Outline: Playback controlled from notification
    Given user has selected a playlist from app
    When he "<action>" track via notification
    Then player should "<outcome>"
    And update notification ui according to "<outcome>"
    Examples:
      | action            | outcome             |
      | pause             | pause current track |
      | skip next         | play next track     |
      | skip prev         | play prev track     |

  #Rule: Provide navigation to app player

  @app-opened
  Scenario: App opened from notification
    Given user is listening to selected playlist
    When he exit app
    And click on app player notification
    Then app should be opened