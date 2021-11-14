Feature: Navigation of app features

  User journey that exercise his usage of app functionality
  to navigate the different app features and screens.

  Scenario: User navigate app
    Given User launch app from device home
    Then Home screen should show catalog feature screen
    When User navigate to settings feature
    Then App should open settings feature screen