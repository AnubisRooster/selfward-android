package com.selfward.di

import android.content.Context
import androidx.room.Room
import com.selfward.core.chat.ChatService
import com.selfward.core.chat.CloudChatService
import com.selfward.core.embedding.EmbeddingModelDownloader
import com.selfward.core.embedding.EmbeddingProviderFactory
import com.selfward.core.local.LocalLLMService
import com.selfward.core.local.ModelDownloader
import com.selfward.core.intake.IntakeStore
import com.selfward.core.journal.DreamRepository
import com.selfward.core.journal.NoteRepository
import com.selfward.core.narrative.NarrativeStore
import com.selfward.core.lock.LockoutStore
import com.selfward.core.lock.PinLockout
import com.selfward.core.lock.PinService
import com.selfward.core.lock.PinStore
import com.selfward.data.export.ExportFiles
import com.selfward.data.export.NarrativePdfWriter
import com.selfward.data.intake.EncryptedIntakeStore
import com.selfward.data.lock.EncryptedPinStore
import com.selfward.data.lock.PrefsLockoutStore
import com.selfward.core.modality.ModalityRouter
import com.selfward.core.prompt.TherapyPromptBuilder
import com.selfward.core.safety.SafetyGuardrails
import com.selfward.core.dashboard.StatsRepository
import com.selfward.core.repository.GraphRepository
import com.selfward.core.repository.SessionRepository
import com.selfward.core.voice.LocalTtsService
import com.selfward.core.voice.SilenceClock
import com.selfward.core.voice.SpeechSource
import com.selfward.core.voice.TtsService
import com.selfward.data.local.MIGRATION_1_2
import com.selfward.data.local.MIGRATION_2_3
import com.selfward.data.local.MIGRATION_3_4
import com.selfward.data.local.MIGRATION_4_5
import com.selfward.data.local.SelfwardDatabase
import com.selfward.data.local.download.AndroidModelDownloader
import com.selfward.data.local.embedding.AndroidEmbeddingModelDownloader
import com.selfward.data.local.embedding.OnnxEmbeddingProvider
import com.selfward.data.local.llm.LlamaCppLocalService
import com.selfward.data.repository.RoomDreamRepository
import com.selfward.data.repository.RoomGraphRepository
import com.selfward.data.narrative.RoomNarrativeStore
import com.selfward.data.repository.RoomNoteRepository
import com.selfward.data.repository.RoomSessionRepository
import com.selfward.data.repository.RoomStatsRepository
import com.selfward.core.settings.SecureSettings
import com.selfward.data.settings.EncryptedSecureSettings
import com.selfward.data.voice.AndroidTtsService
import com.selfward.data.voice.ContinuousSpeechRecognizer
import com.selfward.data.voice.CoroutineSilenceClock
import com.selfward.data.voice.CloudTtsService
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
    fun provideDatabase(@ApplicationContext context: Context): SelfwardDatabase =
        Room.databaseBuilder(context, SelfwardDatabase::class.java, "selfward.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()

    @Provides
    @Singleton
    fun provideSessionRepository(db: SelfwardDatabase): SessionRepository =
        RoomSessionRepository(db)

    @Provides
    @Singleton
    fun provideGraphRepository(db: SelfwardDatabase): GraphRepository =
        RoomGraphRepository(db)

    @Provides
    @Singleton
    fun provideStatsRepository(db: SelfwardDatabase): StatsRepository =
        RoomStatsRepository(db)

    @Provides
    @Singleton
    fun provideLocalLLMService(): LocalLLMService = LlamaCppLocalService()

    @Provides
    @Singleton
    fun provideExportFiles(@ApplicationContext context: Context): ExportFiles =
        ExportFiles(context)

    @Provides
    @Singleton
    fun provideNarrativePdfWriter(): NarrativePdfWriter = NarrativePdfWriter()

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
    fun provideSpeechSource(@ApplicationContext context: Context): SpeechSource =
        ContinuousSpeechRecognizer(context)

    @Provides
    @Singleton
    fun provideSilenceClock(): SilenceClock = CoroutineSilenceClock()

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
    fun provideNarrativeStore(db: SelfwardDatabase): NarrativeStore = RoomNarrativeStore(db)

    @Provides
    @Singleton
    fun provideNoteRepository(db: SelfwardDatabase): NoteRepository = RoomNoteRepository(db)

    @Provides
    @Singleton
    fun provideDreamRepository(db: SelfwardDatabase): DreamRepository = RoomDreamRepository(db)

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
