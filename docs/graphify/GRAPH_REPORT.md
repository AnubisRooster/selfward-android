# Graph Report - selfward-android  (2026-11-23)

## Corpus Check
- 241 files · ~372,061 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 19 file(s) not represented in the graph (top: .xml 12, .properties 2, (none) 1)

## Summary
- 2916 nodes · 8448 edges · 147 communities (62 shown, 85 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 509 edges (avg confidence: 0.72)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- ChatViewModelTest
- OnboardingViewModel
- VoiceConversationTest
- LocalModel
- ModelRankingTest
- .generate()
- EmbeddingModelSpec
- JournalViewModel
- PersonaKind
- DataModule.kt
- NarrativeViewModelTest.kt
- ActiveSessionHolder
- SessionSummary
- PinLockout
- SettingsScreen()
- Tally
- NarrativePdfWriter.kt
- LocalLLMService
- MainScreen.kt
- WordPieceTokenizer
- ChatViewModel.kt
- MessageAnalyzerTest
- UnusableModels
- GraphExportTest
- Message
- Provider
- SessionRepository
- DeviceVoiceRankingTest
- Test
- SettingsViewModelTest
- NarrativeViewModel.kt
- PriceTiersTest
- DreamRepository
- Test
- DownloadProgress
- SettingsScreen.kt
- Theme.kt
- DashboardTest
- CloudChatServiceTest
- ChatViewModel
- Test
- TherapyModality
- RecognitionListener
- RoomStatsRepositoryTest
- ChatViewModelTest.kt
- NewSessionViewModelTest
- RoomSessionRepositoryTest.kt
- ChatScreen.kt
- TherapyGraph
- GraphSnapshot
- CrisisResourcesTest
- .onDone()
- HttpProviderCatalog.kt
- GraphNode
- PairedDownloadTest
- GraphDao
- .viewModel()
- GraphHolder
- Block
- ModelSettings
- .doSend()
- TtsRequest
- GraphScreen.kt
- JournalViewModelTest
- DownloadStatus
- ContinuousSpeechRecognizer
- CloudTtsService
- ChatScreenTest
- .isPermanent()
- .pool()
- ExportFilesTest
- DreamDao
- SessionEntity
- MigrationTest
- NarrativePdfWriterTest.kt
- ExportFiles
- NetworkModule.kt
- ProviderDefaultsAdoptionTest
- MemoryVectorStore
- .paginate()
- PinLockoutTest
- Persona
- SettingsViewModel
- NoteEntity
- MessageEntity
- NewSessionScreen.kt
- HttpOpenRouterCatalogTest
- PinScreen.kt
- ModelChoice
- Test
- ChatProtocolTest
- GraphEdge
- .buildHolder()
- SelfwardDatabase.kt
- AndroidModelDownloader
- RecognitionListener
- ChatProtocol
- SilenceClock
- PinViewModel.kt
- OnboardingScreen.kt
- Role
- CloudChatService
- DashboardSection.kt
- NarrativeEntity
- NarrativeScreen.kt
- Test
- InMemorySessionRepository
- .of()
- .extract()
- com
- .select()
- GraphViewModel.kt
- SessionMappersTest.kt
- ExportedFile
- AndroidTtsService
- .extract()
- GGUFModelCatalogTest.kt
- render.sh

## God Nodes (most connected - your core abstractions)
1. `Message` - 105 edges
2. `LocalModel` - 79 edges
3. `ChatViewModelTest` - 76 edges
4. `ChatViewModel` - 70 edges
5. `Provider` - 62 edges
6. `GraphNode` - 62 edges
7. `FakeSecureSettings` - 62 edges
8. `Persona` - 61 edges
9. `EmbeddingModelSpec` - 48 edges
10. `ModelSettings` - 47 edges

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

## Communities (147 total, 85 thin omitted)

### Community 0 - "ChatViewModelTest"
Cohesion: 0.06
Nodes (28): ChatService, ChatServiceException, FakeSpeechSource, ManualSilenceClock, FakeSecureSettings, FakeChatService, ChatViewModelTest, ChatService (+20 more)

### Community 1 - "OnboardingViewModel"
Cohesion: 0.05
Nodes (19): Intake, IntakeContext, IntakeStore, EncryptedIntakeStore, AppRootViewModel, OnboardingStep, ABOUT_YOU, API_KEY (+11 more)

### Community 2 - "VoiceConversationTest"
Cohesion: 0.07
Nodes (21): VoiceAction, ArmSilence, CancelSilence, Ended, Failed, Send, Speak, StartListening (+13 more)

### Community 3 - "LocalModel"
Cohesion: 0.07
Nodes (4): GGUFModelCatalog, LocalModel, FakeModelDownloader, FakeModelDownloader

### Community 4 - "ModelRankingTest"
Cohesion: 0.10
Nodes (4): ModelRanking, OpenRouterModel, HttpOpenRouterCatalog, ModelRankingTest

### Community 5 - ".generate()"
Cohesion: 0.11
Nodes (5): CycleDetector, Cycles, TherapyInsights, CycleDetectorTest, TherapyInsightsTest

### Community 6 - "EmbeddingModelSpec"
Cohesion: 0.09
Nodes (5): EmbeddingModelCatalog, EmbeddingModelSpec, AndroidEmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 7 - "JournalViewModel"
Cohesion: 0.09
Nodes (12): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, splitList() (+4 more)

### Community 8 - "PersonaKind"
Cohesion: 0.06
Nodes (30): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, CompanionPersonality, BOLD, CALM (+22 more)

### Community 9 - "DataModule.kt"
Cohesion: 0.12
Nodes (7): GraphRepository, TtsService, OnnxEmbeddingProvider, SelfwardDatabase, RoomGraphRepository, RoomDreamRepository, DataModule

### Community 10 - "NarrativeViewModelTest.kt"
Cohesion: 0.15
Nodes (7): MissingApiKeyException, NarrativeViewModelTest, FailingChatService, FakeLocalTtsService, FakeNarrativeStore, FakeNoteRepository, RecordingChatService

### Community 11 - "ActiveSessionHolder"
Cohesion: 0.18
Nodes (7): ActiveSessionHolder, SessionsScreen(), SessionsViewModel, SessionsScreenTest, FakeSessionRepository, FakeStatsRepository, SessionsViewModelTest

### Community 12 - "SessionSummary"
Cohesion: 0.11
Nodes (12): Dashboard, SessionStats, SessionSummary, DashboardUiState, DashboardViewModel, ActiveList(), ArchiveList(), formatTimestamp() (+4 more)

### Community 13 - "PinLockout"
Cohesion: 0.10
Nodes (10): LockoutStore, PinAttempt, Incorrect, LockedOut, Success, PinLockout, PinService, PinStore (+2 more)

### Community 14 - "SettingsScreen()"
Cohesion: 0.11
Nodes (12): AboutSection(), DeviceVoicePicker(), DownloadActionButton(), DownloadProgressAndError(), OpenRouterModelRow(), OpenRouterModelSection(), providerLabel(), SettingsScreen() (+4 more)

### Community 15 - "Tally"
Cohesion: 0.12
Nodes (6): MessageTally, StatsRepository, Tally, RoomStatsRepository, FailingStatsRepository, FakeStatsRepository

### Community 16 - "NarrativePdfWriter.kt"
Cohesion: 0.12
Nodes (10): Style, BODY, HEADING, SUBTITLE, TITLE, Cursor, PdfPageTarget, PageTarget (+2 more)

### Community 17 - "LocalLLMService"
Cohesion: 0.09
Nodes (5): LocalLLMService, LlamaCppLocalService, FakeLocalLLMService, FakeLocalLLMService, RecordingLocalLLM

### Community 19 - "WordPieceTokenizer"
Cohesion: 0.12
Nodes (4): TokenizedInput, TokenizerConfig, WordPieceTokenizer, WordPieceTokenizerTest

### Community 20 - "ChatViewModel.kt"
Cohesion: 0.09
Nodes (5): InsightExtractor, PersonaHolder, LocalTtsService, VoiceCatalog, MainApplication

### Community 21 - "MessageAnalyzerTest"
Cohesion: 0.15
Nodes (7): MessageAnalyzer, EdgeSpec, Extraction, Kind, NodeSpec, Relation, MessageAnalyzerTest

### Community 22 - "UnusableModels"
Cohesion: 0.10
Nodes (3): UnusableModels, PrefsUnusableModels, FakeUnusable

### Community 25 - "Provider"
Cohesion: 0.16
Nodes (7): ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings, EncryptedSecureSettings

### Community 26 - "SessionRepository"
Cohesion: 0.11
Nodes (3): Session, SessionRepository, FakeSessionRepository

### Community 27 - "DeviceVoiceRankingTest"
Cohesion: 0.16
Nodes (7): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD, DeviceVoiceRankingTest

### Community 29 - "SettingsViewModelTest"
Cohesion: 0.16
Nodes (5): SettingsViewModelTest, FakeEmbeddingModelDownloader, FakeLocalTtsService, FakeModelDownloader, FakeOpenRouterCatalog

### Community 30 - "NarrativeViewModel.kt"
Cohesion: 0.15
Nodes (6): NarrativeDocument, NarrativeStore, NarrativePdfWriter, RoomNarrativeStore, NarrativeUiState, NarrativeViewModel

### Community 33 - "DreamRepository"
Cohesion: 0.14
Nodes (5): Dream, DreamRepository, DreamSymbols, FakeDreamRepository, FakeDreamRepository

### Community 34 - "Test"
Cohesion: 0.19
Nodes (5): NarrativePrompt, NarrativeSource, NarrativeSources, NarrativePromptTest, NarrativeSourcesTest

### Community 36 - "SettingsScreen.kt"
Cohesion: 0.15
Nodes (4): DreamRow(), formatDate(), JournalScreen(), NoteRow()

### Community 37 - "Theme.kt"
Cohesion: 0.11
Nodes (3): MainActivity, AppRoot(), SelfwardTheme()

### Community 40 - "ChatViewModel"
Cohesion: 0.18
Nodes (3): Result, ChatScreen(), ChatViewModel

### Community 42 - "TherapyModality"
Cohesion: 0.11
Nodes (14): TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL, PURPOSE (+6 more)

### Community 48 - "ChatScreen.kt"
Cohesion: 0.13
Nodes (10): VoicePhase, IDLE, LISTENING, SPEAKING, THINKING, MessageBubble(), ModalityPicker(), ModelBar() (+2 more)

### Community 51 - "CrisisResourcesTest"
Cohesion: 0.21
Nodes (3): CrisisResources, Resource, CrisisResourcesTest

### Community 52 - ".onDone()"
Cohesion: 0.13
Nodes (3): UtteranceProgressListener, FakeLocalTtsService, LocalTtsService

### Community 56 - "PairedDownloadTest"
Cohesion: 0.25
Nodes (3): PairedDownload, Half, PairedDownloadTest

### Community 57 - "GraphDao"
Cohesion: 0.19
Nodes (3): GraphDao, GraphEdgeEntity, GraphNodeEntity

### Community 58 - ".viewModel()"
Cohesion: 0.25
Nodes (3): PinViewModelTest, FakeLockoutStore, FakePinStore

### Community 59 - "GraphHolder"
Cohesion: 0.23
Nodes (3): EmbeddingProvider, EmbeddingProviderFactory, GraphHolder

### Community 61 - "ModelSettings"
Cohesion: 0.29
Nodes (3): ModelSettings, ModelSettingsTest, SpyingLocalTtsService

### Community 64 - "GraphScreen.kt"
Cohesion: 0.17
Nodes (7): colorFor(), GraphCanvas(), GraphScreen(), kindLabel(), NodeRow(), Section(), timesLabel()

### Community 66 - "DownloadStatus"
Cohesion: 0.20
Nodes (7): DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING, Asset

### Community 68 - "CloudTtsService"
Cohesion: 0.20
Nodes (3): TtsServiceException, CloudTtsService, CloudTtsServiceTest

### Community 70 - "ChatScreenTest"
Cohesion: 0.25
Nodes (7): ChatScreenTest, FakeCatalog, FakeGraphRepository, FakeIntakeStore, FakeLocalTtsService, FakeProviderCatalog, FakeTtsService

### Community 78 - "ExportFiles"
Cohesion: 0.22
Nodes (4): ExportFiles, NarrativeFormat, MARKDOWN, PDF

### Community 81 - "MemoryVectorStore"
Cohesion: 0.23
Nodes (3): MemoryVectorStore, ScoredId, MemoryVectorStoreTest

### Community 83 - "PinLockoutTest"
Cohesion: 0.33
Nodes (3): PinLockoutTest, Clock, FakeLockoutStore

### Community 84 - "Persona"
Cohesion: 0.31
Nodes (3): Persona, TherapyPromptBuilder, TherapyPromptBuilderTest

### Community 86 - "NoteEntity"
Cohesion: 0.23
Nodes (3): SessionCountRow, NoteDao, NoteEntity

### Community 87 - "MessageEntity"
Cohesion: 0.23
Nodes (4): MessageCountRow, MessageDao, ModalityCountRow, MessageEntity

### Community 88 - "NewSessionScreen.kt"
Cohesion: 0.21
Nodes (4): prettifyEnumName(), SelectionChips(), Field(), NewSessionScreen()

### Community 90 - "PinScreen.kt"
Cohesion: 0.18
Nodes (3): NumberPad(), PinKey(), PinScreen()

### Community 95 - "GraphEdge"
Cohesion: 0.20
Nodes (4): GraphEdge, toDomain(), toEntity(), toSessionEntity()

### Community 96 - ".buildHolder()"
Cohesion: 0.26
Nodes (3): GraphHolderTest, FakeEmbeddingProvider, RecordingGraphRepository

### Community 100 - "ChatProtocol"
Cohesion: 0.34
Nodes (12): ChatProtocol, ApiError, ChatRequest, ErrorEnvelope, ErrorMetadata, ReqMessage, StreamChoice, StreamChunk (+4 more)

### Community 102 - "PinViewModel.kt"
Cohesion: 0.21
Nodes (6): PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel

### Community 103 - "OnboardingScreen.kt"
Cohesion: 0.35
Nodes (9): AboutYouStep(), ApiKeyStep(), DisclaimerStep(), GoalsStep(), IntakeStep(), LocalModelStep(), OnboardingScreen(), OnDeviceOnlyNote() (+1 more)

### Community 105 - "Role"
Cohesion: 0.26
Nodes (10): AnthropicProtocol, ApiError, ChatRequest, ReqMessage, StreamDelta, StreamEvent, Role, ASSISTANT (+2 more)

### Community 107 - "DashboardSection.kt"
Cohesion: 0.38
Nodes (7): GlobalStats, Count(), Counts(), DashboardSection(), Modalities(), plural(), Themes()

### Community 109 - "NarrativeScreen.kt"
Cohesion: 0.21
Nodes (3): ExportOption, formatDate(), NarrativeScreen()

### Community 118 - "ExportedFile"
Cohesion: 0.32
Nodes (5): ExportedFile, share(), GraphFormat, GRAPHML, JSON

## Knowledge Gaps
- **88 isolated node(s):** `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH`, `STOIC` (+83 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 318 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **85 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Message` connect `Message` to `ChatViewModelTest`, `NarrativeViewModelTest.kt`, `LocalLLMService`, `ChatViewModel.kt`, `SessionRepository`, `NarrativeViewModel.kt`, `CloudChatServiceTest`, `TherapyModality`, `RoomStatsRepositoryTest`, `ChatViewModelTest.kt`, `RoomSessionRepositoryTest.kt`, `ChatScreen.kt`, `CloudChatServiceTest.kt`, `.doSend()`, `ChatScreenTest.kt`, `ChatScreenTest`, `Persona`, `ChatProtocolTest`, `CloudChatService.kt`, `GraphEdge`, `ChatProtocol`, `Role`, `CloudChatService`, `InMemorySessionRepository`, `SessionMappersTest.kt`?**
  _High betweenness centrality (0.093) - this node is a cross-community bridge._
- **What connects `THERAPIST`, `COMPANION`, `SPIRITUAL` to the rest of the system?**
  _88 weakly-connected nodes found - possible missing edges._
- **Should `ChatViewModelTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05562579013906448 - nodes in this community are weakly interconnected._
- **Why does `GraphNode` connect `GraphNode` to `.generate()`, `DataModule.kt`, `SessionSummary`, `GraphExportTest`, `assertequals`, `DashboardTest`, `TherapyModality`, `RoomStatsRepositoryTest`, `ChatViewModelTest.kt`, `TherapyGraph`, `GraphSnapshot`, `HttpProviderCatalog.kt`, `GraphHolder`, `GraphScreen.kt`, `ChatScreenTest.kt`, `GraphEdge`, `.buildHolder()`, `GraphViewModel.kt`, `SessionMappersTest.kt`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Should `OnboardingViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.05290490100616683 - nodes in this community are weakly interconnected._
- **Why does `ChatViewModel` connect `ChatViewModel` to `ChatViewModelTest`, `OnboardingViewModel`, `VoiceConversationTest`, `LocalModel`, `DataModule.kt`, `ActiveSessionHolder`, `LocalLLMService`, `ChatViewModel.kt`, `UnusableModels`, `Message`, `Provider`, `SessionRepository`, `Test`, `TherapyModality`, `ChatViewModelTest.kt`, `HttpProviderCatalog.kt`, `GraphHolder`, `ModelSettings`, `.doSend()`, `ContinuousSpeechRecognizer`, `ChatScreenTest`, `.isPermanent()`, `NetworkModule.kt`, `Persona`, `SilenceClock`, `.save()`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **Should `VoiceConversationTest` be split into smaller, more focused modules?**
  _Cohesion score 0.0670122176971492 - nodes in this community are weakly interconnected._