package com.diskin.alon.sonix.catalog.di

import com.diskin.alon.sonix.catalog.application.interfaces.PlaylistSender
import com.diskin.alon.sonix.catalog.infrastructure.PlaylistSenderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CatalogInfrastructureModule {

    @Singleton
    @Binds
    abstract fun bindPlaylistSender(sender: PlaylistSenderImpl): PlaylistSender
}