# Graph Report - selfward-android  (2026-10-26)

## Corpus Check
- 241 files · ~372,045 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 19 file(s) not represented in the graph (top: .xml 12, .properties 2, (none) 1)

## Summary
- 2916 nodes · 7770 edges · 137 communities (71 shown, 66 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 171 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- ModelRankingTest
- GraphExportTest
- Block
- CloudChatServiceTest.kt
- VoiceAction
- CloudChatServiceTest
- ChatViewModelTest
- LocalModel
- RoomStatsRepositoryTest
- ContinuousSpeechRecognizer
- ChatScreen.kt
- DeviceVoiceRankingTest
- EmbeddingModelSpec
- VoiceConversationTest
- CrisisResourcesTest
- GraphNode
- FakeSecureSettings
- NarrativeViewModelTest
- ActiveSessionHolder
- ChatViewModel
- SelfwardDatabase
- WordPieceTokenizer
- MessageAnalyzerTest
- Provider
- DownloadStatus
- Message
- TherapyGraph
- DreamRepository
- ModelSettings
- NarrativeViewModel.kt
- PriceTiersTest
- SessionSummary
- Note
- SessionRepository
- Theme.kt
- DashboardTest
- AndroidEmbeddingModelDownloader
- Tally
- .generate()
- Test
- Session
- FakeUnusable
- SpiritualTradition
- DashboardSection.kt
- PinLockout
- Test
- GraphDao
- ChatScreenTest
- MainScreen.kt
- SettingsViewModel
- NewSessionViewModelTest
- PersonaKind
- PairedDownloadTest
- JournalViewModelTest
- SettingsScreenTest
- GraphHolder
- .pool()
- OnboardingViewModel
- com
- .buildRequest()
- .paginate()
- .detect()
- DreamDao
- SessionEntity
- TherapyModality
- NoteEntity
- MessageEntity
- GraphScreen.kt
- JournalViewModel
- SettingsScreen.kt
- GraphHolderTest.kt
- FakeSpeechSource
- PinScreen.kt
- ModelChoice
- ProviderDefaultsAdoptionTest
- MemoryVectorStore
- Intake
- SelfwardDatabase.kt
- PinLockoutTest
- com
- Test
- ChatProtocolTest
- IntakeStore
- Persona
- InMemorySessionRepository
- AndroidModelDownloader
- OnboardingViewModelTest
- CloudChatService
- ChatProtocol
- TherapyInsights
- NarrativeEntity
- DataModule.kt
- OnboardingScreen.kt
- Test
- LlamaCppLocalService
- RoomGraphRepositoryTest.kt
- RoomSessionRepositoryTest.kt
- .isPermanent()
- LocalLLMService
- SilenceClock
- FakeSessionRepository
- SessionsScreen.kt
- .of()
- .extract()
- Inject
- ChatViewModel.kt
- GraphViewModel.kt
- AnthropicProtocol
- .select()
- EncryptedPinStore.kt
- .onDone()
- JournalScreen.kt
- OnboardingStep
- SessionMappersTest.kt
- FakeSessionRepository
- CompanionPersonality
- .extract()
- ExportedFile
- FakeUnusable
- .checkWhichModelsWork()
- GGUFModelCatalogTest.kt
- Role
- FakeLocalLLMService
- FakeLocalTtsService
- FakeLocalTtsService
- PersonaHolder
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

## Communities (137 total, 66 thin omitted)

### Community 0 - "ModelRankingTest"
Cohesion: 0.06
Nodes (8): OpenRouterCatalog, ModelRanking, OpenRouterModel, UnusableModels, HttpOpenRouterCatalog, PrefsUnusableModels, NetworkModule, ModelRankingTest

### Community 1 - "GraphExportTest"
Cohesion: 0.06
Nodes (5): GraphExport, GraphFormat, GRAPHML, JSON, GraphExportTest

### Community 2 - "Block"
Cohesion: 0.05
Nodes (14): NarrativePdfWriterTest, Block, Style, BODY, HEADING, SUBTITLE, TITLE, ExportFiles (+6 more)

### Community 3 - "CloudChatServiceTest.kt"
Cohesion: 0.06
Nodes (6): TtsRequest, TtsServiceException, CloudTtsService, TtsRequestTest, HttpOpenRouterCatalogTest, CloudTtsServiceTest

### Community 4 - "VoiceAction"
Cohesion: 0.06
Nodes (28): ArmSilence, CancelSilence, Ended, Failed, Final, Partial, RecognizerEnded, RecognizerFailed (+20 more)

### Community 5 - "CloudChatServiceTest"
Cohesion: 0.07
Nodes (10): PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel, CloudChatServiceTest, FakeLockoutStore (+2 more)

### Community 6 - "ChatViewModelTest"
Cohesion: 0.10
Nodes (10): ManualSilenceClock, ChatViewModelTest, ChunkedChatService, FailingChatService, FakeChatService, FakeIntakeStore, FakeTtsService, MissingKeyChatService (+2 more)

### Community 7 - "LocalModel"
Cohesion: 0.06
Nodes (8): GGUFModelCatalog, LocalModel, ModelDownloader, FakeModelDownloader, DownloadedModelDownloader, FakeModelDownloader, FakeModelDownloader, FakeModelDownloader

### Community 8 - "RoomStatsRepositoryTest"
Cohesion: 0.08
Nodes (3): ExportFilesTest, MigrationTest, RoomStatsRepositoryTest

### Community 9 - "ContinuousSpeechRecognizer"
Cohesion: 0.06
Nodes (5): SpeechSource, AndroidSttService, RecognitionListener, ContinuousSpeechRecognizer, RecognitionListener

### Community 10 - "ChatScreen.kt"
Cohesion: 0.09
Nodes (7): MessageBubble(), ModalityPicker(), ModelBar(), ModelRow(), prettifyEnumName(), Field(), NewSessionScreen()

### Community 11 - "DeviceVoiceRankingTest"
Cohesion: 0.10
Nodes (9): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD, LocalTtsService, AndroidTtsService (+1 more)

### Community 12 - "EmbeddingModelSpec"
Cohesion: 0.09
Nodes (6): EmbeddingModelCatalog, EmbeddingModelSpec, EmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 14 - "CrisisResourcesTest"
Cohesion: 0.12
Nodes (5): CrisisResources, Resource, SafetyGuardrails, CrisisResourcesTest, SafetyGuardrailsTest

### Community 15 - "GraphNode"
Cohesion: 0.09
Nodes (7): GraphEdge, GraphNode, GraphRepository, GraphSnapshot, RoomGraphRepository, FakeGraphRepository, FakeGraphRepository

### Community 16 - "FakeSecureSettings"
Cohesion: 0.12
Nodes (5): FakeSecureSettings, FakeLocalTtsService, FakeOpenRouterCatalog, SettingsViewModelTest, LocalTtsService

### Community 17 - "NarrativeViewModelTest"
Cohesion: 0.16
Nodes (5): FailingChatService, FakeNarrativeStore, FakeNoteRepository, NarrativeViewModelTest, RecordingChatService

### Community 18 - "ActiveSessionHolder"
Cohesion: 0.19
Nodes (6): ActiveSessionHolder, SessionsScreen(), SessionsViewModel, FakeSessionRepository, FakeStatsRepository, SessionsScreenTest

### Community 21 - "WordPieceTokenizer"
Cohesion: 0.11
Nodes (4): TokenizedInput, TokenizerConfig, WordPieceTokenizer, WordPieceTokenizerTest

### Community 22 - "MessageAnalyzerTest"
Cohesion: 0.15
Nodes (7): EdgeSpec, Extraction, Kind, MessageAnalyzer, NodeSpec, Relation, MessageAnalyzerTest

### Community 23 - "Provider"
Cohesion: 0.13
Nodes (7): ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings, EncryptedSecureSettings

### Community 24 - "DownloadStatus"
Cohesion: 0.10
Nodes (7): DownloadProgress, DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING

### Community 25 - "Message"
Cohesion: 0.13
Nodes (4): ChatService, ChatServiceException, MissingApiKeyException, Message

### Community 26 - "TherapyGraph"
Cohesion: 0.16
Nodes (3): TherapyGraph, TherapyGraphTest, TherapyGraphUpsertTest

### Community 27 - "DreamRepository"
Cohesion: 0.09
Nodes (7): Dream, DreamRepository, DreamSymbols, joinToList(), RoomDreamRepository, FakeDreamRepository, FakeDreamRepository

### Community 28 - "ModelSettings"
Cohesion: 0.13
Nodes (4): ModelSettings, VoiceCatalog, ModelSettingsTest, SpyingLocalTtsService

### Community 29 - "NarrativeViewModel.kt"
Cohesion: 0.12
Nodes (7): NarrativeDocument, NarrativePrompt, NarrativeSources, NarrativeStore, NarrativePdfWriter, NarrativeUiState, NarrativeViewModel

### Community 31 - "SessionSummary"
Cohesion: 0.11
Nodes (4): SessionSummary, FakeSessionRepository, FakeStatsRepository, SessionsViewModelTest

### Community 33 - "Note"
Cohesion: 0.14
Nodes (10): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, splitList() (+2 more)

### Community 34 - "SessionRepository"
Cohesion: 0.12
Nodes (4): Dashboard, SessionRepository, DashboardUiState, DashboardViewModel

### Community 35 - "Theme.kt"
Cohesion: 0.11
Nodes (3): MainActivity, AppRoot(), SelfwardTheme()

### Community 38 - "Tally"
Cohesion: 0.16
Nodes (5): MessageTally, StatsRepository, Tally, RoomStatsRepository, FailingStatsRepository

### Community 41 - "Session"
Cohesion: 0.11
Nodes (5): Session, toDomain(), toEntity(), toSessionEntity(), RoomSessionRepository

### Community 42 - "FakeUnusable"
Cohesion: 0.12
Nodes (7): ChatService, ChatService, ChatService, ChatService, FakeCatalog, FakeUnusable, RefusesFirstModel

### Community 43 - "SpiritualTradition"
Cohesion: 0.10
Nodes (17): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, CrisisPattern, SpiritualTradition, BUDDHIST (+9 more)

### Community 44 - "DashboardSection.kt"
Cohesion: 0.18
Nodes (8): GlobalStats, SelectionChips(), Count(), Counts(), DashboardSection(), Modalities(), plural(), Themes()

### Community 45 - "PinLockout"
Cohesion: 0.15
Nodes (8): Incorrect, LockedOut, LockoutStore, PinAttempt, PinLockout, Success, PinService, PinStore

### Community 46 - "Test"
Cohesion: 0.27
Nodes (3): NarrativeSource, NarrativePromptTest, NarrativeSourcesTest

### Community 47 - "GraphDao"
Cohesion: 0.18
Nodes (3): GraphDao, GraphEdgeEntity, GraphNodeEntity

### Community 48 - "ChatScreenTest"
Cohesion: 0.21
Nodes (5): ChatScreen(), ChatScreenTest, FakeCatalog, FakeChatService, FakeTtsService

### Community 49 - "MainScreen.kt"
Cohesion: 0.11
Nodes (4): MainScreen(), Tab, formatDate(), NarrativeScreen()

### Community 52 - "PersonaKind"
Cohesion: 0.14
Nodes (6): PersonaKind, COMPANION, SPIRITUAL, THERAPIST, NewSessionUiState, NewSessionViewModel

### Community 53 - "PairedDownloadTest"
Cohesion: 0.25
Nodes (3): Half, PairedDownload, PairedDownloadTest

### Community 55 - "SettingsScreenTest"
Cohesion: 0.18
Nodes (3): FakeLocalTtsService, FakeOpenRouterCatalog, SettingsScreenTest

### Community 56 - "GraphHolder"
Cohesion: 0.19
Nodes (3): EmbeddingProvider, EmbeddingProviderFactory, GraphHolder

### Community 60 - "com"
Cohesion: 0.16
Nodes (3): FakeEmbeddingModelDownloader, FakeLocalLLMService, FakeProviderCatalog

### Community 66 - "TherapyModality"
Cohesion: 0.13
Nodes (14): TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL, PURPOSE (+6 more)

### Community 67 - "NoteEntity"
Cohesion: 0.21
Nodes (3): SessionCountRow, NoteDao, NoteEntity

### Community 68 - "MessageEntity"
Cohesion: 0.21
Nodes (4): MessageCountRow, MessageDao, ModalityCountRow, MessageEntity

### Community 69 - "GraphScreen.kt"
Cohesion: 0.21
Nodes (8): ExportOption, colorFor(), GraphCanvas(), GraphScreen(), kindLabel(), NodeRow(), Section(), timesLabel()

### Community 71 - "SettingsScreen.kt"
Cohesion: 0.22
Nodes (8): AboutSection(), DeviceVoicePicker(), DownloadActionButton(), DownloadProgressAndError(), OpenRouterModelRow(), OpenRouterModelSection(), providerLabel(), SettingsScreen()

### Community 72 - "GraphHolderTest.kt"
Cohesion: 0.22
Nodes (3): FakeEmbeddingProvider, GraphHolderTest, RecordingGraphRepository

### Community 73 - "FakeSpeechSource"
Cohesion: 0.14
Nodes (3): FakeSpeechSource, LocalTtsService, FakeLocalTtsService

### Community 75 - "PinScreen.kt"
Cohesion: 0.17
Nodes (3): NumberPad(), PinKey(), PinScreen()

### Community 76 - "ModelChoice"
Cohesion: 0.23
Nodes (3): ModelChoice, ProviderCatalog, HttpProviderCatalog

### Community 78 - "MemoryVectorStore"
Cohesion: 0.19
Nodes (3): MemoryVectorStore, ScoredId, MemoryVectorStoreTest

### Community 79 - "Intake"
Cohesion: 0.26
Nodes (4): Intake, IntakeContext, IntakeContextTest, FakeIntakeStore

### Community 81 - "PinLockoutTest"
Cohesion: 0.33
Nodes (3): Clock, FakeLockoutStore, PinLockoutTest

### Community 82 - "com"
Cohesion: 0.20
Nodes (3): FakeEmbeddingModelDownloader, FakeIntakeStore, FakeProviderCatalog

### Community 85 - "IntakeStore"
Cohesion: 0.19
Nodes (3): IntakeStore, EncryptedIntakeStore, AppRootViewModel

### Community 86 - "Persona"
Cohesion: 0.33
Nodes (3): Persona, TherapyPromptBuilder, TherapyPromptBuilderTest

### Community 91 - "ChatProtocol"
Cohesion: 0.34
Nodes (12): ApiError, ChatProtocol, ChatRequest, ErrorEnvelope, ErrorMetadata, ReqMessage, StreamChoice, StreamChunk (+4 more)

### Community 92 - "TherapyInsights"
Cohesion: 0.22
Nodes (3): CycleDetector, Cycles, TherapyInsights

### Community 95 - "OnboardingScreen.kt"
Cohesion: 0.35
Nodes (9): AboutYouStep(), ApiKeyStep(), DisclaimerStep(), GoalsStep(), IntakeStep(), LocalModelStep(), OnboardingScreen(), OnDeviceOnlyNote() (+1 more)

### Community 104 - "SessionsScreen.kt"
Cohesion: 0.49
Nodes (7): SessionStats, ActiveList(), ArchiveList(), formatTimestamp(), plural(), SessionBadges(), SessionRow()

### Community 110 - "AnthropicProtocol"
Cohesion: 0.44
Nodes (6): AnthropicProtocol, ApiError, ChatRequest, ReqMessage, StreamDelta, StreamEvent

### Community 114 - "JournalScreen.kt"
Cohesion: 0.42
Nodes (4): DreamRow(), formatDate(), JournalScreen(), NoteRow()

### Community 115 - "OnboardingStep"
Cohesion: 0.22
Nodes (9): OnboardingStep, ABOUT_YOU, API_KEY, CONCERNS, DISCLAIMER, GOALS, HISTORY, LOCAL_MODEL (+1 more)

### Community 118 - "CompanionPersonality"
Cohesion: 0.25
Nodes (7): CompanionPersonality, BOLD, CALM, CHEERFUL, DEEP, PLAYFUL, WARM

### Community 120 - "ExportedFile"
Cohesion: 0.32
Nodes (5): ExportedFile, share(), NarrativeFormat, MARKDOWN, PDF

### Community 125 - "Role"
Cohesion: 0.40
Nodes (4): Role, ASSISTANT, SYSTEM, USER

## Knowledge Gaps
- **74 isolated node(s):** `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH`, `STOIC` (+69 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 472 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **66 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Message` connect `Message` to `GraphExportTest`, `CloudChatServiceTest.kt`, `CloudChatServiceTest`, `ChatViewModelTest`, `RoomStatsRepositoryTest`, `ChatScreen.kt`, `NarrativeViewModelTest`, `ChatViewModel`, `NarrativeViewModel.kt`, `SessionSummary`, `SessionRepository`, `Session`, `SpiritualTradition`, `ChatScreenTest`, `ChatScreenTest.kt`, `.buildRequest()`, `TherapyModality`, `ChatViewModelTest.kt`, `ChatProtocolTest`, `Persona`, `InMemorySessionRepository`, `CloudChatService`, `ChatProtocol`, `LlamaCppLocalService`, `RoomSessionRepositoryTest.kt`, `LocalLLMService`, `FakeSessionRepository`, `ChatViewModel.kt`, `AnthropicProtocol`, `SessionMappersTest.kt`, `FakeSessionRepository`, `SessionsScreenTest.kt`, `.checkWhichModelsWork()`, `Role`, `FakeLocalLLMService`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **What connects `THERAPIST`, `COMPANION`, `SPIRITUAL` to the rest of the system?**
  _74 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ModelRankingTest` be split into smaller, more focused modules?**
  _Cohesion score 0.0577324973876698 - nodes in this community are weakly interconnected._
- **Why does `GraphNode` connect `GraphNode` to `GraphExportTest`, `RoomStatsRepositoryTest`, `TherapyGraph`, `assertequals`, `SessionRepository`, `DashboardTest`, `.generate()`, `Session`, `GraphHolder`, `ChatScreenTest.kt`, `.detect()`, `TherapyModality`, `GraphScreen.kt`, `GraphHolderTest.kt`, `ChatViewModelTest.kt`, `TherapyInsights`, `RoomGraphRepositoryTest.kt`, `GraphViewModel.kt`, `SessionMappersTest.kt`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **Should `GraphExportTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05546218487394958 - nodes in this community are weakly interconnected._
- **Why does `ChatViewModel` connect `ChatViewModel` to `ModelRankingTest`, `PersonaHolder`, `VoiceAction`, `ChatViewModelTest`, `LocalModel`, `ContinuousSpeechRecognizer`, `CrisisResourcesTest`, `ActiveSessionHolder`, `Provider`, `ModelSettings`, `SessionRepository`, `Test`, `ChatScreenTest`, `GraphHolder`, `TherapyModality`, `ModelChoice`, `IntakeStore`, `Persona`, `LocalLLMService`, `SilenceClock`, `ChatViewModel.kt`, `.onDone()`, `.checkWhichModelsWork()`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **Should `Block` be split into smaller, more focused modules?**
  _Cohesion score 0.05465765501028504 - nodes in this community are weakly interconnected._