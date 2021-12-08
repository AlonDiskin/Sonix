Feature: Playing a user selected play list

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
   Scenario Outline: Player fail to play track
     Given User has 3 tracks on device
     When User open app
     And User select to play a track at "<position>" from tracks playlist which fail to play
     Then Player should play "<next>" track and show its metadata
     Examples:
       | position | next   |
       | first    | second |
       | second   | third  |
       | third    | first  |