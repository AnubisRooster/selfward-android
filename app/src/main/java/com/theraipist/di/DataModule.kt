package com.theraipist.di

import android.content.Context
import androidx.room.Room
import com.theraipist.core.chat.ChatService
import com.theraipist.core.chat.CloudChatService
import com.theraipist.core.embedding.EmbeddingModelDownloader
import com.theraipist.core.embedding.EmbeddingProviderFactory
import com.theraipist.core.local.LocalLLMService
import com.theraipist.core.local.ModelDownloader
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.prompt.TherapyPromptBuilder
import com.theraipist.core.safety.SafetyGuardrails
import com.theraipist.core.repository.GraphRepository
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.voice.LocalTtsService
import com.theraipist.core.voice.SttService
import com.theraipist.core.voice.TtsService
import com.theraipist.data.local.TherAIpistDatabase
import com.theraipist.data.local.download.AndroidModelDownloader
import com.theraipist.data.local.embedding.AndroidEmbeddingModelDownloader
import com.theraipist.data.local.embedding.OnnxEmbeddingProvider
import com.theraipist.data.local.llm.LlamaCppLocalService
import com.theraipist.data.repository.RoomGraphRepository
import com.theraipist.data.repository.RoomSessionRepository
import com.theraipist.data.settings.SecureSettings
import com.theraipist.data.voice.AndroidTtsService
import com.theraipist.data.voice.CloudSttService
import com.theraipist.data.voice.CloudTtsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TherAIpistDatabase =
        Room.databaseBuilder(context, TherAIpistDatabase::class.java, "theraipist.db")
            .build()

    @Provides
    @Singleton
    fun provideSessionRepository(db: TherAIpistDatabase): SessionRepository =
        RoomSessionRepository(db)

    @Provides
    @Singleton
    fun provideGraphRepository(db: TherAIpistDatabase): GraphRepository =
        RoomGraphRepository(db)

    @Provides
    @Singleton
    fun provideLocalLLMService(): LocalLLMService = LlamaCppLocalService()

    @Provides
    @Singleton
    fun provideModelDownloader(@ApplicationContext context: Context): ModelDownloader =
        AndroidModelDownloader(context)

    @Provides
    @Singleton
    fun provideLocalTtsService(@ApplicationContext context: Context): LocalTtsService =
        AndroidTtsService(context)

    @Provides
    @Singleton
    fun provideEmbeddingModelDownloader(@ApplicationContext context: Context): EmbeddingModelDownloader =
        AndroidEmbeddingModelDownloader(context)

    @Provides
    @Singleton
    fun provideEmbeddingProviderFactory(): EmbeddingProviderFactory =
        EmbeddingProviderFactory { onnxFile, vocabFile -> OnnxEmbeddingProvider(onnxFile, vocabFile) }

    @Provides
    @Singleton
    fun provideTtsService(client: HttpClient, secureSettings: SecureSettings): TtsService =
        CloudTtsService(client, secureSettings)

    @Provides
    @Singleton
    fun provideSttService(client: HttpClient, secureSettings: SecureSettings): SttService =
        CloudSttService(client, secureSettings)

    @Provides
    @Singleton
    fun provideSecureSettings(@ApplicationContext context: Context): SecureSettings =
        SecureSettings(context)

    @Provides
    fun provideModalityRouter(): ModalityRouter = ModalityRouter

    @Provides
    fun provideTherapyPromptBuilder(): TherapyPromptBuilder = TherapyPromptBuilder

    @Provides
    fun provideSafetyGuardrails(): SafetyGuardrails = SafetyGuardrails

    @Provides
    @Singleton
    fun provideChatService(client: HttpClient, secureSettings: SecureSettings): ChatService =
        CloudChatService(client, secureSettings)
}
