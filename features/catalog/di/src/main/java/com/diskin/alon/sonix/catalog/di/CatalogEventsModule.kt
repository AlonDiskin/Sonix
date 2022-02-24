package com.diskin.alon.sonix.catalog.di

import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.catalog.events.SelectedPlaylistEventHandler
import com.diskin.alon.sonix.catalog.events.SelectedPlaylistPublisher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CatalogEventsModule {

    @Singleton
    @Binds
    abstract fun bindPlaylistProvider(handler: SelectedPlaylistEventHandler): SelectedPlayListProvider

    @Singleton
    @Binds
    abstract fun bindPlaylistPublisher(handler: SelectedPlaylistEventHandler): SelectedPlaylistPublisher
}