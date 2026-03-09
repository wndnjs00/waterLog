package com.app.waterlog.di

import com.app.data.repository.AccountRepositoryImpl
import com.app.data.repository.TimeProviderImpl
import com.app.data.repository.WaterRepositoryImpl
import com.app.domain.repository.AccountRepository
import com.app.domain.repository.TimeProvider
import com.app.domain.repository.WaterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindAccountRepository(accountRepositoryImpl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    fun bindTimeProvider(timeProviderImpl: TimeProviderImpl): TimeProvider

    @Binds
    @Singleton
    fun bindWaterRepository(waterRepositoryImpl: WaterRepositoryImpl): WaterRepository
}