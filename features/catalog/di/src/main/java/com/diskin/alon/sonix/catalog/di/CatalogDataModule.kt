package com.diskin.alon.sonix.catalog.di

import com.diskin.alon.sonix.catalog.application.interfaces.AlbumRepository
import com.diskin.alon.sonix.catalog.application.interfaces.AlbumsSortingStore
import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.interfaces.TracksSortingStore
import com.diskin.alon.sonix.catalog.data.AlbumRepositoryImpl
import com.diskin.alon.sonix.catalog.data.AlbumsSortingStoreImpl
import com.diskin.alon.sonix.catalog.data.AudioTrackRepositoryImpl
import com.diskin.alon.sonix.catalog.data.TracksSortingStoreImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CatalogDataModule {

    @Singleton
    @Binds
    abstract fun bindAudioTrackRepository(repo: AudioTrackRepositoryImpl): AudioTrackRepository

    @Singleton
    @Binds
    abstract fun bindSortingStore(store: TracksSortingStoreImpl): TracksSortingStore

    @Singleton
    @Binds
    abstract fun bindAlbumRepository(repo: AlbumRepositoryImpl): AlbumRepository

    @Singleton
    @Binds
    abstract fun bindAlbumSortingStore(store: AlbumsSortingStoreImpl): AlbumsSortingStore
}