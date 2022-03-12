Feature: Status bar notification player

  #Rule: Support playback play/pause and skip prev/next operations with track detail

  @playback-controlled
  Scenario Outline: Playback controlled from notification
    Given user has selected a playlist from app
    And app is "<app state>"
    When he "<action>" track via notification
    Then player should "<outcome>"
    And update notification ui according to "<outcome>"
    Examples:
      | action            | outcome             | app state |
      | pause             | pause current track | open      |
      | pause             | pause current track | close     |
      | skip next         | play next track     | open      |
      | skip next         | play next track     | close     |
      | skip prev         | play prev track     | open      |
      | skip prev         | play prev track     | close     |

  #Rule: Provide navigation to app

  @app-opened
  Scenario: App opened from notification
    Given user is listening to selected playlist
    When he exit app
    And click on app player notification
    Then app should be opened