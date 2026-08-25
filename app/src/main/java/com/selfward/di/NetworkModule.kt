package com.selfward.di

import android.content.Context
import com.selfward.core.catalog.OpenRouterCatalog
import com.selfward.core.catalog.ProviderCatalog
import com.selfward.core.catalog.UnusableModels
import com.selfward.data.catalog.HttpOpenRouterCatalog
import com.selfward.data.catalog.HttpProviderCatalog
import com.selfward.data.catalog.PrefsUnusableModels
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Provides
    @Singleton
    fun provideOpenRouterCatalog(
        client: HttpClient,
        @ApplicationContext context: Context
    ): OpenRouterCatalog = HttpOpenRouterCatalog(client, context)

    @Provides
    @Singleton
    fun provideUnusableModels(@ApplicationContext context: Context): UnusableModels =
        PrefsUnusableModels(context)

    @Provides
    @Singleton
    fun provideProviderCatalog(
        client: HttpClient,
        openRouter: OpenRouterCatalog,
        unusableModels: UnusableModels,
        @ApplicationContext context: Context
    ): ProviderCatalog = HttpProviderCatalog(client, openRouter, unusableModels, context)
}
