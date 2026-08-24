package com.theraipist.di

import android.content.Context
import androidx.room.Room
import com.theraipist.core.chat.ChatService
import com.theraipist.core.chat.CloudChatService
import com.theraipist.core.embedding.EmbeddingModelDownloader
import com.theraipist.core.embedding.EmbeddingProviderFactory
import com.theraipist.core.local.LocalLLMService
import com.theraipist.core.local.ModelDownloader
import com.theraipist.core.intake.IntakeStore
import com.theraipist.core.journal.DreamRepository
import com.theraipist.core.journal.NoteRepository
import com.theraipist.core.lock.LockoutStore
import com.theraipist.core.lock.PinLockout
import com.theraipist.core.lock.PinService
import com.theraipist.core.lock.PinStore
import com.theraipist.data.intake.EncryptedIntakeStore
import com.theraipist.data.lock.EncryptedPinStore
import com.theraipist.data.lock.PrefsLockoutStore
import com.theraipist.core.modality.ModalityRouter
import com.theraipist.core.prompt.TherapyPromptBuilder
import com.theraipist.core.safety.SafetyGuardrails
import com.theraipist.core.repository.GraphRepository
import com.theraipist.core.repository.SessionRepository
import com.theraipist.core.voice.LocalTtsService
import com.theraipist.core.voice.TtsService
import com.theraipist.data.local.MIGRATION_1_2
import com.theraipist.data.local.TherAIpistDatabase
import com.theraipist.data.local.download.AndroidModelDownloader
import com.theraipist.data.local.embedding.AndroidEmbeddingModelDownloader
import com.theraipist.data.local.embedding.OnnxEmbeddingProvider
import com.theraipist.data.local.llm.LlamaCppLocalService
import com.theraipist.data.repository.RoomDreamRepository
import com.theraipist.data.repository.RoomGraphRepository
import com.theraipist.data.repository.RoomNoteRepository
import com.theraipist.data.repository.RoomSessionRepository
import com.theraipist.core.settings.SecureSettings
import com.theraipist.data.settings.EncryptedSecureSettings
import com.theraipist.data.voice.AndroidTtsService
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
            .addMigrations(MIGRATION_1_2)
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
    fun provideNoteRepository(db: TherAIpistDatabase): NoteRepository = RoomNoteRepository(db)

    @Provides
    @Singleton
    fun provideDreamRepository(db: TherAIpistDatabase): DreamRepository = RoomDreamRepository(db)

    @Provides
    @Singleton
    fun provideIntakeStore(@ApplicationContext context: Context): IntakeStore =
        EncryptedIntakeStore(context)

    @Provides
    @Singleton
    fun providePinStore(@ApplicationContext context: Context): PinStore =
        EncryptedPinStore(context)

    @Provides
    @Singleton
    fun provideLockoutStore(@ApplicationContext context: Context): LockoutStore =
        PrefsLockoutStore(context)

    @Provides
    @Singleton
    fun providePinService(pinStore: PinStore, lockoutStore: LockoutStore): PinService =
        PinService(pinStore, PinLockout(lockoutStore))

    @Provides
    @Singleton
    fun provideSecureSettings(@ApplicationContext context: Context): SecureSettings =
        EncryptedSecureSettings(context)

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
