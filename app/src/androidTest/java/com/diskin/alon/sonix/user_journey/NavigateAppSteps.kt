package com.diskin.alon.sonix.user_journey

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.sonix.R
import com.diskin.alon.sonix.util.DeviceUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class NavigateAppSteps : GreenCoffeeSteps() {

    @Given("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
    }

    @Then("^Home screen should show catalog feature screen$")
    fun home_screen_should_show_catalog_feature_screen() {
        onView(withId(R.id.catalog_root))
            .check(matches(isDisplayed()))
    }

    @When("^User navigate to settings feature$")
    fun user_navigate_to_settings_feature() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText("Settings"))
            .perform(click())
    }

    @Then("^App should open settings feature screen$")
    fun app_should_open_settings_feature_screen() {
        onView(withId(R.id.settings_root))
            .check(matches(isDisplayed()))
    }
}