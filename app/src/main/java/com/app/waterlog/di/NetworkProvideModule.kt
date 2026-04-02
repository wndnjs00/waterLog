package com.app.waterlog.di

import com.app.data.remote.OpenAiApi
import com.app.waterlog.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkProvideModule {

    @Provides
    @Singleton
    fun provideOpenAiApi(): OpenAiApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()
            .create(OpenAiApi::class.java)
    }
}
