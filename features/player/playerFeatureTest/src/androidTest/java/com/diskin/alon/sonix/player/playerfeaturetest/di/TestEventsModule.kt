package com.diskin.alon.sonix.player.playerfeaturetest.di

import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestEventsModule {

    @Singleton
    @Provides
    fun provideSelectedPlaylistProvider(): SelectedPlayListProvider {
        return mockk()
    }
}