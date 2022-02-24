Feature: Control tracks playlist from device status bar notification.

  Scenario: Playlist controlled via status bar notification
    Given user is listening to selected playlist
    When he exist app
    And open status bar
    Then app should show a notification with playback controls
    When user pause and skip to next track
    Then app player should skip to next track in playlist and pause playback
