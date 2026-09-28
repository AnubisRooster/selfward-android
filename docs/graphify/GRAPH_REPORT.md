# Graph Report - selfward-android  (2026-09-28)

## Corpus Check
- Large corpus: 252 files · ~605,565 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder.

## Summary
- 2561 nodes · 6667 edges · 117 communities (95 shown, 22 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 123 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- NarrativeViewModel.kt
- OnboardingViewModel
- VoiceAction
- GraphHolder
- RoomStatsRepositoryTest
- ContinuousSpeechRecognizer
- DashboardTest
- ModelRankingTest
- GraphNode
- VoiceConversationTest
- CloudChatServiceTest
- LocalModel
- DeviceVoiceRankingTest
- SessionRepository
- PersonaKind
- CrisisResourcesTest
- HttpProviderCatalog.kt
- PinScreen.kt
- GraphExportTest
- ChatViewModel
- SettingsViewModelTest
- WordPieceTokenizer
- NarrativePdfWriter.kt
- MessageAnalyzerTest
- ModelSettings
- SelfwardDatabase
- ChatScreen.kt
- ActiveSessionHolder
- EmbeddingModelSpec
- DreamRepository
- assertequals
- JournalScreen.kt
- PriceTiersTest
- Provider
- NoteRepository
- SettingsViewModel
- DownloadStatus
- AndroidEmbeddingModelDownloader
- .viewModel()
- .generate()
- Test
- Theme.kt
- DownloadProgress
- Message
- Persona
- Inject
- GraphDao
- NarrativeTypesetTest
- NetworkModule.kt
- .paginate()
- ChatViewModel.kt
- CloudChatServiceTest.kt
- .pool()
- SettingsScreen.kt
- JournalViewModelTest
- NarrativePdfWriterTest.kt
- UnusableModels
- .detect()
- TherapyModality
- DataModule.kt
- DreamDao
- SessionDao
- MainScreen.kt
- NewSessionViewModelTest
- CloudChatService
- Role
- CloudTtsService
- GraphViewModel.kt
- NoteDao
- MessageDao
- GraphScreen.kt
- JournalViewModel
- ProviderDefaultsAdoptionTest
- PinLockoutTest
- HttpOpenRouterCatalogTest
- Test
- SelfwardDatabase.kt
- NarrativeViewModelTest.kt
- ModelChoice
- ChatProtocol
- ChatProtocolTest
- TherapyInsights
- TtsRequest
- OnboardingScreen.kt
- graphify_pipeline.py
- NarrativeEntity
- Test
- DashboardSection.kt
- ExportFiles
- LlamaCppLocalService
- SessionsViewModel
- .isPermanent()
- PinLockout
- LockoutStore
- RoomGraphRepositoryTest.kt
- SpiritualTradition
- SessionsScreen.kt
- .of()
- .extract()
- PinService
- .speak()
- .select()
- HttpOpenRouterCatalog
- SessionMappersTest.kt
- AnthropicProtocol
- .extract()
- .summarise()
- GGUFModelCatalogTest
- .emit()
- ManualSilenceClock
- gradlew
- Migrations.kt
- NarrativeFormat
- render.sh

## God Nodes (most connected - your core abstractions)
1. `Message` - 76 edges
2. `ChatViewModel` - 65 edges
3. `Provider` - 60 edges
4. `GraphNode` - 56 edges
5. `Persona` - 51 edges
6. `LocalModel` - 46 edges
7. `EmbeddingModelSpec` - 39 edges
8. `VoiceConversationTest` - 37 edges
9. `GraphEdge` - 36 edges
10. `SelfwardDatabase` - 35 edges

## Surprising Connections (you probably didn't know these)
- `NarrativePdfWriterTest` --calls--> `NarrativePdfWriter`  [INFERRED]
  app/src/androidTest/java/com/selfward/data/export/NarrativePdfWriterTest.kt → app/src/main/java/com/selfward/data/export/NarrativePdfWriter.kt
- `ExportFilesTest` --calls--> `ExportFiles`  [INFERRED]
  app/src/test/java/com/selfward/data/export/ExportFilesTest.kt → app/src/main/java/com/selfward/data/export/ExportFiles.kt
- `AppRoot()` --calls--> `MainScreen()`  [INFERRED]
  app/src/main/java/com/selfward/ui/AppRoot.kt → app/src/main/java/com/selfward/ui/MainScreen.kt
- `ChatViewModel` --calls--> `ChatUiState`  [INFERRED]
  app/src/main/java/com/selfward/ui/chat/ChatViewModel.kt → app/src/main/java/com/selfward/ui/chat/ChatUiState.kt
- `MainApplication` --references--> `ModelSettings`  [EXTRACTED]
  app/src/main/java/com/selfward/MainApplication.kt → app/src/main/java/com/selfward/core/ModelSettings.kt

## Import Cycles
- None detected.

## Communities (117 total, 22 thin omitted)

### Community 0 - "NarrativeViewModel.kt"
Cohesion: 0.05
Nodes (30): ChatService, Flow, LocalLLMService, NarrativeDocument, NarrativePrompt, NarrativeSource, NarrativeSources, NarrativeStore (+22 more)

### Community 1 - "OnboardingViewModel"
Cohesion: 0.06
Nodes (27): Intake, IntakeContext, IntakeStore, EncryptedIntakeStore, SharedPreferences, Singleton, AppRootViewModel, HiltViewModel (+19 more)

### Community 2 - "VoiceAction"
Cohesion: 0.06
Nodes (28): ArmSilence, CancelSilence, Ended, Failed, Final, Partial, RecognizerEnded, RecognizerFailed (+20 more)

### Community 3 - "GraphHolder"
Cohesion: 0.06
Nodes (21): EmbeddingProvider, FloatArray, EmbeddingProviderFactory, FloatArray, Singleton, MemoryVectorStore, ScoredId, GraphHolder (+13 more)

### Community 4 - "RoomStatsRepositoryTest"
Cohesion: 0.08
Nodes (19): ExportFilesTest, Config, RunWith, Test, After, Config, Context, RunWith (+11 more)

### Community 5 - "ContinuousSpeechRecognizer"
Cohesion: 0.06
Nodes (17): AndroidSttService, RecognitionListener, Bundle, ByteArray, Context, RecognitionListener, SpeechRecognizer, ContinuousSpeechRecognizer (+9 more)

### Community 6 - "DashboardTest"
Cohesion: 0.10
Nodes (7): Dashboard, MessageTally, StatsRepository, Tally, RoomStatsRepository, DashboardTest, Test

### Community 7 - "ModelRankingTest"
Cohesion: 0.14
Nodes (5): ModelRanking, OpenRouterModel, Test, ModelRankingTest, JvmOverloads

### Community 8 - "GraphNode"
Cohesion: 0.10
Nodes (8): GraphEdge, GraphNode, TherapyGraph, Test, TherapyGraphTest, Test, TherapyGraphUpsertTest, atomiclong

### Community 9 - "VoiceConversationTest"
Cohesion: 0.12
Nodes (3): FakeSpeechSource, Test, VoiceConversationTest

### Community 10 - "CloudChatServiceTest"
Cohesion: 0.12
Nodes (13): HiltViewModel, ViewModel, PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel (+5 more)

### Community 11 - "LocalModel"
Cohesion: 0.12
Nodes (5): GGUFModelCatalog, LocalModel, ModelDownloader, AndroidModelDownloader, FakeModelDownloader

### Community 12 - "DeviceVoiceRankingTest"
Cohesion: 0.11
Nodes (13): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD, LocalTtsService, AndroidTtsService (+5 more)

### Community 13 - "SessionRepository"
Cohesion: 0.08
Nodes (8): InMemorySessionRepository, Session, SessionRepository, SessionSummary, toDomain(), toEntity(), toSessionEntity(), RoomSessionRepository

### Community 14 - "PersonaKind"
Cohesion: 0.08
Nodes (22): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, CompanionPersonality, BOLD, CALM (+14 more)

### Community 15 - "CrisisResourcesTest"
Cohesion: 0.12
Nodes (7): CrisisResources, Resource, SafetyGuardrails, CrisisResourcesTest, Test, Test, SafetyGuardrailsTest

### Community 16 - "HttpProviderCatalog.kt"
Cohesion: 0.09
Nodes (29): Flow, HttpClient, HttpClient, HttpClient, HttpClient, After, bearerauth, body (+21 more)

### Community 17 - "PinScreen.kt"
Cohesion: 0.09
Nodes (30): animatefloatasstate, AppRoot(), Composable, Composable, NumberPad(), PinKey(), PinScreen(), background (+22 more)

### Community 18 - "GraphExportTest"
Cohesion: 0.23
Nodes (3): GraphExport, GraphExportTest, Test

### Community 19 - "ChatViewModel"
Cohesion: 0.11
Nodes (4): Result, ChatViewModel, Flow, LocalTtsService

### Community 20 - "SettingsViewModelTest"
Cohesion: 0.12
Nodes (10): FakeLocalTtsService, FakeOpenRouterCatalog, After, Before, com, LocalTtsService, OptIn, Test (+2 more)

### Community 21 - "WordPieceTokenizer"
Cohesion: 0.11
Nodes (8): LongArray, TokenizedInput, TokenizerConfig, WordPieceTokenizer, Before, Test, WordPieceTokenizerTest, normalizer

### Community 22 - "NarrativePdfWriter.kt"
Cohesion: 0.13
Nodes (17): Block, Style, BODY, HEADING, SUBTITLE, TITLE, Cursor, Canvas (+9 more)

### Community 23 - "MessageAnalyzerTest"
Cohesion: 0.15
Nodes (8): EdgeSpec, Extraction, Kind, MessageAnalyzer, NodeSpec, Relation, Test, MessageAnalyzerTest

### Community 24 - "ModelSettings"
Cohesion: 0.13
Nodes (8): LocalTtsService, ModelSettings, VoiceCatalog, LocalTtsService, Test, ModelSettingsTest, SpyingLocalTtsService, FakeSecureSettings

### Community 25 - "SelfwardDatabase"
Cohesion: 0.19
Nodes (7): SelfwardDatabase, DataModule, Context, HttpClient, LocalTtsService, Provides, Singleton

### Community 26 - "ChatScreen.kt"
Cohesion: 0.11
Nodes (28): activityresultcontracts, alertdialog, ChatScreen(), Composable, Modifier, MessageBubble(), ModalityPicker(), ModelBar() (+20 more)

### Community 27 - "ActiveSessionHolder"
Cohesion: 0.09
Nodes (10): ActiveSessionHolder, Singleton, FakeSessionRepository, FakeStatsRepository, After, Before, OptIn, Test (+2 more)

### Community 28 - "EmbeddingModelSpec"
Cohesion: 0.12
Nodes (5): EmbeddingModelCatalog, EmbeddingModelSpec, EmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 29 - "DreamRepository"
Cohesion: 0.09
Nodes (7): Dream, DreamRepository, DreamSymbols, joinToList(), RoomDreamRepository, FakeDreamRepository, FakeDreamRepository

### Community 30 - "assertequals"
Cohesion: 0.18
Nodes (9): assertequals, assertfalse, assertnotnull, assertnull, asserttrue, kind, relation, simpledateformat (+1 more)

### Community 31 - "JournalScreen.kt"
Cohesion: 0.15
Nodes (24): alignment, Composable, Modifier, prettifyEnumName(), SelectionChips(), DreamRow(), formatDate(), JournalScreen() (+16 more)

### Community 32 - "PriceTiersTest"
Cohesion: 0.19
Nodes (3): PriceTiers, Test, PriceTiersTest

### Community 33 - "Provider"
Cohesion: 0.15
Nodes (11): ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings, EncryptedSecureSettings, Singleton (+3 more)

### Community 34 - "NoteRepository"
Cohesion: 0.13
Nodes (11): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, splitList() (+3 more)

### Community 35 - "SettingsViewModel"
Cohesion: 0.12
Nodes (5): HiltViewModel, LocalTtsService, ViewModel, SettingsViewModel, isactive

### Community 36 - "DownloadStatus"
Cohesion: 0.11
Nodes (19): DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING, Context, DownloadManager (+11 more)

### Community 38 - ".viewModel()"
Cohesion: 0.19
Nodes (4): FakeLockoutStore, FakePinStore, Test, PinViewModelTest

### Community 40 - "Test"
Cohesion: 0.22
Nodes (3): ModalityRouter, Test, ModalityRouterTest

### Community 41 - "Theme.kt"
Cohesion: 0.11
Nodes (19): activity, AndroidEntryPoint, Bundle, MainActivity, Composable, SelfwardTheme(), ComponentActivity, darkcolorscheme (+11 more)

### Community 42 - "DownloadProgress"
Cohesion: 0.23
Nodes (5): DownloadProgress, Half, PairedDownload, Test, PairedDownloadTest

### Community 43 - "Message"
Cohesion: 0.25
Nodes (3): Message, AnthropicProtocolTest, Test

### Community 44 - "Persona"
Cohesion: 0.22
Nodes (6): Persona, TherapyPromptBuilder, Test, TherapyPromptBuilderTest, InMemorySessionRepositoryTest, Test

### Community 45 - "Inject"
Cohesion: 0.16
Nodes (14): Singleton, Singleton, PersonaHolder, MainApplication, DashboardUiState, DashboardViewModel, HiltViewModel, StateFlow (+6 more)

### Community 46 - "GraphDao"
Cohesion: 0.18
Nodes (10): GraphDao, Dao, Insert, Query, GraphEdgeEntity, Entity, PrimaryKey, GraphNodeEntity (+2 more)

### Community 47 - "NarrativeTypesetTest"
Cohesion: 0.20
Nodes (8): Canvas, Config, RunWith, Test, NarrativeTypesetTest, RecordingTarget, bitmap, robolectrictestrunner

### Community 48 - "NetworkModule.kt"
Cohesion: 0.20
Nodes (11): OpenRouterCatalog, ProviderCatalog, Context, HttpClient, InstallIn, Module, Provides, Singleton (+3 more)

### Community 49 - ".paginate()"
Cohesion: 0.26
Nodes (5): NarrativeExport, Test, NarrativeExportTest, date, DateFormat

### Community 50 - "ChatViewModel.kt"
Cohesion: 0.13
Nodes (13): SilenceClock, CoroutineSilenceClock, HiltViewModel, ViewModel, cancellationexception, CoroutineScope, emitall, firstornull (+5 more)

### Community 51 - "CloudChatServiceTest.kt"
Cohesion: 0.16
Nodes (17): Config, MockEngine, RunWith, MockEngine, bytechannel, bytereadchannel, completabledeferred, headersof (+9 more)

### Community 52 - ".pool()"
Cohesion: 0.18
Nodes (8): FloatArray, LongArray, MeanPooling, FloatArray, Test, MeanPoolingTest, assertarrayequals, assertthrows

### Community 53 - "SettingsScreen.kt"
Cohesion: 0.19
Nodes (18): AboutSection(), DeviceVoicePicker(), DownloadActionButton(), DownloadProgressAndError(), com, Composable, OpenRouterModelRow(), OpenRouterModelSection() (+10 more)

### Community 54 - "JournalViewModelTest"
Cohesion: 0.25
Nodes (5): JournalViewModelTest, After, Before, OptIn, Test

### Community 55 - "NarrativePdfWriterTest.kt"
Cohesion: 0.22
Nodes (8): androidjunit4, ByteArray, RunWith, Test, NarrativePdfWriterTest, bytearrayoutputstream, instrumentationregistry, style

### Community 56 - "UnusableModels"
Cohesion: 0.12
Nodes (3): UnusableModels, PrefsUnusableModels, context

### Community 58 - "TherapyModality"
Cohesion: 0.12
Nodes (14): TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL, PURPOSE (+6 more)

### Community 59 - "DataModule.kt"
Cohesion: 0.11
Nodes (9): SpeechSource, OnnxEmbeddingProvider, RoomGraphRepository, InstallIn, Module, migration_1_2, migration_2_3, migration_3_4 (+1 more)

### Community 60 - "DreamDao"
Cohesion: 0.20
Nodes (8): DreamDao, Dao, Insert, Query, DreamEntity, Entity, PrimaryKey, onconflictstrategy

### Community 61 - "SessionDao"
Cohesion: 0.20
Nodes (8): Dao, Insert, Query, SessionDao, Entity, PrimaryKey, SessionEntity, index

### Community 62 - "MainScreen.kt"
Cohesion: 0.12
Nodes (17): Composable, MainScreen(), Tab, editnote, fillmaxsize, forum, icon, icons (+9 more)

### Community 63 - "NewSessionViewModelTest"
Cohesion: 0.27
Nodes (4): After, OptIn, Test, NewSessionViewModelTest

### Community 64 - "CloudChatService"
Cohesion: 0.23
Nodes (5): ChatServiceException, Exception, Flow, MissingApiKeyException, CloudChatService

### Community 65 - "Role"
Cohesion: 0.16
Nodes (10): Role, ASSISTANT, SYSTEM, USER, After, Before, Config, RunWith (+2 more)

### Community 66 - "CloudTtsService"
Cohesion: 0.21
Nodes (7): Exception, TtsService, TtsServiceException, CloudTtsService, ByteArray, CloudTtsServiceTest, Test

### Community 67 - "GraphViewModel.kt"
Cohesion: 0.18
Nodes (11): ExportedFile, Uri, share(), GraphFormat, GRAPHML, JSON, GraphViewModel, InsightsUiState (+3 more)

### Community 68 - "NoteDao"
Cohesion: 0.21
Nodes (8): SessionCountRow, Dao, Insert, Query, NoteDao, Entity, PrimaryKey, NoteEntity

### Community 69 - "MessageDao"
Cohesion: 0.21
Nodes (9): Dao, Insert, Query, MessageCountRow, MessageDao, ModalityCountRow, Entity, PrimaryKey (+1 more)

### Community 70 - "GraphScreen.kt"
Cohesion: 0.21
Nodes (15): ExportOption, colorFor(), GraphCanvas(), GraphScreen(), kindLabel(), com, Composable, NodeRow() (+7 more)

### Community 71 - "JournalViewModel"
Cohesion: 0.20
Nodes (4): JournalUiState, JournalViewModel, HiltViewModel, ViewModel

### Community 73 - "PinLockoutTest"
Cohesion: 0.33
Nodes (4): Clock, FakeLockoutStore, Test, PinLockoutTest

### Community 75 - "Test"
Cohesion: 0.30
Nodes (3): ProviderDefaults, Test, ProviderDefaultsTest

### Community 76 - "SelfwardDatabase.kt"
Cohesion: 0.21
Nodes (9): InsightDao, Dao, Insert, Query, InsightEntity, Entity, PrimaryKey, Database (+1 more)

### Community 77 - "NarrativeViewModelTest.kt"
Cohesion: 0.31
Nodes (9): Config, RunWith, Before, dispatchers, experimentalcoroutinesapi, resetmain, runtest, setmain (+1 more)

### Community 79 - "ChatProtocol"
Cohesion: 0.35
Nodes (13): ApiError, ChatProtocol, ChatRequest, ErrorEnvelope, ErrorMetadata, Serializable, ReqMessage, StreamChoice (+5 more)

### Community 81 - "TherapyInsights"
Cohesion: 0.22
Nodes (3): CycleDetector, Cycles, TherapyInsights

### Community 82 - "TtsRequest"
Cohesion: 0.23
Nodes (6): Serializable, TtsRequest, ByteArray, Test, TtsRequestTest, SerialName

### Community 83 - "OnboardingScreen.kt"
Cohesion: 0.35
Nodes (13): AboutYouStep(), ApiKeyStep(), DisclaimerStep(), GoalsStep(), IntakeStep(), Composable, LocalModelStep(), OnboardingScreen() (+5 more)

### Community 84 - "graphify_pipeline.py"
Cohesion: 0.14
Nodes (12): graphify_analyze, graphify_build, graphify_cluster, graphify_detect, graphify_export, graphify_extract, graphify_llm, graphify_report (+4 more)

### Community 85 - "NarrativeEntity"
Cohesion: 0.26
Nodes (7): Dao, Insert, Query, NarrativeDao, Entity, PrimaryKey, NarrativeEntity

### Community 86 - "Test"
Cohesion: 0.32
Nodes (3): SseParser, Test, SseParserTest

### Community 87 - "DashboardSection.kt"
Cohesion: 0.38
Nodes (11): GlobalStats, Count(), Counts(), DashboardSection(), Composable, Modifier, OptIn, Modalities() (+3 more)

### Community 88 - "ExportFiles"
Cohesion: 0.32
Nodes (5): ExportFiles, Context, Uri, fileprovider, outputstream

### Community 89 - "LlamaCppLocalService"
Cohesion: 0.24
Nodes (5): Flow, LlamaCppLocalService, emptyflow, llamaconfig, LlamaModel

### Community 90 - "SessionsViewModel"
Cohesion: 0.26
Nodes (4): HiltViewModel, ViewModel, SessionsViewModel, viewmodelscope

### Community 92 - "PinLockout"
Cohesion: 0.27
Nodes (5): Incorrect, LockedOut, PinAttempt, PinLockout, Success

### Community 93 - "LockoutStore"
Cohesion: 0.27
Nodes (5): LockoutStore, EncryptedPinStore, SharedPreferences, Singleton, PrefsLockoutStore

### Community 94 - "RoomGraphRepositoryTest.kt"
Cohesion: 0.29
Nodes (6): After, Before, Config, RunWith, Test, RoomGraphRepositoryTest

### Community 95 - "SpiritualTradition"
Cohesion: 0.20
Nodes (10): SpiritualTradition, BUDDHIST, CHRISTIAN, HINDU, INTERFAITH, ISLAMIC, JEWISH, SECULAR (+2 more)

### Community 96 - "SessionsScreen.kt"
Cohesion: 0.56
Nodes (9): SessionStats, ActiveList(), ArchiveList(), formatTimestamp(), Composable, plural(), SessionBadges(), SessionRow() (+1 more)

### Community 97 - ".of()"
Cohesion: 0.31
Nodes (4): CosineSimilarity, FloatArray, CosineSimilarityTest, Test

### Community 101 - ".select()"
Cohesion: 0.36
Nodes (3): ModelSelector, Test, ModelSelectorTest

### Community 104 - "AnthropicProtocol"
Cohesion: 0.54
Nodes (7): AnthropicProtocol, ApiError, ChatRequest, Serializable, ReqMessage, StreamDelta, StreamEvent

### Community 105 - ".extract()"
Cohesion: 0.38
Nodes (3): InsightExtractor, InsightExtractorTest, Test

### Community 110 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 112 - "NarrativeFormat"
Cohesion: 0.67
Nodes (3): NarrativeFormat, MARKDOWN, PDF

## Knowledge Gaps
- **91 isolated node(s):** `CrisisPattern`, `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH` (+86 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 410 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ChatViewModel` connect `ChatViewModel` to `NarrativeViewModel.kt`, `OnboardingViewModel`, `VoiceAction`, `GraphHolder`, `LocalModel`, `SessionRepository`, `CrisisResourcesTest`, `ModelSettings`, `ChatScreen.kt`, `ActiveSessionHolder`, `Provider`, `Test`, `Persona`, `Inject`, `NetworkModule.kt`, `ChatViewModel.kt`, `UnusableModels`, `TherapyModality`, `DataModule.kt`, `CloudTtsService`, `ModelChoice`, `.speak()`, `.summarise()`?**
  _High betweenness centrality (0.094) - this node is a cross-community bridge._
- **Why does `GraphNode` connect `GraphNode` to `GraphHolder`, `GraphViewModel.kt`, `RoomStatsRepositoryTest`, `DashboardTest`, `.generate()`, `GraphScreen.kt`, `SessionMappersTest.kt`, `SessionRepository`, `HttpProviderCatalog.kt`, `TherapyInsights`, `GraphExportTest`, `RoomGraphRepositoryTest.kt`, `.detect()`, `TherapyModality`, `DataModule.kt`, `assertequals`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Why does `Message` connect `Message` to `NarrativeViewModel.kt`, `RoomStatsRepositoryTest`, `CloudChatServiceTest`, `SessionRepository`, `HttpProviderCatalog.kt`, `ChatViewModel`, `ChatScreen.kt`, `ActiveSessionHolder`, `Persona`, `ChatViewModel.kt`, `CloudChatServiceTest.kt`, `TherapyModality`, `CloudChatService`, `Role`, `NarrativeViewModelTest.kt`, `ChatProtocol`, `ChatProtocolTest`, `LlamaCppLocalService`, `SessionMappersTest.kt`, `AnthropicProtocol`, `.emit()`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `GraphNode` (e.g. with `.node()` and `.restoreAdvancesCounterPastRestoredIds()`) actually correct?**
  _`GraphNode` has 5 INFERRED edges - model-reasoned connections that need verification._
- **What connects `CrisisPattern`, `THERAPIST`, `COMPANION` to the rest of the system?**
  _91 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `NarrativeViewModel.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05009276437847866 - nodes in this community are weakly interconnected._
- **Should `OnboardingViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.05593607305936073 - nodes in this community are weakly interconnected._