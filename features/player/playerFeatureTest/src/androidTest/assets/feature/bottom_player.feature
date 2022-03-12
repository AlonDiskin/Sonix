Feature: App bottom player

  #Rule: Restore player last played playlist when user reopen app

    @playlist-restored
    Scenario: Player last playlist restored
      Given user currently listen to tracks playlist
      When he relaunch the app after pausing track
      Then player should restore last played playlist
      And display last played track in paused state

    @last-played-deleted
    Scenario: Last played deleted
      Given user listen to one of his tracks
      When he relaunch app after deleting last played track from device
      Then player should restore playlist without the deleted track
      And display the track next to deleted as last played

  #Rule: Control tracks playback from ui bottom player

    @playback-controlled
    Scenario: Track playback controlled
      Given user selected a track
      When he pause the track playback
      Then player should pause the playback
      When play the track again
      Then player should resume the playback

  #Rule: Play a user selected play list

    @play-selected
    Scenario Outline: Player plays selected play list
      Given User has 3 tracks on device
      When User open app,and not yet selected playlist to listen to
      Then Player should be hidden
      When User select to play a track at "<position>" from tracks playlist
      Then Player should start playing selected track and show its metadata
      When Track playback completes
      Then Player should "<start_next>" next track in playlist and show its metadata
      Examples:
        | position | start_next |
        | first    | true       |
        | second   | true       |
        | third    | false      |

    @play-fail
    Scenario Outline: Next track played when selected fail
      Given user select to listen to a track positioned at "<position>" in a playlist
      When player fail to play selected track
      Then player should play "<next>" from playlist
      And display its metadata
      Examples:
        | position | next   |
        | first    | second |
        | last     | first  |
