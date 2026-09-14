# Graph Report - selfward-android  (2026-09-14)

## Corpus Check
- Large corpus: 252 files · ~534,391 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder.

## Summary
- 2449 nodes · 5347 edges · 141 communities (79 shown, 58 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 172 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- VoiceConversationTest
- NarrativeDocument
- Block
- ChatViewModel
- LocalModel
- MessageAnalyzer
- ChatViewModelTest
- EmbeddingModelSpec
- TtsRequest
- OpenRouterModel
- DownloadStatus
- CloudChatServiceTest
- ActiveSessionHolder
- SessionRepository
- FakeSecureSettings
- ChatViewModelTest.kt
- WordPieceTokenizer
- ModelRankingTest
- Message
- GraphExportTest
- .buildVm()
- ContinuousSpeechRecognizer
- TherapyGraph
- Intake
- DreamRepository
- Note
- ModelSettings
- GraphRepository
- AndroidEmbeddingModelDownloader
- Provider
- Tally
- DashboardTest
- .viewModel()
- SpiritualTradition
- DataModule.kt
- Persona
- TherapyInsightsTest
- VoiceTranscriptTest
- SettingsViewModel
- SessionSummary
- FakeSessionRepository
- TherapyModality
- PriceTiersTest
- MessageAnalyzerTest
- Role
- TherapyInsights
- GraphNode
- LocalLLMService
- OnboardingViewModel
- ModalityRouterTest
- ChatScreenTest
- UnusableModels
- ChatProtocol
- AndroidModelDownloader
- OpenRouterCatalog
- ChatServiceException
- AppRoot()
- JournalViewModel
- CycleDetectorTest
- PinLockoutTest
- NewSessionViewModelTest
- ChatViewModel.kt
- MemoryVectorStore
- DreamDao
- DataModule
- .buildHolder()
- CrisisResourcesTest
- com
- JournalViewModelTest
- ModelChoice
- GraphEdge
- InMemorySessionRepository
- DeviceVoice
- ProviderDefaultsAdoptionTest
- PairedDownloadTest
- DeviceVoiceRankingTest
- RoomStatsRepositoryTest
- OnboardingViewModelTest
- GraphHolder
- ChatScreen()
- GraphDao
- GraphViewModel.kt
- MessageDao
- SelfwardDatabase
- SessionDao
- RecognitionListener
- NarrativeViewModel.kt
- NarrativeSourcesTest
- AnthropicProtocol
- PinLockout
- SettingsViewModel.kt
- NewSessionViewModel
- ProviderDefaultsTest
- ChatProtocolTest
- FakeSessionRepository
- SilenceClock
- ExportFiles
- NoteDao
- OnboardingScreen.kt
- SafetyGuardrailsTest
- MigrationTest
- FakeEmbeddingModelDownloader
- OnboardingStep
- ModelRefusalTest
- MeanPoolingTest
- ExportFilesTest
- CompanionPersonality
- OnnxEmbeddingProvider
- NarrativeSource
- ExportedFile
- InsightEntity
- SelectionChips()
- DreamSymbolsTest
- FakeUnusable
- CrisisResources
- VoiceTranscript
- EncryptedSecureSettings
- AndroidSttService
- AndroidTtsService
- SseParserTest
- PersonaHolder
- PairedDownload
- Mappers.kt
- NarrativePromptTest
- JournalScreen.kt
- CosineSimilarityTest
- GGUFModelCatalogTest
- ModelSelectorTest
- FakeLocalLLMService
- FakeLocalTtsService
- ChatService
- SseParser
- CosineSimilarity
- InsightExtractorTest
- gradlew
- graphify_pipeline.py
- render.sh

## God Nodes (most connected - your core abstractions)
1. `Message` - 103 edges
2. `LocalModel` - 79 edges
3. `ChatViewModelTest` - 74 edges
4. `FakeSecureSettings` - 61 edges
5. `Provider` - 60 edges
6. `GraphNode` - 59 edges
7. `Persona` - 56 edges
8. `EmbeddingModelSpec` - 48 edges
9. `ChatViewModel` - 48 edges
10. `ModelSettings` - 41 edges

## Surprising Connections (you probably didn't know these)
- `NarrativePromptTest` --calls--> `NarrativeSource`  [INFERRED]
  app/src/test/java/com/selfward/core/narrative/NarrativeSourcesTest.kt → app/src/main/java/com/selfward/core/narrative/Narrative.kt
- `ExportFilesTest` --calls--> `ExportFiles`  [INFERRED]
  app/src/test/java/com/selfward/data/export/ExportFilesTest.kt → app/src/main/java/com/selfward/data/export/ExportFiles.kt
- `AppRoot()` --calls--> `MainScreen()`  [INFERRED]
  app/src/main/java/com/selfward/ui/AppRoot.kt → app/src/main/java/com/selfward/ui/MainScreen.kt
- `ChatViewModel` --calls--> `ChatUiState`  [INFERRED]
  app/src/main/java/com/selfward/ui/chat/ChatViewModel.kt → app/src/main/java/com/selfward/ui/chat/ChatUiState.kt
- `NarrativePdfWriterTest` --calls--> `NarrativePdfWriter`  [INFERRED]
  app/src/androidTest/java/com/selfward/data/export/NarrativePdfWriterTest.kt → app/src/main/java/com/selfward/data/export/NarrativePdfWriter.kt

## Import Cycles
- None detected.

## Communities (141 total, 58 thin omitted)

### Community 0 - "VoiceConversationTest"
Cohesion: 0.05
Nodes (21): ArmSilence, CancelSilence, Ended, Failed, Final, Partial, RecognizerEnded, RecognizerFailed (+13 more)

### Community 1 - "NarrativeDocument"
Cohesion: 0.07
Nodes (14): NarrativeDocument, NarrativeStore, RoomNarrativeStore, NarrativeExportTest, FailingChatService, FakeDreamRepository, FakeLocalTtsService, FakeNarrativeStore (+6 more)

### Community 2 - "Block"
Cohesion: 0.07
Nodes (22): ByteArray, NarrativePdfWriterTest, Block, NarrativeExport, Style, BODY, HEADING, SUBTITLE (+14 more)

### Community 3 - "ChatViewModel"
Cohesion: 0.06
Nodes (10): CrisisLevel, CRITICAL, WARNING, SafetyGuardrails, UtteranceProgressListener, ChatUiState, ChatViewModel, ByteArray (+2 more)

### Community 4 - "LocalModel"
Cohesion: 0.07
Nodes (8): LocalModel, ModelDownloader, ModelSelector, FakeModelDownloader, DownloadedModelDownloader, FakeModelDownloader, FakeModelDownloader, FakeModelDownloader

### Community 5 - "MessageAnalyzer"
Cohesion: 0.08
Nodes (32): GlobalStats, EdgeSpec, Extraction, Kind, MessageAnalyzer, NodeSpec, Relation, ExportOption (+24 more)

### Community 6 - "ChatViewModelTest"
Cohesion: 0.08
Nodes (8): FakeSpeechSource, ManualSilenceClock, ChatViewModelTest, LocalTtsService, ChunkedChatService, FailingChatService, FakeLocalTtsService, LocalTtsService

### Community 7 - "EmbeddingModelSpec"
Cohesion: 0.08
Nodes (8): EmbeddingModelCatalog, EmbeddingModelSpec, EmbeddingModelDownloader, DownloadProgress, DownloadManager, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 8 - "TtsRequest"
Cohesion: 0.09
Nodes (15): TtsRequest, ByteArray, Exception, TtsService, TtsServiceException, CloudTtsService, ByteArray, TtsRequestTest (+7 more)

### Community 9 - "OpenRouterModel"
Cohesion: 0.14
Nodes (5): ModelRanking, OpenRouterModel, HttpOpenRouterCatalog, HttpOpenRouterCatalogTest, MockEngine

### Community 10 - "DownloadStatus"
Cohesion: 0.09
Nodes (20): GGUFModelCatalog, DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING, AboutSection() (+12 more)

### Community 11 - "CloudChatServiceTest"
Cohesion: 0.13
Nodes (9): ViewModel, PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel, CloudChatServiceTest (+1 more)

### Community 12 - "ActiveSessionHolder"
Cohesion: 0.16
Nodes (6): ActiveSessionHolder, SessionsScreen(), SessionsViewModel, FakeSessionRepository, FakeStatsRepository, SessionsScreenTest

### Community 13 - "SessionRepository"
Cohesion: 0.07
Nodes (4): Session, SessionRepository, RoomSessionRepository, FakeSessionRepository

### Community 14 - "FakeSecureSettings"
Cohesion: 0.12
Nodes (6): FakeSecureSettings, FakeLocalTtsService, FakeOpenRouterCatalog, LocalTtsService, SettingsViewModelTest, LocalTtsService

### Community 15 - "ChatViewModelTest.kt"
Cohesion: 0.09
Nodes (10): ChatService, ChatService, ChatService, FakeCatalog, FakeProviderCatalog, FakeUnusable, ChatService, Flow (+2 more)

### Community 16 - "WordPieceTokenizer"
Cohesion: 0.10
Nodes (6): FloatArray, TokenizedInput, TokenizerConfig, WordPieceTokenizer, WordPieceTokenizerTest, LongArray

### Community 18 - "Message"
Cohesion: 0.11
Nodes (4): Message, AnthropicProtocolTest, ChatService, Flow

### Community 19 - "GraphExportTest"
Cohesion: 0.20
Nodes (4): GraphExportTest, Document, JsonArray, JsonObject

### Community 20 - ".buildVm()"
Cohesion: 0.12
Nodes (4): FakeChatService, FakeIntakeStore, MissingKeyChatService, ProgrammableLocalLLMService

### Community 21 - "ContinuousSpeechRecognizer"
Cohesion: 0.11
Nodes (7): SpeechSource, ContinuousSpeechRecognizer, RecognitionListener, Bundle, ByteArray, RecognitionListener, SpeechRecognizer

### Community 22 - "TherapyGraph"
Cohesion: 0.13
Nodes (3): TherapyGraph, TherapyGraphTest, TherapyGraphUpsertTest

### Community 23 - "Intake"
Cohesion: 0.12
Nodes (7): Intake, IntakeStore, EncryptedIntakeStore, SharedPreferences, IntakeContextTest, FakeIntakeStore, FakeIntakeStore

### Community 24 - "DreamRepository"
Cohesion: 0.12
Nodes (8): Dream, DreamRepository, DreamSymbols, joinToList(), RoomDreamRepository, splitList(), toDomain(), FakeDreamRepository

### Community 25 - "Note"
Cohesion: 0.12
Nodes (8): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, FakeNoteRepository

### Community 26 - "ModelSettings"
Cohesion: 0.13
Nodes (6): ModelSettings, MainApplication, LocalTtsService, ModelSettingsTest, SpyingLocalTtsService, Application

### Community 27 - "GraphRepository"
Cohesion: 0.11
Nodes (5): EmbeddingProviderFactory, GraphRepository, GraphSnapshot, FakeGraphRepository, FakeGraphRepository

### Community 29 - "Provider"
Cohesion: 0.15
Nodes (7): ProviderDefaults, ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings

### Community 30 - "Tally"
Cohesion: 0.15
Nodes (4): StatsRepository, Tally, RoomStatsRepository, FailingStatsRepository

### Community 32 - ".viewModel()"
Cohesion: 0.15
Nodes (3): FakeLockoutStore, FakePinStore, PinViewModelTest

### Community 33 - "SpiritualTradition"
Cohesion: 0.10
Nodes (17): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, CrisisPattern, SpiritualTradition, BUDDHIST (+9 more)

### Community 34 - "DataModule.kt"
Cohesion: 0.16
Nodes (6): LockoutStore, PinService, PinStore, EncryptedPinStore, SharedPreferences, PrefsLockoutStore

### Community 35 - "Persona"
Cohesion: 0.15
Nodes (4): Persona, TherapyPromptBuilder, TherapyPromptBuilderTest, SessionMappersTest

### Community 39 - "SessionSummary"
Cohesion: 0.21
Nodes (10): Dashboard, SessionStats, SessionSummary, ActiveList(), ArchiveList(), formatTimestamp(), plural(), SessionBadges() (+2 more)

### Community 40 - "FakeSessionRepository"
Cohesion: 0.14
Nodes (3): FakeSessionRepository, FakeStatsRepository, SessionsViewModelTest

### Community 41 - "TherapyModality"
Cohesion: 0.12
Nodes (11): ModalityRouter, TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL (+3 more)

### Community 44 - "Role"
Cohesion: 0.12
Nodes (9): PersonaKind, COMPANION, SPIRITUAL, THERAPIST, Role, ASSISTANT, SYSTEM, USER (+1 more)

### Community 45 - "TherapyInsights"
Cohesion: 0.22
Nodes (4): CycleDetector, Cycles, Result, TherapyInsights

### Community 46 - "GraphNode"
Cohesion: 0.14
Nodes (3): GraphNode, RoomGraphRepository, RoomGraphRepositoryTest

### Community 47 - "LocalLLMService"
Cohesion: 0.18
Nodes (5): Flow, LocalLLMService, Flow, LlamaCppLocalService, LlamaModel

### Community 48 - "OnboardingViewModel"
Cohesion: 0.18
Nodes (3): ViewModel, OnboardingUiState, OnboardingViewModel

### Community 50 - "ChatScreenTest"
Cohesion: 0.20
Nodes (5): ChatScreenTest, FakeCatalog, FakeChatService, FakeProviderCatalog, ChatService

### Community 52 - "ChatProtocol"
Cohesion: 0.13
Nodes (12): ApiError, ChatProtocol, ChatRequest, ErrorEnvelope, ErrorMetadata, ReqMessage, StreamChoice, StreamChunk (+4 more)

### Community 54 - "OpenRouterCatalog"
Cohesion: 0.21
Nodes (5): OpenRouterCatalog, ProviderCatalog, Context, HttpClient, NetworkModule

### Community 55 - "ChatServiceException"
Cohesion: 0.23
Nodes (7): ChatServiceException, Exception, MissingApiKeyException, CloudChatService, ChatService, Flow, HttpResponse

### Community 56 - "AppRoot()"
Cohesion: 0.20
Nodes (10): Bundle, MainActivity, AppRoot(), AppRootViewModel, ViewModel, NumberPad(), PinKey(), PinScreen() (+2 more)

### Community 57 - "JournalViewModel"
Cohesion: 0.21
Nodes (3): JournalUiState, JournalViewModel, ViewModel

### Community 59 - "PinLockoutTest"
Cohesion: 0.26
Nodes (3): Clock, FakeLockoutStore, PinLockoutTest

### Community 61 - "ChatViewModel.kt"
Cohesion: 0.15
Nodes (3): PriceTiers, ModelRefusal, InsightExtractor

### Community 62 - "MemoryVectorStore"
Cohesion: 0.18
Nodes (4): FloatArray, MemoryVectorStore, ScoredId, MemoryVectorStoreTest

### Community 63 - "DreamDao"
Cohesion: 0.17
Nodes (3): DreamDao, SessionCountRow, DreamEntity

### Community 64 - "DataModule"
Cohesion: 0.21
Nodes (5): DataModule, ChatService, Context, HttpClient, LocalTtsService

### Community 65 - ".buildHolder()"
Cohesion: 0.22
Nodes (4): FakeEmbeddingProvider, GraphHolderTest, FloatArray, RecordingGraphRepository

### Community 67 - "com"
Cohesion: 0.21
Nodes (3): FakeEmbeddingModelDownloader, FakeLocalLLMService, com

### Community 72 - "DeviceVoice"
Cohesion: 0.21
Nodes (6): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD

### Community 78 - "GraphHolder"
Cohesion: 0.32
Nodes (3): EmbeddingProvider, FloatArray, GraphHolder

### Community 79 - "ChatScreen()"
Cohesion: 0.24
Nodes (12): VoicePhase, IDLE, LISTENING, SPEAKING, THINKING, ChatScreen(), Modifier, MessageBubble() (+4 more)

### Community 80 - "GraphDao"
Cohesion: 0.23
Nodes (3): GraphDao, GraphEdgeEntity, GraphNodeEntity

### Community 81 - "GraphViewModel.kt"
Cohesion: 0.23
Nodes (7): GraphFormat, GRAPHML, JSON, GraphViewModel, InsightsUiState, StateFlow, ViewModel

### Community 82 - "MessageDao"
Cohesion: 0.23
Nodes (4): MessageCountRow, MessageDao, ModalityCountRow, MessageEntity

### Community 83 - "SelfwardDatabase"
Cohesion: 0.26
Nodes (4): NarrativeDao, NarrativeEntity, SelfwardDatabase, RoomDatabase

### Community 85 - "RecognitionListener"
Cohesion: 0.23
Nodes (3): RecognitionListener, Bundle, ByteArray

### Community 86 - "NarrativeViewModel.kt"
Cohesion: 0.29
Nodes (4): StateFlow, ViewModel, NarrativeUiState, NarrativeViewModel

### Community 88 - "AnthropicProtocol"
Cohesion: 0.24
Nodes (6): AnthropicProtocol, ApiError, ChatRequest, ReqMessage, StreamDelta, StreamEvent

### Community 89 - "PinLockout"
Cohesion: 0.27
Nodes (5): Incorrect, LockedOut, PinAttempt, PinLockout, Success

### Community 90 - "SettingsViewModel.kt"
Cohesion: 0.22
Nodes (3): LocalTtsService, VoiceCatalog, ViewModel

### Community 91 - "NewSessionViewModel"
Cohesion: 0.25
Nodes (3): ViewModel, NewSessionUiState, NewSessionViewModel

### Community 95 - "SilenceClock"
Cohesion: 0.31
Nodes (3): SilenceClock, CoroutineSilenceClock, Job

### Community 98 - "OnboardingScreen.kt"
Cohesion: 0.42
Nodes (9): AboutYouStep(), ApiKeyStep(), DisclaimerStep(), GoalsStep(), IntakeStep(), LocalModelStep(), OnboardingScreen(), OnDeviceOnlyNote() (+1 more)

### Community 102 - "OnboardingStep"
Cohesion: 0.22
Nodes (9): OnboardingStep, ABOUT_YOU, API_KEY, CONCERNS, DISCLAIMER, GOALS, HISTORY, LOCAL_MODEL (+1 more)

### Community 106 - "CompanionPersonality"
Cohesion: 0.25
Nodes (7): CompanionPersonality, BOLD, CALM, CHEERFUL, DEEP, PLAYFUL, WARM

### Community 107 - "OnnxEmbeddingProvider"
Cohesion: 0.29
Nodes (3): MeanPooling, FloatArray, OnnxEmbeddingProvider

### Community 108 - "NarrativeSource"
Cohesion: 0.36
Nodes (3): NarrativePrompt, NarrativeSource, NarrativeSources

### Community 109 - "ExportedFile"
Cohesion: 0.32
Nodes (6): ExportedFile, share(), NarrativeFormat, MARKDOWN, PDF, Intent

### Community 111 - "SelectionChips()"
Cohesion: 0.43
Nodes (6): Modifier, prettifyEnumName(), SelectionChips(), Field(), NewSessionScreen(), T

### Community 117 - "AndroidSttService"
Cohesion: 0.48
Nodes (3): AndroidSttService, RecognitionListener, SpeechRecognizer

### Community 122 - "Mappers.kt"
Cohesion: 0.53
Nodes (3): toDomain(), toEntity(), toSessionEntity()

### Community 124 - "JournalScreen.kt"
Cohesion: 0.80
Nodes (4): DreamRow(), formatDate(), JournalScreen(), NoteRow()

### Community 134 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **104 isolated node(s):** `CrisisPattern`, `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH` (+99 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 602 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **58 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Message` connect `Message` to `FakeLocalLLMService`, `NarrativeDocument`, `ChatService`, `ChatViewModel`, `ChatViewModelTest`, `CloudChatServiceTest`, `SessionRepository`, `ChatViewModelTest.kt`, `.buildVm()`, `Note`, `SpiritualTradition`, `Persona`, `FakeSessionRepository`, `Role`, `LocalLLMService`, `ChatScreenTest`, `ChatProtocol`, `ChatServiceException`, `ChatViewModel.kt`, `InMemorySessionRepository`, `RoomStatsRepositoryTest`, `ChatScreen()`, `NarrativeViewModel.kt`, `AnthropicProtocol`, `ChatProtocolTest`, `FakeSessionRepository`, `Mappers.kt`?**
  _High betweenness centrality (0.120) - this node is a cross-community bridge._
- **Why does `GraphNode` connect `GraphNode` to `ChatViewModel`, `MessageAnalyzer`, `ChatViewModelTest`, `ChatViewModelTest.kt`, `GraphExportTest`, `TherapyGraph`, `Note`, `GraphRepository`, `DashboardTest`, `Persona`, `TherapyInsightsTest`, `SessionSummary`, `TherapyModality`, `Role`, `TherapyInsights`, `CycleDetectorTest`, `.buildHolder()`, `GraphEdge`, `RoomStatsRepositoryTest`, `GraphHolder`, `GraphViewModel.kt`, `Mappers.kt`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **Why does `Persona` connect `Persona` to `SpiritualTradition`, `NarrativeDocument`, `ChatViewModel`, `JournalViewModelTest`, `ChatViewModelTest`, `InMemorySessionRepository`, `FakeSessionRepository`, `Role`, `SessionRepository`, `RoomStatsRepositoryTest`, `ChatViewModelTest.kt`, `ChatScreenTest`, `PersonaHolder`, `Note`, `Mappers.kt`, `NewSessionViewModel`, `NewSessionViewModelTest`, `ChatViewModel.kt`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **What connects `CrisisPattern`, `THERAPIST`, `COMPANION` to the rest of the system?**
  _104 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `VoiceConversationTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05405405405405406 - nodes in this community are weakly interconnected._
- **Should `NarrativeDocument` be split into smaller, more focused modules?**
  _Cohesion score 0.0675990675990676 - nodes in this community are weakly interconnected._
- **Should `Block` be split into smaller, more focused modules?**
  _Cohesion score 0.07291666666666667 - nodes in this community are weakly interconnected._