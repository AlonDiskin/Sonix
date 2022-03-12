package com.diskin.alon.sonix.catalog.featuretesting.di

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
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
    fun provideResources(): Resources {
        return ApplicationProvider.getApplicationContext<Context>().resources
    }

    @Singleton
    @Provides
    fun provideContentResolver(): ContentResolver {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(
                ApplicationProvider.getApplicationContext()
            )
    }
}