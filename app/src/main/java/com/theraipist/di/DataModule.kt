package com.theraipist.di

import android.content.Context
import androidx.room.Room
import com.theraipist.core.chat.ApiConfig
import com.theraipist.core.chat.ChatService
import com.theraipist.core.chat.CloudChatService
import com.theraipist.core.local.LocalLLMService
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.prompt.TherapyPromptBuilder
import com.theraipist.core.safety.SafetyGuardrails
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.voice.SttService
import com.theraipist.core.voice.TtsService
import com.theraipist.data.local.TherAIpistDatabase
import com.theraipist.data.local.llm.LlamaCppLocalService
import com.theraipist.data.repository.RoomSessionRepository
import com.theraipist.data.voice.CloudSttService
import com.theraipist.data.voice.CloudTtsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import com.theraipist.core.chat.ApiConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TherAIpistDatabase =
        Room.databaseBuilder(context, TherAIpistDatabase::class.java, "theraipist.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideSessionRepository(db: TherAIpistDatabase): SessionRepository =
        RoomSessionRepository(db)

    @Provides
    @Singleton
    fun provideLocalLLMService(): LocalLLMService = LlamaCppLocalService()

    @Provides
    @Singleton
    fun provideTtsService(client: HttpClient, apiConfig: ApiConfig): TtsService =
        CloudTtsService(client, apiConfig)

    @Provides
    @Singleton
    fun provideSttService(client: HttpClient, apiConfig: ApiConfig): SttService =
        CloudSttService(client, apiConfig)

    @Provides
    fun provideModalityRouter(): ModalityRouter = ModalityRouter

    @Provides
    fun provideTherapyPromptBuilder(): TherapyPromptBuilder = TherapyPromptBuilder

    @Provides
    fun provideSafetyGuardrails(): SafetyGuardrails = SafetyGuardrails

    @Provides
    @Singleton
    fun provideChatService(client: HttpClient, apiConfig: ApiConfig): ChatService =
        CloudChatService(client, apiConfig)
}
