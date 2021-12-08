package com.diskin.alon.sonix.player.di

import com.diskin.alon.sonix.player.data.TrackMetadataStoreImpl
import com.diskin.alon.sonix.player.infrastructure.interfaces.TrackMetadataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerDataModule {

    @Singleton
    @Binds
    abstract fun bindTrackMetadataStore(store:  TrackMetadataStoreImpl): TrackMetadataStore
}