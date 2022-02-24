package com.diskin.alon.sonix

import com.diskin.alon.sonix.home.presentation.AppGraphProvider
import javax.inject.Inject

class AppGraphProviderImpl @Inject constructor() : AppGraphProvider {

    override fun getAppGraph(): Int {
        return R.navigation.app_nav_graph
    }

    override fun getPlayerGraph(): Int {
        return R.navigation.player_nav_graph
    }
}