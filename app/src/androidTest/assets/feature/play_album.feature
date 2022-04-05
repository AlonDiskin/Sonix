Feature: Play album's tracks

  Scenario: Album tracks played
    Given user launch app from device home
    When he open albums browser
    And select an album from albums browser
    And select to play whole album
    Then app should play album as playlist from first track