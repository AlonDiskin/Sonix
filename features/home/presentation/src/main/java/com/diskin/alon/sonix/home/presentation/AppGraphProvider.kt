package com.diskin.alon.sonix.home.presentation

import androidx.annotation.NavigationRes

interface AppGraphProvider {

    @NavigationRes
    fun getAppGraph(): Int

    @NavigationRes
    fun getPlayerGraph(): Int
}