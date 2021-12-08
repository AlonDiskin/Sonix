Feature: Control tracks playback from ui bottom player

  @play-pause-track
  Scenario: Track is paused and played with progress update
    Given User has track on device
    When User select to play track
    Then Player should play track and display pause button
    When User press the pause button
    Then Player should pause track and display play button