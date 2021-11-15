package com.diskin.alon.sonix.catalog.di

import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.interfaces.SortingStore
import com.diskin.alon.sonix.catalog.data.AudioTrackRepositoryImpl
import com.diskin.alon.sonix.catalog.data.SortingStoreImpl
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
    abstract fun bindSortingStore(store: SortingStoreImpl): SortingStore
}