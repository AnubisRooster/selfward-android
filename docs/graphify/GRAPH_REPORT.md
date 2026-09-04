# Graph Report - selfward-android  (2026-09-04)

## Corpus Check
- Large corpus: 250 files · ~531,914 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder.

## Summary
- 2449 nodes · 5305 edges · 134 communities (73 shown, 56 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 172 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Block
- VoiceConversationTest
- MessageAnalyzer
- LocalModel
- DeviceVoiceRankingTest
- PersonaKind
- EmbeddingModelSpec
- TtsRequest
- OpenRouterModel
- CloudChatServiceTest
- ChatViewModelTest
- ActiveSessionHolder
- GraphNode
- WordPieceTokenizer
- FakeSecureSettings
- Provider
- Message
- SessionRepository
- ModelRankingTest
- UnusableModels
- Intake
- Persona
- GraphExportTest
- SessionSummary
- ContinuousSpeechRecognizer
- ChatViewModelTest.kt
- DreamRepository
- SettingsScreen()
- NarrativeViewModelTest
- SettingsViewModel
- ModelSettings
- Note
- .emit()
- ChatViewModel.kt
- Tally
- DashboardTest
- AndroidEmbeddingModelDownloader
- ChatScreenTest.kt
- ChatScreenTest
- .viewModel()
- GraphHolder
- DataModule.kt
- TherapyInsightsTest
- VoiceTranscriptTest
- GraphRepository
- ChatViewModel
- .buildVm()
- TherapyModality
- RecognitionListener
- PriceTiersTest
- MessageAnalyzerTest
- GraphEdge
- TherapyInsights
- OnboardingViewModel
- ModalityRouterTest
- Role
- AndroidModelDownloader
- JournalViewModel
- ChatProtocol
- DataModule
- AppRoot()
- CycleDetectorTest
- PinLockoutTest
- NewSessionViewModelTest
- ModelChoice
- MemoryVectorStore
- DreamDao
- CrisisResourcesTest
- JournalViewModelTest
- DownloadStatus
- NarrativeSource
- ProviderDefaultsAdoptionTest
- .buildHolder()
- PairedDownloadTest
- RoomStatsRepositoryTest
- OnboardingViewModelTest
- NarrativeDocument
- InMemorySessionRepository
- ChatScreen()
- GraphDao
- GraphViewModel.kt
- FakeStatsRepository
- SafetyGuardrails
- MessageDao
- SelfwardDatabase
- SessionDao
- NarrativeViewModel.kt
- NarrativeSourcesTest
- LlamaCppLocalService
- .onDone()
- ProviderDefaultsTest
- NarrativeExportTest
- FakeSessionRepository
- PinLockout
- SilenceClock
- NoteDao
- OnboardingScreen.kt
- SafetyGuardrailsTest
- MigrationTest
- FakeEmbeddingModelDownloader
- PriceTiers
- OnboardingStep
- ModelRefusalTest
- MeanPoolingTest
- FakeSessionRepository
- ExportedFile
- InsightEntity
- DreamSymbolsTest
- FakeUnusable
- RecordingLocalLLM
- CrisisResources
- SseParserTest
- .streamReply()
- .buildGraphHolder()
- FakeDreamRepository
- ModelRefusal
- CosineSimilarityTest
- GGUFModelCatalogTest
- ModelSelectorTest
- FakeLocalTtsService
- LocalTtsService
- FakeLocalTtsService
- SseParser
- CosineSimilarity
- PersonaHolder
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
- `ChatViewModel` --calls--> `ChatUiState`  [INFERRED]
  app/src/main/java/com/selfward/ui/chat/ChatViewModel.kt → app/src/main/java/com/selfward/ui/chat/ChatUiState.kt
- `AppRoot()` --calls--> `MainScreen()`  [INFERRED]
  app/src/main/java/com/selfward/ui/AppRoot.kt → app/src/main/java/com/selfward/ui/MainScreen.kt
- `NarrativePdfWriterTest` --calls--> `NarrativePdfWriter`  [INFERRED]
  app/src/androidTest/java/com/selfward/data/export/NarrativePdfWriterTest.kt → app/src/main/java/com/selfward/data/export/NarrativePdfWriter.kt
- `NewSessionViewModelTest` --calls--> `ActiveSessionHolder`  [EXTRACTED]
  app/src/test/java/com/selfward/ui/newsession/NewSessionViewModelTest.kt → app/src/main/java/com/selfward/core/ActiveSessionHolder.kt
- `GraphHolder` --calls--> `TherapyGraph`  [EXTRACTED]
  app/src/main/java/com/selfward/core/GraphHolder.kt → app/src/main/java/com/selfward/core/graph/TherapyGraph.kt

## Import Cycles
- None detected.

## Communities (134 total, 56 thin omitted)

### Community 0 - "Block"
Cohesion: 0.05
Nodes (25): ByteArray, NarrativePdfWriterTest, Block, NarrativeExport, Style, BODY, HEADING, SUBTITLE (+17 more)

### Community 1 - "VoiceConversationTest"
Cohesion: 0.05
Nodes (21): ArmSilence, CancelSilence, Ended, Failed, Final, Partial, RecognizerEnded, RecognizerFailed (+13 more)

### Community 2 - "MessageAnalyzer"
Cohesion: 0.06
Nodes (42): GlobalStats, EdgeSpec, Extraction, Kind, MessageAnalyzer, NodeSpec, Relation, ExportOption (+34 more)

### Community 3 - "LocalModel"
Cohesion: 0.07
Nodes (8): LocalModel, ModelDownloader, ModelSelector, FakeModelDownloader, DownloadedModelDownloader, FakeModelDownloader, FakeModelDownloader, FakeModelDownloader

### Community 4 - "DeviceVoiceRankingTest"
Cohesion: 0.06
Nodes (12): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD, LocalTtsService, VoiceCatalog (+4 more)

### Community 5 - "PersonaKind"
Cohesion: 0.06
Nodes (31): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, CompanionPersonality, BOLD, CALM (+23 more)

### Community 6 - "EmbeddingModelSpec"
Cohesion: 0.09
Nodes (8): EmbeddingModelCatalog, EmbeddingModelSpec, EmbeddingModelDownloader, DownloadProgress, DownloadManager, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 7 - "TtsRequest"
Cohesion: 0.09
Nodes (15): TtsRequest, ByteArray, Exception, TtsService, TtsServiceException, CloudTtsService, ByteArray, TtsRequestTest (+7 more)

### Community 8 - "OpenRouterModel"
Cohesion: 0.14
Nodes (5): ModelRanking, OpenRouterModel, HttpOpenRouterCatalog, HttpOpenRouterCatalogTest, MockEngine

### Community 9 - "CloudChatServiceTest"
Cohesion: 0.13
Nodes (9): ViewModel, PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel, CloudChatServiceTest (+1 more)

### Community 10 - "ChatViewModelTest"
Cohesion: 0.10
Nodes (5): FakeSpeechSource, ManualSilenceClock, ChatViewModelTest, FailingChatService, FakeLocalTtsService

### Community 11 - "ActiveSessionHolder"
Cohesion: 0.16
Nodes (6): ActiveSessionHolder, SessionsScreen(), SessionsViewModel, FakeSessionRepository, FakeStatsRepository, SessionsScreenTest

### Community 12 - "GraphNode"
Cohesion: 0.10
Nodes (4): GraphNode, TherapyGraph, TherapyGraphTest, TherapyGraphUpsertTest

### Community 13 - "WordPieceTokenizer"
Cohesion: 0.09
Nodes (7): FloatArray, MeanPooling, TokenizedInput, TokenizerConfig, WordPieceTokenizer, WordPieceTokenizerTest, LongArray

### Community 14 - "FakeSecureSettings"
Cohesion: 0.11
Nodes (6): FakeSecureSettings, FakeLocalTtsService, FakeOpenRouterCatalog, LocalTtsService, SettingsViewModelTest, LocalTtsService

### Community 15 - "Provider"
Cohesion: 0.14
Nodes (8): ProviderDefaults, ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings, EncryptedSecureSettings

### Community 16 - "Message"
Cohesion: 0.09
Nodes (3): Message, AnthropicProtocolTest, ChatProtocolTest

### Community 17 - "SessionRepository"
Cohesion: 0.09
Nodes (6): Session, SessionRepository, toDomain(), toEntity(), toSessionEntity(), RoomSessionRepository

### Community 19 - "UnusableModels"
Cohesion: 0.10
Nodes (6): OpenRouterCatalog, UnusableModels, PrefsUnusableModels, Context, HttpClient, NetworkModule

### Community 20 - "Intake"
Cohesion: 0.11
Nodes (8): Intake, IntakeContext, IntakeStore, EncryptedIntakeStore, SharedPreferences, IntakeContextTest, FakeIntakeStore, FakeIntakeStore

### Community 21 - "Persona"
Cohesion: 0.11
Nodes (5): Persona, TherapyPromptBuilder, TherapyPromptBuilderTest, SessionMappersTest, RoomSessionRepositoryTest

### Community 22 - "GraphExportTest"
Cohesion: 0.20
Nodes (4): GraphExportTest, Document, JsonArray, JsonObject

### Community 23 - "SessionSummary"
Cohesion: 0.13
Nodes (11): Dashboard, SessionStats, SessionSummary, ActiveList(), ArchiveList(), formatTimestamp(), plural(), SessionBadges() (+3 more)

### Community 24 - "ContinuousSpeechRecognizer"
Cohesion: 0.11
Nodes (7): SpeechSource, ContinuousSpeechRecognizer, RecognitionListener, Bundle, ByteArray, RecognitionListener, SpeechRecognizer

### Community 25 - "ChatViewModelTest.kt"
Cohesion: 0.09
Nodes (8): ChatService, ChatService, ChatService, FakeCatalog, FakeProviderCatalog, FakeUnusable, Flow, RefusesFirstModel

### Community 26 - "DreamRepository"
Cohesion: 0.12
Nodes (8): Dream, DreamRepository, DreamSymbols, joinToList(), RoomDreamRepository, splitList(), toDomain(), FakeDreamRepository

### Community 27 - "SettingsScreen()"
Cohesion: 0.14
Nodes (13): AboutSection(), DeviceVoicePicker(), DownloadActionButton(), DownloadProgressAndError(), com, OpenRouterModelRow(), OpenRouterModelSection(), providerLabel() (+5 more)

### Community 28 - "NarrativeViewModelTest"
Cohesion: 0.22
Nodes (6): FailingChatService, FakeNarrativeStore, FakeNoteRepository, ChatService, NarrativeViewModelTest, RecordingChatService

### Community 29 - "SettingsViewModel"
Cohesion: 0.11
Nodes (3): GGUFModelCatalog, ViewModel, SettingsViewModel

### Community 30 - "ModelSettings"
Cohesion: 0.13
Nodes (6): ModelSettings, MainApplication, LocalTtsService, ModelSettingsTest, SpyingLocalTtsService, Application

### Community 31 - "Note"
Cohesion: 0.12
Nodes (8): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, FakeNoteRepository

### Community 32 - ".emit()"
Cohesion: 0.11
Nodes (3): FakeChatService, FakeIntakeStore, ProgrammableLocalLLMService

### Community 33 - "ChatViewModel.kt"
Cohesion: 0.14
Nodes (10): ChatService, ChatServiceException, Exception, Flow, MissingApiKeyException, CloudChatService, ChatService, Flow (+2 more)

### Community 34 - "Tally"
Cohesion: 0.15
Nodes (4): StatsRepository, Tally, RoomStatsRepository, FailingStatsRepository

### Community 37 - "ChatScreenTest.kt"
Cohesion: 0.12
Nodes (5): Flow, LocalLLMService, FakeEmbeddingModelDownloader, FakeLocalLLMService, com

### Community 38 - "ChatScreenTest"
Cohesion: 0.14
Nodes (6): ChatScreenTest, FakeCatalog, FakeChatService, FakeLocalLLMService, FakeProviderCatalog, ChatService

### Community 39 - ".viewModel()"
Cohesion: 0.15
Nodes (3): FakeLockoutStore, FakePinStore, PinViewModelTest

### Community 40 - "GraphHolder"
Cohesion: 0.16
Nodes (6): EmbeddingProvider, FloatArray, EmbeddingProviderFactory, GraphHolder, FloatArray, OnnxEmbeddingProvider

### Community 41 - "DataModule.kt"
Cohesion: 0.14
Nodes (6): LockoutStore, PinService, PinStore, EncryptedPinStore, SharedPreferences, PrefsLockoutStore

### Community 44 - "GraphRepository"
Cohesion: 0.11
Nodes (4): GraphRepository, GraphSnapshot, RoomGraphRepository, FakeGraphRepository

### Community 46 - ".buildVm()"
Cohesion: 0.14
Nodes (5): ChatService, ChunkedChatService, ChatService, MissingKeyChatService, ObservingChatService

### Community 47 - "TherapyModality"
Cohesion: 0.12
Nodes (11): ModalityRouter, TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL (+3 more)

### Community 48 - "RecognitionListener"
Cohesion: 0.14
Nodes (6): AndroidSttService, RecognitionListener, Bundle, ByteArray, RecognitionListener, SpeechRecognizer

### Community 51 - "GraphEdge"
Cohesion: 0.18
Nodes (3): GraphExport, GraphEdge, RoomGraphRepositoryTest

### Community 52 - "TherapyInsights"
Cohesion: 0.22
Nodes (4): CycleDetector, Cycles, Result, TherapyInsights

### Community 53 - "OnboardingViewModel"
Cohesion: 0.18
Nodes (3): ViewModel, OnboardingUiState, OnboardingViewModel

### Community 55 - "Role"
Cohesion: 0.14
Nodes (10): AnthropicProtocol, ApiError, ChatRequest, ReqMessage, StreamDelta, StreamEvent, Role, ASSISTANT (+2 more)

### Community 57 - "JournalViewModel"
Cohesion: 0.19
Nodes (3): JournalUiState, JournalViewModel, ViewModel

### Community 58 - "ChatProtocol"
Cohesion: 0.14
Nodes (12): ApiError, ChatProtocol, ChatRequest, ErrorEnvelope, ErrorMetadata, ReqMessage, StreamChoice, StreamChunk (+4 more)

### Community 59 - "DataModule"
Cohesion: 0.20
Nodes (5): DataModule, ChatService, Context, HttpClient, LocalTtsService

### Community 60 - "AppRoot()"
Cohesion: 0.19
Nodes (10): Bundle, MainActivity, AppRoot(), AppRootViewModel, ViewModel, NumberPad(), PinKey(), PinScreen() (+2 more)

### Community 62 - "PinLockoutTest"
Cohesion: 0.26
Nodes (3): Clock, FakeLockoutStore, PinLockoutTest

### Community 64 - "ModelChoice"
Cohesion: 0.28
Nodes (3): ModelChoice, ProviderCatalog, HttpProviderCatalog

### Community 65 - "MemoryVectorStore"
Cohesion: 0.17
Nodes (4): FloatArray, MemoryVectorStore, ScoredId, MemoryVectorStoreTest

### Community 66 - "DreamDao"
Cohesion: 0.17
Nodes (3): DreamDao, SessionCountRow, DreamEntity

### Community 69 - "DownloadStatus"
Cohesion: 0.18
Nodes (8): DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING, Half, PairedDownload

### Community 70 - "NarrativeSource"
Cohesion: 0.18
Nodes (4): NarrativePrompt, NarrativeSource, NarrativeSources, NarrativePromptTest

### Community 72 - ".buildHolder()"
Cohesion: 0.24
Nodes (4): FakeEmbeddingProvider, GraphHolderTest, FloatArray, RecordingGraphRepository

### Community 76 - "NarrativeDocument"
Cohesion: 0.22
Nodes (3): NarrativeDocument, NarrativeStore, RoomNarrativeStore

### Community 78 - "ChatScreen()"
Cohesion: 0.24
Nodes (12): VoicePhase, IDLE, LISTENING, SPEAKING, THINKING, ChatScreen(), Modifier, MessageBubble() (+4 more)

### Community 79 - "GraphDao"
Cohesion: 0.23
Nodes (3): GraphDao, GraphEdgeEntity, GraphNodeEntity

### Community 80 - "GraphViewModel.kt"
Cohesion: 0.23
Nodes (7): GraphFormat, GRAPHML, JSON, GraphViewModel, InsightsUiState, StateFlow, ViewModel

### Community 82 - "SafetyGuardrails"
Cohesion: 0.18
Nodes (5): CrisisLevel, CRITICAL, WARNING, SafetyGuardrails, ChatUiState

### Community 83 - "MessageDao"
Cohesion: 0.23
Nodes (4): MessageCountRow, MessageDao, ModalityCountRow, MessageEntity

### Community 84 - "SelfwardDatabase"
Cohesion: 0.26
Nodes (4): NarrativeDao, NarrativeEntity, SelfwardDatabase, RoomDatabase

### Community 86 - "NarrativeViewModel.kt"
Cohesion: 0.29
Nodes (4): StateFlow, ViewModel, NarrativeUiState, NarrativeViewModel

### Community 88 - "LlamaCppLocalService"
Cohesion: 0.27
Nodes (3): Flow, LlamaCppLocalService, LlamaModel

### Community 93 - "PinLockout"
Cohesion: 0.31
Nodes (5): Incorrect, LockedOut, PinAttempt, PinLockout, Success

### Community 94 - "SilenceClock"
Cohesion: 0.29
Nodes (3): SilenceClock, CoroutineSilenceClock, Job

### Community 96 - "OnboardingScreen.kt"
Cohesion: 0.42
Nodes (9): AboutYouStep(), ApiKeyStep(), DisclaimerStep(), GoalsStep(), IntakeStep(), LocalModelStep(), OnboardingScreen(), OnDeviceOnlyNote() (+1 more)

### Community 102 - "OnboardingStep"
Cohesion: 0.22
Nodes (9): OnboardingStep, ABOUT_YOU, API_KEY, CONCERNS, DISCLAIMER, GOALS, HISTORY, LOCAL_MODEL (+1 more)

### Community 106 - "ExportedFile"
Cohesion: 0.32
Nodes (6): ExportedFile, share(), NarrativeFormat, MARKDOWN, PDF, Intent

### Community 127 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **104 isolated node(s):** `CrisisPattern`, `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH` (+99 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 623 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **56 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Message` connect `Message` to `PersonaKind`, `CloudChatServiceTest`, `ChatViewModelTest`, `SessionRepository`, `Persona`, `SessionSummary`, `ChatViewModelTest.kt`, `NarrativeViewModelTest`, `Note`, `.emit()`, `ChatViewModel.kt`, `Tally`, `ChatScreenTest.kt`, `ChatScreenTest`, `ChatViewModel`, `.buildVm()`, `Role`, `ChatProtocol`, `RoomStatsRepositoryTest`, `NarrativeDocument`, `InMemorySessionRepository`, `ChatScreen()`, `SafetyGuardrails`, `NarrativeViewModel.kt`, `LlamaCppLocalService`, `FakeSessionRepository`, `FakeSessionRepository`, `RecordingLocalLLM`, `.streamReply()`?**
  _High betweenness centrality (0.120) - this node is a cross-community bridge._
- **Why does `GraphNode` connect `GraphNode` to `MessageAnalyzer`, `SessionRepository`, `Persona`, `GraphExportTest`, `SessionSummary`, `ChatViewModelTest.kt`, `Note`, `DashboardTest`, `ChatScreenTest.kt`, `GraphHolder`, `TherapyInsightsTest`, `GraphRepository`, `TherapyModality`, `GraphEdge`, `TherapyInsights`, `CycleDetectorTest`, `.buildHolder()`, `RoomStatsRepositoryTest`, `GraphViewModel.kt`, `SafetyGuardrails`, `.buildGraphHolder()`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **Why does `Persona` connect `Persona` to `PersonaKind`, `ChatViewModelTest`, `SessionRepository`, `SessionSummary`, `ChatViewModelTest.kt`, `NarrativeViewModelTest`, `Note`, `ChatViewModel.kt`, `Tally`, `ChatScreenTest.kt`, `ChatScreenTest`, `ChatViewModel`, `NewSessionViewModelTest`, `JournalViewModelTest`, `RoomStatsRepositoryTest`, `NarrativeDocument`, `InMemorySessionRepository`, `FakeSessionRepository`, `PersonaHolder`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **What connects `CrisisPattern`, `THERAPIST`, `COMPANION` to the rest of the system?**
  _104 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Block` be split into smaller, more focused modules?**
  _Cohesion score 0.05230678812812224 - nodes in this community are weakly interconnected._
- **Should `VoiceConversationTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05405405405405406 - nodes in this community are weakly interconnected._
- **Should `MessageAnalyzer` be split into smaller, more focused modules?**
  _Cohesion score 0.06384180790960452 - nodes in this community are weakly interconnected._