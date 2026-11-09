# Graph Report - selfward-android  (2026-11-09)

## Corpus Check
- 241 files · ~372,202 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 19 file(s) not represented in the graph (top: .xml 12, .properties 2, (none) 1)

## Summary
- 2916 nodes · 7766 edges · 140 communities (65 shown, 75 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 165 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- DataModule.kt
- ModelRankingTest
- GraphExportTest
- VoiceConversationTest
- OnboardingViewModel
- CloudChatServiceTest.kt
- CloudChatServiceTest
- ChatScreenTest
- ContinuousSpeechRecognizer
- LocalModel
- ChatViewModelTest
- PersonaKind
- EmbeddingModelSpec
- ActiveSessionHolder
- ChatViewModel
- CrisisResourcesTest
- SettingsScreenTest.kt
- GraphNode
- FakeSecureSettings
- WordPieceTokenizer
- NarrativePdfWriter.kt
- MessageAnalyzerTest
- ModelSettings
- GraphRepository
- Message
- Test
- Provider
- FakeSpeechSource
- PriceTiersTest
- DreamRepository
- Test
- Note
- NarrativeViewModelTest
- DownloadStatus
- DeviceVoiceRankingTest
- Theme.kt
- SettingsScreen.kt
- ChatService
- DashboardTest
- AndroidEmbeddingModelDownloader
- Tally
- .generate()
- SessionRepository
- Test
- NarrativeScreen.kt
- RoomStatsRepositoryTest
- Block
- ChatViewModel.kt
- GraphDao
- com
- FakeSessionRepository
- GraphHolder
- .paginate()
- PairedDownloadTest
- SettingsViewModel
- JournalViewModelTest
- NewSessionViewModelTest
- Role
- .pool()
- GraphEdge
- NarrativeViewModelTest.kt
- GraphScreen.kt
- ChatScreen.kt
- NarrativePdfWriterTest.kt
- .buildRequest()
- DashboardViewModel.kt
- .detect()
- TherapyModality
- DreamDao
- SessionEntity
- ModelChoice
- Session
- NoteEntity
- MessageEntity
- NewSessionScreen.kt
- JournalViewModel
- MainScreen.kt
- ProviderDefaultsAdoptionTest
- MemoryVectorStore
- Persona
- SettingsViewModel.kt
- MigrationTest
- PinScreen.kt
- ChatProtocolTest
- TherapyInsights
- InMemorySessionRepository
- SelfwardDatabase.kt
- AndroidModelDownloader
- ExportFilesTest
- Test
- SessionSummary
- NarrativeEntity
- OnboardingScreen.kt
- DashboardSection.kt
- Test
- ExportFiles
- LlamaCppLocalService
- RoomGraphRepositoryTest.kt
- RoomSessionRepositoryTest.kt
- .isPermanent()
- CloudChatService
- .of()
- .extract()
- GraphViewModel.kt
- AnthropicProtocol
- .select()
- SessionMappersTest.kt
- .extract()
- AndroidTtsService
- .onDone()
- ModelRefusal
- .byId()
- FakeLocalTtsService
- RecordingLocalLLM
- render.sh

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

## Communities (140 total, 75 thin omitted)

### Community 0 - "DataModule.kt"
Cohesion: 0.05
Nodes (16): Incorrect, LockedOut, LockoutStore, PinAttempt, PinLockout, Success, PinService, PinStore (+8 more)

### Community 1 - "ModelRankingTest"
Cohesion: 0.06
Nodes (8): OpenRouterCatalog, ModelRanking, OpenRouterModel, UnusableModels, HttpOpenRouterCatalog, PrefsUnusableModels, NetworkModule, ModelRankingTest

### Community 2 - "GraphExportTest"
Cohesion: 0.05
Nodes (7): GraphExport, ExportedFile, share(), GraphFormat, GRAPHML, JSON, GraphExportTest

### Community 3 - "VoiceConversationTest"
Cohesion: 0.06
Nodes (26): ArmSilence, CancelSilence, Ended, Failed, Final, Partial, RecognizerEnded, RecognizerFailed (+18 more)

### Community 4 - "OnboardingViewModel"
Cohesion: 0.05
Nodes (19): Intake, IntakeContext, IntakeStore, EncryptedIntakeStore, AppRootViewModel, OnboardingStep, ABOUT_YOU, API_KEY (+11 more)

### Community 5 - "CloudChatServiceTest.kt"
Cohesion: 0.05
Nodes (8): TtsRequest, TtsService, TtsServiceException, CloudTtsService, TtsRequestTest, HttpOpenRouterCatalogTest, CloudTtsServiceTest, FakeTtsService

### Community 6 - "CloudChatServiceTest"
Cohesion: 0.08
Nodes (10): PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel, CloudChatServiceTest, FakeLockoutStore (+2 more)

### Community 7 - "ChatScreenTest"
Cohesion: 0.05
Nodes (10): ChatScreenTest, FakeCatalog, FakeChatService, FakeEmbeddingModelDownloader, FakeIntakeStore, FakeLocalLLMService, FakeLocalTtsService, FakeProviderCatalog (+2 more)

### Community 8 - "ContinuousSpeechRecognizer"
Cohesion: 0.06
Nodes (5): SpeechSource, AndroidSttService, RecognitionListener, ContinuousSpeechRecognizer, RecognitionListener

### Community 9 - "LocalModel"
Cohesion: 0.07
Nodes (7): LocalModel, ModelDownloader, FakeModelDownloader, DownloadedModelDownloader, FakeModelDownloader, FakeModelDownloader, FakeModelDownloader

### Community 10 - "ChatViewModelTest"
Cohesion: 0.12
Nodes (6): ChatViewModelTest, ChunkedChatService, FakeChatService, FakeTtsService, MissingKeyChatService, ProgrammableLocalLLMService

### Community 11 - "PersonaKind"
Cohesion: 0.06
Nodes (30): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, CompanionPersonality, BOLD, CALM (+22 more)

### Community 12 - "EmbeddingModelSpec"
Cohesion: 0.09
Nodes (6): EmbeddingModelCatalog, EmbeddingModelSpec, EmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 13 - "ActiveSessionHolder"
Cohesion: 0.16
Nodes (6): ActiveSessionHolder, SessionsScreen(), SessionsViewModel, FakeSessionRepository, FakeStatsRepository, SessionsScreenTest

### Community 15 - "CrisisResourcesTest"
Cohesion: 0.12
Nodes (5): CrisisResources, Resource, SafetyGuardrails, CrisisResourcesTest, SafetyGuardrailsTest

### Community 16 - "SettingsScreenTest.kt"
Cohesion: 0.10
Nodes (11): AboutSection(), DeviceVoicePicker(), DownloadActionButton(), DownloadProgressAndError(), OpenRouterModelRow(), OpenRouterModelSection(), providerLabel(), SettingsScreen() (+3 more)

### Community 17 - "GraphNode"
Cohesion: 0.12
Nodes (4): GraphNode, TherapyGraph, TherapyGraphTest, TherapyGraphUpsertTest

### Community 18 - "FakeSecureSettings"
Cohesion: 0.13
Nodes (5): FakeSecureSettings, FakeLocalTtsService, FakeOpenRouterCatalog, SettingsViewModelTest, LocalTtsService

### Community 19 - "WordPieceTokenizer"
Cohesion: 0.11
Nodes (4): TokenizedInput, TokenizerConfig, WordPieceTokenizer, WordPieceTokenizerTest

### Community 20 - "NarrativePdfWriter.kt"
Cohesion: 0.12
Nodes (10): Style, BODY, HEADING, SUBTITLE, TITLE, Cursor, NarrativePdfWriter, PageTarget (+2 more)

### Community 21 - "MessageAnalyzerTest"
Cohesion: 0.15
Nodes (7): EdgeSpec, Extraction, Kind, MessageAnalyzer, NodeSpec, Relation, MessageAnalyzerTest

### Community 22 - "ModelSettings"
Cohesion: 0.12
Nodes (5): ModelSettings, LocalTtsService, VoiceCatalog, ModelSettingsTest, SpyingLocalTtsService

### Community 23 - "GraphRepository"
Cohesion: 0.11
Nodes (7): GraphRepository, GraphSnapshot, FakeEmbeddingProvider, GraphHolderTest, RecordingGraphRepository, FakeGraphRepository, FakeGraphRepository

### Community 24 - "Message"
Cohesion: 0.11
Nodes (3): ChatServiceException, MissingApiKeyException, Message

### Community 27 - "Provider"
Cohesion: 0.14
Nodes (7): ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings, EncryptedSecureSettings

### Community 28 - "FakeSpeechSource"
Cohesion: 0.09
Nodes (6): FakeSpeechSource, ManualSilenceClock, LocalTtsService, FailingChatService, FakeLocalTtsService, FakeSessionRepository

### Community 30 - "DreamRepository"
Cohesion: 0.09
Nodes (7): Dream, DreamRepository, DreamSymbols, joinToList(), RoomDreamRepository, FakeDreamRepository, FakeDreamRepository

### Community 31 - "Test"
Cohesion: 0.19
Nodes (5): NarrativePrompt, NarrativeSource, NarrativeSources, NarrativePromptTest, NarrativeSourcesTest

### Community 32 - "Note"
Cohesion: 0.14
Nodes (10): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, splitList() (+2 more)

### Community 33 - "NarrativeViewModelTest"
Cohesion: 0.26
Nodes (5): FailingChatService, FakeNarrativeStore, FakeNoteRepository, NarrativeViewModelTest, RecordingChatService

### Community 34 - "DownloadStatus"
Cohesion: 0.12
Nodes (7): DownloadProgress, DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING

### Community 35 - "DeviceVoiceRankingTest"
Cohesion: 0.20
Nodes (7): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD, DeviceVoiceRankingTest

### Community 36 - "Theme.kt"
Cohesion: 0.11
Nodes (3): MainActivity, AppRoot(), SelfwardTheme()

### Community 37 - "SettingsScreen.kt"
Cohesion: 0.16
Nodes (4): DreamRow(), formatDate(), JournalScreen(), NoteRow()

### Community 38 - "ChatService"
Cohesion: 0.12
Nodes (9): ChatService, ChatService, ChatService, ChatService, ChatService, FakeCatalog, FakeUnusable, ObservingChatService (+1 more)

### Community 41 - "Tally"
Cohesion: 0.16
Nodes (5): MessageTally, StatsRepository, Tally, RoomStatsRepository, FailingStatsRepository

### Community 43 - "SessionRepository"
Cohesion: 0.13
Nodes (4): LocalLLMService, SessionRepository, NarrativeUiState, NarrativeViewModel

### Community 45 - "NarrativeScreen.kt"
Cohesion: 0.15
Nodes (3): ExportOption, formatDate(), NarrativeScreen()

### Community 47 - "Block"
Cohesion: 0.23
Nodes (3): Block, NarrativeTypesetTest, RecordingTarget

### Community 48 - "ChatViewModel.kt"
Cohesion: 0.12
Nodes (3): GGUFModelCatalog, SilenceClock, CoroutineSilenceClock

### Community 49 - "GraphDao"
Cohesion: 0.18
Nodes (3): GraphDao, GraphEdgeEntity, GraphNodeEntity

### Community 50 - "com"
Cohesion: 0.14
Nodes (4): FakeEmbeddingModelDownloader, FakeIntakeStore, FakeLocalLLMService, FakeProviderCatalog

### Community 51 - "FakeSessionRepository"
Cohesion: 0.14
Nodes (3): FakeSessionRepository, FakeStatsRepository, SessionsViewModelTest

### Community 52 - "GraphHolder"
Cohesion: 0.18
Nodes (3): EmbeddingProvider, EmbeddingProviderFactory, GraphHolder

### Community 53 - ".paginate()"
Cohesion: 0.25
Nodes (5): NarrativeExport, NarrativeFormat, MARKDOWN, PDF, NarrativeExportTest

### Community 54 - "PairedDownloadTest"
Cohesion: 0.25
Nodes (3): Half, PairedDownload, PairedDownloadTest

### Community 59 - "Role"
Cohesion: 0.21
Nodes (16): ApiError, ChatProtocol, ChatRequest, ErrorEnvelope, ErrorMetadata, ReqMessage, StreamChoice, StreamChunk (+8 more)

### Community 61 - "GraphEdge"
Cohesion: 0.14
Nodes (5): GraphEdge, toDomain(), toEntity(), toSessionEntity(), RoomGraphRepository

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

### Community 69 - "TherapyModality"
Cohesion: 0.12
Nodes (14): TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL, PURPOSE (+6 more)

### Community 72 - "ModelChoice"
Cohesion: 0.21
Nodes (3): ModelChoice, ProviderCatalog, HttpProviderCatalog

### Community 74 - "NoteEntity"
Cohesion: 0.21
Nodes (3): SessionCountRow, NoteDao, NoteEntity

### Community 75 - "MessageEntity"
Cohesion: 0.21
Nodes (4): MessageCountRow, MessageDao, ModalityCountRow, MessageEntity

### Community 76 - "NewSessionScreen.kt"
Cohesion: 0.19
Nodes (4): prettifyEnumName(), SelectionChips(), Field(), NewSessionScreen()

### Community 80 - "MemoryVectorStore"
Cohesion: 0.19
Nodes (3): MemoryVectorStore, ScoredId, MemoryVectorStoreTest

### Community 81 - "Persona"
Cohesion: 0.31
Nodes (3): Persona, TherapyPromptBuilder, TherapyPromptBuilderTest

### Community 84 - "PinScreen.kt"
Cohesion: 0.18
Nodes (3): NumberPad(), PinKey(), PinScreen()

### Community 86 - "TherapyInsights"
Cohesion: 0.20
Nodes (3): CycleDetector, Cycles, TherapyInsights

### Community 92 - "SessionSummary"
Cohesion: 0.23
Nodes (8): SessionStats, SessionSummary, ActiveList(), ArchiveList(), formatTimestamp(), plural(), SessionBadges(), SessionRow()

### Community 94 - "OnboardingScreen.kt"
Cohesion: 0.35
Nodes (9): AboutYouStep(), ApiKeyStep(), DisclaimerStep(), GoalsStep(), IntakeStep(), LocalModelStep(), OnboardingScreen(), OnDeviceOnlyNote() (+1 more)

### Community 96 - "DashboardSection.kt"
Cohesion: 0.29
Nodes (6): Count(), Counts(), DashboardSection(), Modalities(), plural(), Themes()

### Community 107 - "AnthropicProtocol"
Cohesion: 0.44
Nodes (6): AnthropicProtocol, ApiError, ChatRequest, ReqMessage, StreamDelta, StreamEvent

## Knowledge Gaps
- **74 isolated node(s):** `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH`, `STOIC` (+69 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 477 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **75 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Message` connect `Message` to `GraphExportTest`, `CloudChatServiceTest.kt`, `CloudChatServiceTest`, `ChatScreenTest`, `ChatViewModelTest`, `ChatViewModel`, `NarrativeViewModelTest`, `SessionRepository`, `RoomStatsRepositoryTest`, `ChatViewModel.kt`, `FakeSessionRepository`, `ChatViewModelTest.kt`, `Role`, `GraphEdge`, `NarrativeViewModelTest.kt`, `ChatScreen.kt`, `.buildRequest()`, `TherapyModality`, `Session`, `Persona`, `ChatProtocolTest`, `InMemorySessionRepository`, `ChatScreenTest.kt`, `LlamaCppLocalService`, `RoomSessionRepositoryTest.kt`, `CloudChatService`, `AnthropicProtocol`, `SessionMappersTest.kt`, `RecordingLocalLLM`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **What connects `THERAPIST`, `COMPANION`, `SPIRITUAL` to the rest of the system?**
  _74 weakly-connected nodes found - possible missing edges._
- **Should `DataModule.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.054431960049937576 - nodes in this community are weakly interconnected._
- **Why does `GraphNode` connect `GraphNode` to `GraphExportTest`, `DashboardViewModel.kt`, `.detect()`, `TherapyModality`, `RoomGraphRepositoryTest.kt`, `DashboardTest`, `.generate()`, `GraphViewModel.kt`, `SessionMappersTest.kt`, `RoomStatsRepositoryTest`, `GraphHolder`, `TherapyInsights`, `GraphRepository`, `ChatViewModelTest.kt`, `assertequals`, `GraphEdge`, `ChatScreenTest.kt`, `GraphScreen.kt`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **Should `ModelRankingTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05669050051072523 - nodes in this community are weakly interconnected._
- **Why does `ChatViewModel` connect `ChatViewModel` to `ModelRankingTest`, `VoiceConversationTest`, `OnboardingViewModel`, `CloudChatServiceTest.kt`, `ChatScreenTest`, `ContinuousSpeechRecognizer`, `LocalModel`, `ChatViewModelTest`, `ActiveSessionHolder`, `CrisisResourcesTest`, `ModelSettings`, `Provider`, `ChatService`, `SessionRepository`, `Test`, `ChatViewModel.kt`, `GraphHolder`, `ChatScreen.kt`, `TherapyModality`, `ModelChoice`, `Persona`, `SettingsViewModel.kt`, `ModelRefusal`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Should `GraphExportTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05399625768511093 - nodes in this community are weakly interconnected._