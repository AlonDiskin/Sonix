package com.diskin.alon.sonix.catalog.di

import com.diskin.alon.sonix.catalog.application.interfaces.*
import com.diskin.alon.sonix.catalog.data.*
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

    @Singleton
    @Binds
    abstract fun bindArtistRepository(repo: ArtistRepositoryImpl): ArtistRepository

    @Singleton
    @Binds
    abstract fun bindArtistSortingStore(store: ArtistsSortingStoreImpl): ArtistsSortingStore
}