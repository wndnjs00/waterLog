package com.app.waterlog.di

import com.app.data.datastore.BadgeDataStore
import com.app.data.repository.BadgeRepositoryImpl
import com.app.domain.repository.BadgeRepository
import com.app.domain.repository.BadgeShownStore
import com.app.data.datastore.ChatLimitDataStore
import com.app.domain.repository.ChatLimitStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BadgeModule {
    @Binds
    @Singleton
    fun bindBadgeRepository(badgeRepositoryImpl: BadgeRepositoryImpl): BadgeRepository

    @Binds
    @Singleton
    fun bindBadgeShownStore(badgeDataStore: BadgeDataStore): BadgeShownStore

    @Binds
    @Singleton
    fun bindChatLimitStore(chatLimitDataStore: ChatLimitDataStore): ChatLimitStore
}