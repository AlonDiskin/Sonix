Feature: Play artist's tracks

  Scenario: Artists tracks played
    Given user launch app from device home
    When he open artists browser
    And select an artist from browser
    And select to play all artists tracks
    Then app should play artist tracks as playlist from first track