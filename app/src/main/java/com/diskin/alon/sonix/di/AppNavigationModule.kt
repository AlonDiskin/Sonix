package com.diskin.alon.sonix.di

import com.diskin.alon.sonix.AppGraphProviderImpl
import com.diskin.alon.sonix.home.presentation.AppGraphProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppNavigationModule {

    @Binds
    @Singleton
    abstract fun bindAppGraphProvider(graphProvider: AppGraphProviderImpl): AppGraphProvider
}