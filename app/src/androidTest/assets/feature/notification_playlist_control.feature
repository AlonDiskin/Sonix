Feature: Control tracks playlist from device status bar notification.

  Scenario: Playlist controlled via status bar notification
    Given user is listening to selected playlist
    When he exist app
    And open app player notification
    When user pause and skip to next track
    Then app player should skip to next track in playlist and pause playback
