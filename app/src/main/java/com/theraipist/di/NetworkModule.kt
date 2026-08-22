package com.theraipist.di

import com.theraipist.core.chat.ApiConfig
import com.theraipist.core.chat.Provider
import com.theraipist.data.settings.SecureSettings
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
    fun provideApiConfig(settings: SecureSettings): ApiConfig {
        val baseUrl = when (settings.provider) {
            Provider.OPENAI -> "https://api.openai.com/v1"
            Provider.OPENROUTER -> "https://openrouter.ai/api/v1"
            Provider.ANTHROPIC -> "https://api.anthropic.com/v1"
        }
        return ApiConfig(
            provider = settings.provider,
            baseUrl = baseUrl,
            apiKey = settings.apiKey ?: "",
            model = settings.model
        )
    }
}
