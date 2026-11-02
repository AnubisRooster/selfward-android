# Graph Report - selfward-android  (2026-11-02)

## Corpus Check
- 241 files · ~372,045 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 19 file(s) not represented in the graph (top: .xml 12, .properties 2, (none) 1)

## Summary
- 2916 nodes · 7770 edges · 162 communities (67 shown, 95 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 171 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- OnboardingViewModel
- VoiceAction
- CloudChatServiceTest
- ModelRankingTest
- .generate()
- LocalModel
- ContinuousSpeechRecognizer
- MemoryVectorStore
- ChatViewModelTest
- EmbeddingModelSpec
- VoiceConversationTest
- RoomStatsRepositoryTest
- CrisisResourcesTest
- SettingsScreenTest.kt
- Test
- Message
- GraphRepository
- FakeSecureSettings
- ActiveSessionHolder
- SessionSummary
- WordPieceTokenizer
- NarrativePdfWriter.kt
- MessageAnalyzerTest
- GraphExportTest
- SelfwardDatabase
- Provider
- PriceTiersTest
- DreamRepository
- Test
- TherapyGraph
- ChatViewModel
- Note
- DeviceVoiceRankingTest
- NarrativeViewModelTest
- FakeSessionRepository
- DownloadStatus
- Theme.kt
- SettingsScreen.kt
- DashboardTest
- GraphNode
- AndroidEmbeddingModelDownloader
- Tally
- SessionRepository
- Test
- NarrativeScreen.kt
- Block
- PinLockout
- SettingsViewModel.kt
- NewSessionViewModel.kt
- ModelSettings
- GraphDao
- com
- NewSessionViewModelTest
- GraphHolder
- .paginate()
- PairedDownloadTest
- SettingsViewModel
- FakeUnusable
- .pool()
- NarrativeViewModelTest.kt
- GraphScreen.kt
- ChatScreen.kt
- NarrativePdfWriterTest.kt
- .buildRequest()
- DashboardViewModel.kt
- TherapyModality
- TtsRequest
- GraphViewModel.kt
- DreamDao
- SessionEntity
- JournalViewModelTest
- PersonaKind
- ModelChoice
- OpenRouterCatalog
- UnusableModels
- NoteEntity
- MessageEntity
- NewSessionScreen.kt
- JournalViewModel
- MainScreen.kt
- ChatScreenTest
- ProviderDefaultsAdoptionTest
- PinLockoutTest
- HttpOpenRouterCatalogTest
- PinScreen.kt
- ChatProtocolTest
- Persona
- InMemorySessionRepository
- SelfwardDatabase.kt
- AndroidModelDownloader
- Test
- NarrativeEntity
- OnboardingScreen.kt
- ChatProtocol
- DataModule.kt
- DashboardSection.kt
- LocalTtsService
- ExportFiles
- LlamaCppLocalService
- .onDone()
- RoomGraphRepositoryTest.kt
- RoomSessionRepositoryTest.kt
- com
- .isPermanent()
- CloudChatService
- Test
- SilenceClock
- EncryptedPinStore.kt
- SpiritualTradition
- ChatViewModel.kt
- .extract()
- CloudTtsService
- AnthropicProtocol
- .select()
- SessionMappersTest.kt
- FakeSessionRepository
- FakeUnusable
- .checkWhichModelsWork()
- FakeSessionRepository
- Role
- .extract()
- GGUFModelCatalogTest.kt
- TtsServiceException
- FakeLocalLLMService
- FakeLocalTtsService
- FakeIntakeStore
- FakeLocalTtsService
- RecordingLocalLLM
- FakeCatalog
- render.sh
- FakeChatService

## God Nodes (most connected - your core abstractions)
1. `Message` - 105 edges
2. `LocalModel` - 79 edges
3. `ChatViewModelTest` - 76 edges
4. `ChatViewModel` - 69 edges
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

## Communities (162 total, 95 thin omitted)

### Community 0 - "OnboardingViewModel"
Cohesion: 0.05
Nodes (19): Intake, IntakeContext, IntakeStore, EncryptedIntakeStore, AppRootViewModel, OnboardingStep, ABOUT_YOU, API_KEY (+11 more)

### Community 1 - "VoiceAction"
Cohesion: 0.06
Nodes (27): ArmSilence, CancelSilence, Ended, Failed, Final, Partial, RecognizerEnded, RecognizerFailed (+19 more)

### Community 2 - "CloudChatServiceTest"
Cohesion: 0.08
Nodes (10): PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel, CloudChatServiceTest, FakeLockoutStore (+2 more)

### Community 3 - "ModelRankingTest"
Cohesion: 0.11
Nodes (4): ModelRanking, OpenRouterModel, HttpOpenRouterCatalog, ModelRankingTest

### Community 4 - ".generate()"
Cohesion: 0.11
Nodes (5): CycleDetector, Cycles, TherapyInsights, CycleDetectorTest, TherapyInsightsTest

### Community 5 - "LocalModel"
Cohesion: 0.07
Nodes (7): LocalModel, ModelDownloader, FakeModelDownloader, DownloadedModelDownloader, FakeModelDownloader, FakeModelDownloader, FakeModelDownloader

### Community 6 - "ContinuousSpeechRecognizer"
Cohesion: 0.06
Nodes (5): SpeechSource, AndroidSttService, RecognitionListener, ContinuousSpeechRecognizer, RecognitionListener

### Community 7 - "MemoryVectorStore"
Cohesion: 0.08
Nodes (6): CosineSimilarity, MemoryVectorStore, ScoredId, CosineSimilarityTest, MemoryVectorStoreTest, MigrationTest

### Community 8 - "ChatViewModelTest"
Cohesion: 0.13
Nodes (10): ChatService, ChatViewModelTest, ChunkedChatService, FailingChatService, FakeChatService, FakeIntakeStore, FakeTtsService, MissingKeyChatService (+2 more)

### Community 9 - "EmbeddingModelSpec"
Cohesion: 0.09
Nodes (6): EmbeddingModelCatalog, EmbeddingModelSpec, EmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 12 - "CrisisResourcesTest"
Cohesion: 0.12
Nodes (5): CrisisResources, Resource, SafetyGuardrails, CrisisResourcesTest, SafetyGuardrailsTest

### Community 13 - "SettingsScreenTest.kt"
Cohesion: 0.10
Nodes (11): AboutSection(), DeviceVoicePicker(), DownloadActionButton(), DownloadProgressAndError(), OpenRouterModelRow(), OpenRouterModelSection(), providerLabel(), SettingsScreen() (+3 more)

### Community 14 - "Test"
Cohesion: 0.10
Nodes (4): FakeSpeechSource, ManualSilenceClock, LocalTtsService, FakeLocalTtsService

### Community 15 - "Message"
Cohesion: 0.10
Nodes (3): ChatServiceException, MissingApiKeyException, Message

### Community 16 - "GraphRepository"
Cohesion: 0.10
Nodes (7): GraphRepository, GraphSnapshot, FakeEmbeddingProvider, GraphHolderTest, RecordingGraphRepository, FakeGraphRepository, FakeGraphRepository

### Community 17 - "FakeSecureSettings"
Cohesion: 0.13
Nodes (5): FakeSecureSettings, FakeLocalTtsService, FakeOpenRouterCatalog, SettingsViewModelTest, LocalTtsService

### Community 18 - "ActiveSessionHolder"
Cohesion: 0.19
Nodes (6): ActiveSessionHolder, SessionsScreen(), SessionsViewModel, FakeSessionRepository, FakeStatsRepository, SessionsScreenTest

### Community 19 - "SessionSummary"
Cohesion: 0.09
Nodes (11): SessionStats, Session, SessionSummary, toSessionEntity(), RoomSessionRepository, ActiveList(), ArchiveList(), formatTimestamp() (+3 more)

### Community 20 - "WordPieceTokenizer"
Cohesion: 0.11
Nodes (4): TokenizedInput, TokenizerConfig, WordPieceTokenizer, WordPieceTokenizerTest

### Community 21 - "NarrativePdfWriter.kt"
Cohesion: 0.12
Nodes (10): Style, BODY, HEADING, SUBTITLE, TITLE, Cursor, NarrativePdfWriter, PageTarget (+2 more)

### Community 22 - "MessageAnalyzerTest"
Cohesion: 0.15
Nodes (7): EdgeSpec, Extraction, Kind, MessageAnalyzer, NodeSpec, Relation, MessageAnalyzerTest

### Community 25 - "Provider"
Cohesion: 0.14
Nodes (7): ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings, EncryptedSecureSettings

### Community 28 - "DreamRepository"
Cohesion: 0.09
Nodes (7): Dream, DreamRepository, DreamSymbols, joinToList(), RoomDreamRepository, FakeDreamRepository, FakeDreamRepository

### Community 29 - "Test"
Cohesion: 0.19
Nodes (5): NarrativePrompt, NarrativeSource, NarrativeSources, NarrativePromptTest, NarrativeSourcesTest

### Community 31 - "TherapyGraph"
Cohesion: 0.17
Nodes (3): TherapyGraph, TherapyGraphTest, TherapyGraphUpsertTest

### Community 33 - "Note"
Cohesion: 0.14
Nodes (10): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, splitList() (+2 more)

### Community 34 - "DeviceVoiceRankingTest"
Cohesion: 0.19
Nodes (7): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD, DeviceVoiceRankingTest

### Community 35 - "NarrativeViewModelTest"
Cohesion: 0.26
Nodes (5): FailingChatService, FakeNarrativeStore, FakeNoteRepository, NarrativeViewModelTest, RecordingChatService

### Community 36 - "FakeSessionRepository"
Cohesion: 0.10
Nodes (3): FakeSessionRepository, FakeStatsRepository, SessionsViewModelTest

### Community 37 - "DownloadStatus"
Cohesion: 0.12
Nodes (7): DownloadProgress, DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING

### Community 38 - "Theme.kt"
Cohesion: 0.11
Nodes (3): MainActivity, AppRoot(), SelfwardTheme()

### Community 39 - "SettingsScreen.kt"
Cohesion: 0.16
Nodes (4): DreamRow(), formatDate(), JournalScreen(), NoteRow()

### Community 41 - "GraphNode"
Cohesion: 0.12
Nodes (5): GraphEdge, GraphNode, toDomain(), toEntity(), RoomGraphRepository

### Community 43 - "Tally"
Cohesion: 0.16
Nodes (5): MessageTally, StatsRepository, Tally, RoomStatsRepository, FailingStatsRepository

### Community 44 - "SessionRepository"
Cohesion: 0.13
Nodes (5): GGUFModelCatalog, LocalLLMService, SessionRepository, NarrativeUiState, NarrativeViewModel

### Community 46 - "NarrativeScreen.kt"
Cohesion: 0.15
Nodes (3): ExportOption, formatDate(), NarrativeScreen()

### Community 47 - "Block"
Cohesion: 0.23
Nodes (3): Block, NarrativeTypesetTest, RecordingTarget

### Community 48 - "PinLockout"
Cohesion: 0.15
Nodes (8): Incorrect, LockedOut, LockoutStore, PinAttempt, PinLockout, Success, PinService, PinStore

### Community 49 - "SettingsViewModel.kt"
Cohesion: 0.13
Nodes (3): PersonaHolder, VoiceCatalog, MainApplication

### Community 51 - "NewSessionViewModel.kt"
Cohesion: 0.13
Nodes (9): CompanionPersonality, BOLD, CALM, CHEERFUL, DEEP, PLAYFUL, WARM, NewSessionUiState (+1 more)

### Community 52 - "ModelSettings"
Cohesion: 0.20
Nodes (3): ModelSettings, ModelSettingsTest, SpyingLocalTtsService

### Community 53 - "GraphDao"
Cohesion: 0.18
Nodes (3): GraphDao, GraphEdgeEntity, GraphNodeEntity

### Community 54 - "com"
Cohesion: 0.14
Nodes (4): ChatService, FakeEmbeddingModelDownloader, FakeLocalLLMService, FakeProviderCatalog

### Community 56 - "GraphHolder"
Cohesion: 0.18
Nodes (3): EmbeddingProvider, EmbeddingProviderFactory, GraphHolder

### Community 57 - ".paginate()"
Cohesion: 0.25
Nodes (5): NarrativeExport, NarrativeFormat, MARKDOWN, PDF, NarrativeExportTest

### Community 58 - "PairedDownloadTest"
Cohesion: 0.25
Nodes (3): Half, PairedDownload, PairedDownloadTest

### Community 60 - "FakeUnusable"
Cohesion: 0.13
Nodes (6): ChatService, ChatService, ChatService, FakeCatalog, FakeUnusable, RefusesFirstModel

### Community 62 - "NarrativeViewModelTest.kt"
Cohesion: 0.13
Nodes (3): NarrativeDocument, NarrativeStore, RoomNarrativeStore

### Community 63 - "GraphScreen.kt"
Cohesion: 0.17
Nodes (7): colorFor(), GraphCanvas(), GraphScreen(), kindLabel(), NodeRow(), Section(), timesLabel()

### Community 64 - "ChatScreen.kt"
Cohesion: 0.19
Nodes (6): ChatScreen(), MessageBubble(), ModalityPicker(), ModelBar(), ModelRow(), VoiceStatus()

### Community 67 - "DashboardViewModel.kt"
Cohesion: 0.20
Nodes (4): Dashboard, GlobalStats, DashboardUiState, DashboardViewModel

### Community 68 - "TherapyModality"
Cohesion: 0.12
Nodes (14): TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL, PURPOSE (+6 more)

### Community 70 - "GraphViewModel.kt"
Cohesion: 0.18
Nodes (7): ExportedFile, share(), GraphFormat, GRAPHML, JSON, GraphViewModel, InsightsUiState

### Community 75 - "PersonaKind"
Cohesion: 0.14
Nodes (11): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, CrisisPattern, PersonaKind, COMPANION (+3 more)

### Community 76 - "ModelChoice"
Cohesion: 0.21
Nodes (3): ModelChoice, ProviderCatalog, HttpProviderCatalog

### Community 79 - "NoteEntity"
Cohesion: 0.21
Nodes (3): SessionCountRow, NoteDao, NoteEntity

### Community 80 - "MessageEntity"
Cohesion: 0.21
Nodes (4): MessageCountRow, MessageDao, ModalityCountRow, MessageEntity

### Community 81 - "NewSessionScreen.kt"
Cohesion: 0.19
Nodes (4): prettifyEnumName(), SelectionChips(), Field(), NewSessionScreen()

### Community 86 - "PinLockoutTest"
Cohesion: 0.33
Nodes (3): Clock, FakeLockoutStore, PinLockoutTest

### Community 88 - "PinScreen.kt"
Cohesion: 0.18
Nodes (3): NumberPad(), PinKey(), PinScreen()

### Community 90 - "Persona"
Cohesion: 0.33
Nodes (3): Persona, TherapyPromptBuilder, TherapyPromptBuilderTest

### Community 96 - "OnboardingScreen.kt"
Cohesion: 0.35
Nodes (9): AboutYouStep(), ApiKeyStep(), DisclaimerStep(), GoalsStep(), IntakeStep(), LocalModelStep(), OnboardingScreen(), OnDeviceOnlyNote() (+1 more)

### Community 99 - "ChatProtocol"
Cohesion: 0.37
Nodes (12): ApiError, ChatProtocol, ChatRequest, ErrorEnvelope, ErrorMetadata, ReqMessage, StreamChoice, StreamChunk (+4 more)

### Community 101 - "DashboardSection.kt"
Cohesion: 0.29
Nodes (6): Count(), Counts(), DashboardSection(), Modalities(), plural(), Themes()

### Community 114 - "SpiritualTradition"
Cohesion: 0.20
Nodes (10): SpiritualTradition, BUDDHIST, CHRISTIAN, HINDU, INTERFAITH, ISLAMIC, JEWISH, SECULAR (+2 more)

### Community 119 - "AnthropicProtocol"
Cohesion: 0.44
Nodes (6): AnthropicProtocol, ApiError, ChatRequest, ReqMessage, StreamDelta, StreamEvent

### Community 126 - "Role"
Cohesion: 0.33
Nodes (4): Role, ASSISTANT, SYSTEM, USER

## Knowledge Gaps
- **74 isolated node(s):** `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH`, `STOIC` (+69 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 477 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **95 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Message` connect `Message` to `.streamReply()`, `CloudChatServiceTest`, `FakeLocalLLMService`, `ChatViewModelTest`, `RecordingLocalLLM`, `RoomStatsRepositoryTest`, `FakeChatService`, `SessionSummary`, `HttpProviderCatalog.kt`, `ChatViewModel`, `NarrativeViewModelTest`, `FakeSessionRepository`, `GraphNode`, `SessionRepository`, `CloudChatServiceTest.kt`, `NarrativeViewModelTest.kt`, `ChatScreen.kt`, `.buildRequest()`, `TherapyModality`, `ChatViewModelTest.kt`, `PersonaKind`, `ChatScreenTest`, `ChatProtocolTest`, `Persona`, `InMemorySessionRepository`, `ChatScreenTest.kt`, `LlamaCppLocalService`, `RoomSessionRepositoryTest.kt`, `CloudChatService`, `ChatViewModel.kt`, `AnthropicProtocol`, `SessionMappersTest.kt`, `FakeSessionRepository`, `.checkWhichModelsWork()`, `Role`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **What connects `THERAPIST`, `COMPANION`, `SPIRITUAL` to the rest of the system?**
  _74 weakly-connected nodes found - possible missing edges._
- **Should `OnboardingViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.05405405405405406 - nodes in this community are weakly interconnected._
- **Why does `GraphNode` connect `GraphNode` to `ChatScreenTest.kt`, `DashboardViewModel.kt`, `.generate()`, `TherapyModality`, `GraphViewModel.kt`, `DashboardTest`, `ChatViewModelTest.kt`, `RoomGraphRepositoryTest.kt`, `RoomStatsRepositoryTest`, `GraphRepository`, `GraphExportTest.kt`, `GraphExportTest`, `GraphHolder`, `SessionMappersTest.kt`, `assertequals`, `GraphScreen.kt`, `TherapyGraph`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **Should `VoiceAction` be split into smaller, more focused modules?**
  _Cohesion score 0.060764587525150904 - nodes in this community are weakly interconnected._
- **Why does `ChatViewModel` connect `ChatViewModel` to `OnboardingViewModel`, `VoiceAction`, `.streamReply()`, `LocalModel`, `ContinuousSpeechRecognizer`, `ChatViewModelTest`, `CrisisResourcesTest`, `Test`, `ActiveSessionHolder`, `Provider`, `SessionRepository`, `Test`, `SettingsViewModel.kt`, `ModelSettings`, `GraphHolder`, `ChatScreen.kt`, `TherapyModality`, `ModelChoice`, `OpenRouterCatalog`, `UnusableModels`, `ChatScreenTest`, `Persona`, `DataModule.kt`, `LocalTtsService`, `.onDone()`, `SilenceClock`, `ChatViewModel.kt`, `.checkWhichModelsWork()`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **Should `CloudChatServiceTest` be split into smaller, more focused modules?**
  _Cohesion score 0.07562136435748282 - nodes in this community are weakly interconnected._