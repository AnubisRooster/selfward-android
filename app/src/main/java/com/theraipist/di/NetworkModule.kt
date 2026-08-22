package com.theraipist.di

import com.theraipist.core.chat.ApiConfig
import com.theraipist.core.chat.Provider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient()

    @Provides
    @Singleton
    fun provideApiConfig(): ApiConfig = ApiConfig(
        provider = Provider.OPENAI,
        baseUrl = "https://api.openai.com/v1",
        apiKey = "",
        model = "gpt-4o-mini"
    )
}
