package com.app.waterlog.di

import android.content.Context
import com.app.data.datastore.BadgeDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideBadgeDataStore(
        @ApplicationContext context: Context
    ): BadgeDataStore {
        return BadgeDataStore(context)
    }
}