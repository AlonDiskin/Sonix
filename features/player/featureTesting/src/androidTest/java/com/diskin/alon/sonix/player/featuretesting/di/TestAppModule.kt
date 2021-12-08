package com.diskin.alon.sonix.player.featuretesting.di

import android.app.Application
import android.content.ContentResolver
import com.diskin.alon.sonix.player.infrastructure.AudioPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Singleton
    @Provides
    fun provideContentResolver(): ContentResolver {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideAudioPlayer(app: Application): AudioPlayer {
        return AudioPlayer(app)
    }
}