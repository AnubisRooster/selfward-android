# Graph Report - selfward-android  (2026-09-21)

## Corpus Check
- Large corpus: 252 files · ~536,211 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder.

## Summary
- 2699 nodes · 6614 edges · 135 communities (90 shown, 45 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 171 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- VoiceAction
- SettingsViewModelTest
- PersonaKind
- ChatScreenTest
- MemoryVectorStore
- ActiveSessionHolder
- DashboardTest
- LocalModel
- Persona
- test
- CrisisResourcesTest
- NarrativeViewModelTest
- SettingsViewModel
- EmbeddingModelSpec
- OnboardingViewModel
- GraphEdge
- VoiceConversationTest
- CloudChatServiceTest
- ChatScreen.kt
- NarrativePdfWriter.kt
- GraphNode
- ModelRankingTest
- ChatScreenTest.kt
- ChatViewModel.kt
- HttpProviderCatalog.kt
- MessageAnalyzerTest
- ChatViewModel
- ContinuousSpeechRecognizer
- Intake
- MainScreen.kt
- PriceTiersTest
- Message
- DreamRepository
- DownloadStatus
- ChatProtocol
- FakeSecureSettings
- TtsRequest
- AndroidEmbeddingModelDownloader
- FakeUnusable
- NoteRepository
- ModalityRouterTest
- GraphExportTest
- .viewModel()
- Provider
- .generate()
- .buildVm()
- DataModule.kt
- GraphScreen.kt
- LocalLLMService
- RecognitionListener
- SettingsScreen.kt
- PairedDownloadTest
- CloudChatServiceTest.kt
- ModelChoice
- FakeChatService
- NetworkModule.kt
- UnusableModels
- SessionsScreen.kt
- .paginate()
- .detect()
- DataModule
- NarrativeViewModel.kt
- .isPermanent()
- WordPieceTokenizer
- ChatViewModelTest
- com
- NewSessionViewModelTest
- ChatViewModelTest.kt
- ExportFiles
- TherapyInsights
- TherapyModality
- GraphDao.kt
- SelfwardDatabase
- AndroidModelDownloader
- .buildHolder()
- PinLockoutTest
- .typesetting()
- RoomStatsRepositoryTest
- JournalViewModelTest
- PinScreen.kt
- MainScreen()
- ProviderDefaultsAdoptionTest
- ChatServiceException
- RoomSessionRepository
- JournalViewModel
- graphify_pipeline.py
- ProviderDefaultsTest
- Role
- CloudChatService
- .pool()
- PinLockout
- launch
- OnboardingScreen.kt
- HttpOpenRouterCatalogTest
- composable
- inject
- OpenRouterModel
- GraphHolder
- WordPieceTokenizerTest
- .cytoscapeJson()
- NarrativeDocument
- HttpOpenRouterCatalog
- GraphViewModel.kt
- DashboardSection.kt
- entity
- .onDone()
- FakeSessionRepository
- SseParserTest
- GraphDao
- MessageDao
- SessionDao
- DreamDao
- NoteDao
- .emit()
- FakeLocalTtsService
- NarrativePdfWriterTest
- EmbeddingProviderFactory
- .extract()
- GGUFModelCatalog
- .select()
- TtsService
- ExportFilesTest
- .extract()
- DashboardViewModel.kt
- MainApplication.kt
- FakeSpeechSource
- ManualSilenceClock
- gradlew
- locale
- Migrations.kt
- .produceReply()
- render.sh

## God Nodes (most connected - your core abstractions)
1. `Message` - 99 edges
2. `LocalModel` - 79 edges
3. `ChatViewModelTest` - 74 edges
4. `Provider` - 60 edges
5. `GraphNode` - 57 edges
6. `Persona` - 52 edges
7. `EmbeddingModelSpec` - 48 edges
8. `ChatViewModel` - 48 edges
9. `GraphEdge` - 38 edges
10. `DownloadStatus` - 38 edges

## Surprising Connections (you probably didn't know these)
- `ChatViewModel` --calls--> `ChatUiState`  [INFERRED]
  app/src/main/java/com/selfward/ui/chat/ChatViewModel.kt → app/src/main/java/com/selfward/ui/chat/ChatUiState.kt
- `NarrativePdfWriterTest` --calls--> `NarrativePdfWriter`  [INFERRED]
  app/src/androidTest/java/com/selfward/data/export/NarrativePdfWriterTest.kt → app/src/main/java/com/selfward/data/export/NarrativePdfWriter.kt
- `ExportFilesTest` --calls--> `ExportFiles`  [INFERRED]
  app/src/test/java/com/selfward/data/export/ExportFilesTest.kt → app/src/main/java/com/selfward/data/export/ExportFiles.kt
- `MainApplication` --references--> `ModelSettings`  [EXTRACTED]
  app/src/main/java/com/selfward/MainApplication.kt → app/src/main/java/com/selfward/core/ModelSettings.kt
- `NewSessionViewModelTest` --calls--> `ActiveSessionHolder`  [EXTRACTED]
  app/src/test/java/com/selfward/ui/newsession/NewSessionViewModelTest.kt → app/src/main/java/com/selfward/core/ActiveSessionHolder.kt

## Import Cycles
- None detected.

## Communities (135 total, 45 thin omitted)

### Community 0 - "VoiceAction"
Cohesion: 0.05
Nodes (28): ArmSilence, CancelSilence, Ended, Failed, Final, Partial, RecognizerEnded, RecognizerFailed (+20 more)

### Community 1 - "SettingsViewModelTest"
Cohesion: 0.06
Nodes (16): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD, AndroidTtsService, LocalTtsService (+8 more)

### Community 2 - "PersonaKind"
Cohesion: 0.05
Nodes (33): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, CompanionPersonality, BOLD, CALM (+25 more)

### Community 3 - "ChatScreenTest"
Cohesion: 0.06
Nodes (13): ChatScreen(), ChatScreenTest, FakeCatalog, FakeChatService, FakeEmbeddingModelDownloader, FakeLocalLLMService, FakeLocalTtsService, FakeSessionRepository (+5 more)

### Community 4 - "MemoryVectorStore"
Cohesion: 0.06
Nodes (14): CosineSimilarity, FloatArray, FloatArray, MemoryVectorStore, ScoredId, NarrativePrompt, NarrativeSource, NarrativeSources (+6 more)

### Community 5 - "ActiveSessionHolder"
Cohesion: 0.09
Nodes (9): ActiveSessionHolder, SessionsScreen(), SessionsViewModel, FakeSessionRepository, FakeStatsRepository, SessionsScreenTest, FakeSessionRepository, FakeStatsRepository (+1 more)

### Community 6 - "DashboardTest"
Cohesion: 0.08
Nodes (7): Dashboard, MessageTally, StatsRepository, Tally, RoomStatsRepository, DashboardTest, FailingStatsRepository

### Community 7 - "LocalModel"
Cohesion: 0.07
Nodes (7): LocalModel, ModelDownloader, FakeModelDownloader, DownloadedModelDownloader, FakeModelDownloader, FakeModelDownloader, FakeModelDownloader

### Community 8 - "Persona"
Cohesion: 0.07
Nodes (10): Persona, TherapyPromptBuilder, InMemorySessionRepository, Session, SessionRepository, toDomain(), toEntity(), toSessionEntity() (+2 more)

### Community 9 - "test"
Cohesion: 0.13
Nodes (12): androidjunit4, assertequals, assertfalse, assertnull, asserttrue, bytearrayoutputstream, instrumentationregistry, kind (+4 more)

### Community 10 - "CrisisResourcesTest"
Cohesion: 0.07
Nodes (11): CrisisResources, Resource, CrisisLevel, CRITICAL, WARNING, SafetyGuardrails, ChatUiState, DisclaimerStep() (+3 more)

### Community 11 - "NarrativeViewModelTest"
Cohesion: 0.11
Nodes (10): FailingChatService, FakeDreamRepository, FakeLocalTtsService, FakeNarrativeStore, FakeNoteRepository, ChatService, LocalTtsService, NarrativeViewModelTest (+2 more)

### Community 12 - "SettingsViewModel"
Cohesion: 0.09
Nodes (8): DeviceVoicePicker(), com, SettingsScreen(), SettingsViewModel, FakeLocalTtsService, FakeOpenRouterCatalog, LocalTtsService, SettingsScreenTest

### Community 13 - "EmbeddingModelSpec"
Cohesion: 0.10
Nodes (6): EmbeddingModelCatalog, EmbeddingModelSpec, EmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 14 - "OnboardingViewModel"
Cohesion: 0.09
Nodes (11): OnboardingStep, ABOUT_YOU, API_KEY, CONCERNS, DISCLAIMER, GOALS, HISTORY, LOCAL_MODEL (+3 more)

### Community 15 - "GraphEdge"
Cohesion: 0.07
Nodes (9): GraphEdge, GraphRepository, GraphSnapshot, RoomGraphRepository, RoomGraphRepositoryTest, FakeGraphRepository, FakeGraphRepository, mutex (+1 more)

### Community 17 - "CloudChatServiceTest"
Cohesion: 0.11
Nodes (10): ViewModel, PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel, CloudChatServiceTest (+2 more)

### Community 18 - "ChatScreen.kt"
Cohesion: 0.11
Nodes (31): activityresultcontracts, alertdialog, Modifier, MessageBubble(), ModalityPicker(), Modifier, prettifyEnumName(), SelectionChips() (+23 more)

### Community 19 - "NarrativePdfWriter.kt"
Cohesion: 0.12
Nodes (18): Block, Style, BODY, HEADING, SUBTITLE, TITLE, Cursor, Canvas (+10 more)

### Community 20 - "GraphNode"
Cohesion: 0.11
Nodes (5): GraphNode, TherapyGraph, TherapyGraphTest, TherapyGraphUpsertTest, atomiclong

### Community 22 - "ChatScreenTest.kt"
Cohesion: 0.15
Nodes (21): applicationprovider, assertcountequals, assertisdisplayed, bitmap, composetestrule, config, createcomposerule, hastext (+13 more)

### Community 23 - "ChatViewModel.kt"
Cohesion: 0.11
Nodes (16): ProviderDefaults, PersonaHolder, LocalTtsService, VoiceCatalog, JournalUiState, ViewModel, ViewModel, asstateflow (+8 more)

### Community 24 - "HttpProviderCatalog.kt"
Cohesion: 0.12
Nodes (24): Flow, bearerauth, body, bodyaschannel, bodyastext, bytearrayinputstream, contenttype, Document (+16 more)

### Community 25 - "MessageAnalyzerTest"
Cohesion: 0.13
Nodes (7): EdgeSpec, Extraction, Kind, MessageAnalyzer, NodeSpec, Relation, MessageAnalyzerTest

### Community 26 - "ChatViewModel"
Cohesion: 0.12
Nodes (3): Result, ChatViewModel, ViewModel

### Community 27 - "ContinuousSpeechRecognizer"
Cohesion: 0.11
Nodes (8): SpeechSource, ContinuousSpeechRecognizer, RecognitionListener, Bundle, ByteArray, RecognitionListener, SpeechRecognizer, Intent

### Community 28 - "Intake"
Cohesion: 0.12
Nodes (8): Intake, IntakeContext, IntakeStore, EncryptedIntakeStore, SharedPreferences, IntakeContextTest, FakeIntakeStore, FakeIntakeStore

### Community 29 - "MainScreen.kt"
Cohesion: 0.10
Nodes (25): Tab, box, dropdownmenu, dropdownmenuitem, editnote, fillmaxsize, forum, getvalue (+17 more)

### Community 31 - "Message"
Cohesion: 0.14
Nodes (8): AnthropicProtocol, ApiError, ChatRequest, ReqMessage, StreamDelta, StreamEvent, Message, AnthropicProtocolTest

### Community 32 - "DreamRepository"
Cohesion: 0.12
Nodes (9): Dream, DreamRepository, DreamSymbols, joinToList(), RoomDreamRepository, splitList(), toDomain(), FakeDreamRepository (+1 more)

### Community 33 - "DownloadStatus"
Cohesion: 0.11
Nodes (19): DownloadProgress, DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING, DownloadManager (+11 more)

### Community 34 - "ChatProtocol"
Cohesion: 0.11
Nodes (13): ApiError, ChatProtocol, ChatRequest, ErrorEnvelope, ErrorMetadata, ReqMessage, StreamChoice, StreamChunk (+5 more)

### Community 35 - "FakeSecureSettings"
Cohesion: 0.16
Nodes (5): ModelSettings, LocalTtsService, ModelSettingsTest, SpyingLocalTtsService, FakeSecureSettings

### Community 36 - "TtsRequest"
Cohesion: 0.14
Nodes (7): TtsRequest, CloudTtsService, ByteArray, TtsRequestTest, CloudTtsServiceTest, ByteArray, ByteArray

### Community 38 - "FakeUnusable"
Cohesion: 0.11
Nodes (8): ChatService, ChatService, ChatService, ChatService, FakeCatalog, FakeProviderCatalog, FakeUnusable, RefusesFirstModel

### Community 39 - "NoteRepository"
Cohesion: 0.12
Nodes (8): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, FakeNoteRepository

### Community 42 - ".viewModel()"
Cohesion: 0.15
Nodes (3): FakeLockoutStore, FakePinStore, PinViewModelTest

### Community 43 - "Provider"
Cohesion: 0.19
Nodes (7): ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings, EncryptedSecureSettings

### Community 45 - ".buildVm()"
Cohesion: 0.13
Nodes (6): ChunkedChatService, FailingChatService, FakeTtsService, ChatService, MissingKeyChatService, ObservingChatService

### Community 46 - "DataModule.kt"
Cohesion: 0.11
Nodes (9): PinService, PinStore, EncryptedPinStore, SharedPreferences, PrefsLockoutStore, migration_1_2, migration_2_3, migration_3_4 (+1 more)

### Community 47 - "GraphScreen.kt"
Cohesion: 0.13
Nodes (19): ExportOption, colorFor(), GraphCanvas(), GraphScreen(), kindLabel(), com, NodeRow(), Section() (+11 more)

### Community 48 - "LocalLLMService"
Cohesion: 0.14
Nodes (7): Flow, LocalLLMService, Flow, LlamaCppLocalService, emptyflow, llamaconfig, LlamaModel

### Community 49 - "RecognitionListener"
Cohesion: 0.13
Nodes (7): AndroidSttService, RecognitionListener, Bundle, ByteArray, RecognitionListener, SpeechRecognizer, recognizerintent

### Community 50 - "SettingsScreen.kt"
Cohesion: 0.14
Nodes (18): DreamRow(), formatDate(), JournalScreen(), NoteRow(), providerLabel(), clickable, items, lazycolumn (+10 more)

### Community 51 - "PairedDownloadTest"
Cohesion: 0.21
Nodes (3): Half, PairedDownload, PairedDownloadTest

### Community 52 - "CloudChatServiceTest.kt"
Cohesion: 0.16
Nodes (15): MockEngine, assertarrayequals, assertthrows, bytechannel, bytereadchannel, completabledeferred, headersof, httpheaders (+7 more)

### Community 53 - "ModelChoice"
Cohesion: 0.20
Nodes (6): ModelChoice, ProviderCatalog, HttpProviderCatalog, ModelBar(), ModelRow(), FakeProviderCatalog

### Community 54 - "FakeChatService"
Cohesion: 0.17
Nodes (3): FakeChatService, FakeIntakeStore, ProgrammableLocalLLMService

### Community 55 - "NetworkModule.kt"
Cohesion: 0.18
Nodes (9): OpenRouterCatalog, Context, HttpClient, NetworkModule, contentnegotiation, installin, module, provides (+1 more)

### Community 57 - "SessionsScreen.kt"
Cohesion: 0.23
Nodes (10): SessionStats, SessionSummary, ActiveList(), ArchiveList(), formatTimestamp(), plural(), SessionBadges(), SessionRow() (+2 more)

### Community 58 - ".paginate()"
Cohesion: 0.28
Nodes (3): NarrativeExport, NarrativeExportTest, DateFormat

### Community 60 - "DataModule"
Cohesion: 0.19
Nodes (5): DataModule, ChatService, Context, HttpClient, LocalTtsService

### Community 61 - "NarrativeViewModel.kt"
Cohesion: 0.18
Nodes (8): StateFlow, ViewModel, NarrativeFormat, MARKDOWN, PDF, NarrativeUiState, NarrativeViewModel, cancellationexception

### Community 63 - "WordPieceTokenizer"
Cohesion: 0.23
Nodes (3): TokenizedInput, WordPieceTokenizer, normalizer

### Community 65 - "com"
Cohesion: 0.19
Nodes (3): FakeEmbeddingModelDownloader, FakeLocalLLMService, com

### Community 67 - "ChatViewModelTest.kt"
Cohesion: 0.47
Nodes (9): after, assertnotnull, before, dispatchers, experimentalcoroutinesapi, resetmain, runtest, setmain (+1 more)

### Community 68 - "ExportFiles"
Cohesion: 0.24
Nodes (4): ExportFiles, fileprovider, outputstream, Uri

### Community 69 - "TherapyInsights"
Cohesion: 0.20
Nodes (3): CycleDetector, Cycles, TherapyInsights

### Community 70 - "TherapyModality"
Cohesion: 0.15
Nodes (10): TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL, PURPOSE (+2 more)

### Community 71 - "GraphDao.kt"
Cohesion: 0.32
Nodes (5): SessionCountRow, dao, insert, onconflictstrategy, query

### Community 72 - "SelfwardDatabase"
Cohesion: 0.20
Nodes (7): InsightDao, NarrativeDao, InsightEntity, NarrativeEntity, SelfwardDatabase, database, RoomDatabase

### Community 74 - ".buildHolder()"
Cohesion: 0.22
Nodes (4): FakeEmbeddingProvider, GraphHolderTest, FloatArray, RecordingGraphRepository

### Community 75 - "PinLockoutTest"
Cohesion: 0.29
Nodes (3): Clock, FakeLockoutStore, PinLockoutTest

### Community 76 - ".typesetting()"
Cohesion: 0.23
Nodes (3): Canvas, NarrativeTypesetTest, RecordingTarget

### Community 79 - "PinScreen.kt"
Cohesion: 0.16
Nodes (13): alignment, animatefloatasstate, NumberPad(), PinKey(), PinScreen(), background, backspace, circleshape (+5 more)

### Community 80 - "MainScreen()"
Cohesion: 0.20
Nodes (11): androidentrypoint, Bundle, MainActivity, AppRoot(), AppRootViewModel, ViewModel, MainScreen(), SelfwardTheme() (+3 more)

### Community 82 - "ChatServiceException"
Cohesion: 0.21
Nodes (6): ChatService, ChatServiceException, Exception, Flow, MissingApiKeyException, Flow

### Community 85 - "graphify_pipeline.py"
Cohesion: 0.14
Nodes (12): graphify_analyze, graphify_build, graphify_cluster, graphify_detect, graphify_export, graphify_extract, graphify_llm, graphify_report (+4 more)

### Community 87 - "Role"
Cohesion: 0.22
Nodes (7): Role, ASSISTANT, SYSTEM, USER, json, serializable, serialname

### Community 89 - ".pool()"
Cohesion: 0.23
Nodes (4): FloatArray, MeanPooling, MeanPoolingTest, LongArray

### Community 90 - "PinLockout"
Cohesion: 0.24
Nodes (6): Incorrect, LockedOut, LockoutStore, PinAttempt, PinLockout, Success

### Community 91 - "launch"
Cohesion: 0.21
Nodes (6): SilenceClock, CoroutineSilenceClock, coroutinescope, Job, launch, supervisorjob

### Community 92 - "OnboardingScreen.kt"
Cohesion: 0.27
Nodes (12): AboutYouStep(), ApiKeyStep(), GoalsStep(), IntakeStep(), LocalModelStep(), OnboardingScreen(), OnDeviceOnlyNote(), WelcomeStep() (+4 more)

### Community 94 - "composable"
Cohesion: 0.17
Nodes (11): activity, composable, darkcolorscheme, issystemindarktheme, lightcolorscheme, localview, materialtheme, roundedcornershape (+3 more)

### Community 95 - "inject"
Cohesion: 0.35
Nodes (7): applicationcontext, concurrenthashmap, context, encryptedsharedpreferences, inject, masterkey, singleton

### Community 96 - "OpenRouterModel"
Cohesion: 0.30
Nodes (4): ModelRanking, OpenRouterModel, OpenRouterModelRow(), OpenRouterModelSection()

### Community 97 - "GraphHolder"
Cohesion: 0.33
Nodes (3): EmbeddingProvider, FloatArray, GraphHolder

### Community 99 - ".cytoscapeJson()"
Cohesion: 0.29
Nodes (4): GraphExport, GraphFormat, GRAPHML, JSON

### Community 100 - "NarrativeDocument"
Cohesion: 0.26
Nodes (3): NarrativeDocument, NarrativeStore, RoomNarrativeStore

### Community 102 - "GraphViewModel.kt"
Cohesion: 0.27
Nodes (6): ExportedFile, share(), GraphViewModel, InsightsUiState, StateFlow, ViewModel

### Community 103 - "DashboardSection.kt"
Cohesion: 0.35
Nodes (10): GlobalStats, Count(), Counts(), DashboardSection(), Modifier, Modalities(), plural(), Themes() (+2 more)

### Community 104 - "entity"
Cohesion: 0.42
Nodes (3): entity, index, primarykey

### Community 108 - "GraphDao"
Cohesion: 0.29
Nodes (3): GraphDao, GraphEdgeEntity, GraphNodeEntity

### Community 109 - "MessageDao"
Cohesion: 0.22
Nodes (4): MessageCountRow, MessageDao, ModalityCountRow, MessageEntity

### Community 114 - "FakeLocalTtsService"
Cohesion: 0.25
Nodes (3): LocalTtsService, FakeLocalTtsService, LocalTtsService

### Community 116 - "EmbeddingProviderFactory"
Cohesion: 0.25
Nodes (3): EmbeddingProviderFactory, FloatArray, OnnxEmbeddingProvider

### Community 120 - "TtsService"
Cohesion: 0.29
Nodes (4): ByteArray, Exception, TtsService, TtsServiceException

### Community 123 - "DashboardViewModel.kt"
Cohesion: 0.60
Nodes (4): DashboardUiState, DashboardViewModel, StateFlow, ViewModel

### Community 124 - "MainApplication.kt"
Cohesion: 0.50
Nodes (3): MainApplication, Application, hiltandroidapp

### Community 127 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **104 isolated node(s):** `CrisisPattern`, `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH` (+99 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 522 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **45 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Message` connect `Message` to `PersonaKind`, `.produceReply()`, `ChatScreenTest`, `ActiveSessionHolder`, `Persona`, `CrisisResourcesTest`, `NarrativeViewModelTest`, `CloudChatServiceTest`, `ChatScreen.kt`, `ChatScreenTest.kt`, `ChatViewModel.kt`, `HttpProviderCatalog.kt`, `ChatProtocol`, `LocalLLMService`, `CloudChatServiceTest.kt`, `FakeChatService`, `NarrativeViewModel.kt`, `ChatViewModelTest.kt`, `TherapyModality`, `RoomStatsRepositoryTest`, `ChatServiceException`, `RoomSessionRepository`, `Role`, `CloudChatService`, `FakeSessionRepository`, `.emit()`?**
  _High betweenness centrality (0.101) - this node is a cross-community bridge._
- **Why does `GraphNode` connect `GraphNode` to `locale`, `PersonaKind`, `DashboardTest`, `Persona`, `test`, `CrisisResourcesTest`, `GraphEdge`, `ChatScreenTest.kt`, `HttpProviderCatalog.kt`, `GraphExportTest`, `.generate()`, `GraphScreen.kt`, `SessionsScreen.kt`, `.detect()`, `ChatViewModelTest.kt`, `TherapyInsights`, `.buildHolder()`, `RoomStatsRepositoryTest`, `GraphHolder`, `.cytoscapeJson()`, `GraphViewModel.kt`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **Why does `Provider` connect `Provider` to `OpenRouterModel`, `FakeSecureSettings`, `ChatViewModelTest.kt`, `test`, `OnboardingViewModel`, `ProviderDefaultsAdoptionTest`, `SettingsScreen.kt`, `CloudChatServiceTest`, `ModelChoice`, `ProviderDefaultsTest`, `ChatViewModel.kt`, `CloudChatService`, `HttpProviderCatalog.kt`, `ChatViewModel`, `ChatScreenTest.kt`, `OnboardingScreen.kt`, `PriceTiersTest`, `inject`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **What connects `CrisisPattern`, `THERAPIST`, `COMPANION` to the rest of the system?**
  _104 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `VoiceAction` be split into smaller, more focused modules?**
  _Cohesion score 0.05311871227364185 - nodes in this community are weakly interconnected._
- **Should `SettingsViewModelTest` be split into smaller, more focused modules?**
  _Cohesion score 0.055811571940604196 - nodes in this community are weakly interconnected._
- **Should `PersonaKind` be split into smaller, more focused modules?**
  _Cohesion score 0.0512987012987013 - nodes in this community are weakly interconnected._