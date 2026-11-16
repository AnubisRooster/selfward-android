# Graph Report - selfward-android  (2026-11-16)

## Corpus Check
- 241 files · ~371,960 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 19 file(s) not represented in the graph (top: .xml 12, .properties 2, (none) 1)

## Summary
- 2916 nodes · 7766 edges · 140 communities (68 shown, 72 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 165 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- ModelRankingTest
- GraphExportTest
- CloudChatServiceTest.kt
- RoomStatsRepositoryTest
- NarrativeViewModelTest
- VoiceAction
- ChatScreenTest
- CloudChatServiceTest
- ContinuousSpeechRecognizer
- LocalModel
- DeviceVoiceRankingTest
- OnboardingViewModel
- EmbeddingModelSpec
- VoiceConversationTest
- SettingsScreenTest.kt
- ChatViewModel.kt
- ChatViewModel
- CrisisResourcesTest
- GraphNode
- FakeSecureSettings
- ActiveSessionHolder
- GraphRepository
- PinScreen.kt
- WordPieceTokenizer
- NarrativePdfWriter.kt
- MessageAnalyzerTest
- SelfwardDatabase
- ChatViewModelTest
- Provider
- SessionRepository
- ModelSettings
- PriceTiersTest
- DreamRepository
- Test
- Message
- ChatService
- TherapyGraph
- SessionsScreen.kt
- DownloadStatus
- Note
- SettingsScreen.kt
- FakeSessionRepository
- DashboardTest
- Intake
- AndroidEmbeddingModelDownloader
- Tally
- .generate()
- Test
- Theme.kt
- ChatScreen.kt
- Block
- PinLockout
- SettingsViewModel
- GraphDao
- NewSessionViewModelTest
- .paginate()
- NarrativeViewModel.kt
- PairedDownloadTest
- JournalViewModelTest
- GraphHolder
- .pool()
- NarrativePdfWriterTest.kt
- PersonaKind
- .buildRequest()
- .detect()
- TherapyModality
- EncryptedPinStore.kt
- DreamDao
- SessionEntity
- GraphScreen.kt
- FakeChatService
- .buildVm()
- ModelChoice
- NoteEntity
- MessageEntity
- JournalViewModel
- MainScreen.kt
- ProviderDefaultsAdoptionTest
- PinLockoutTest
- FakeSpeechSource
- com
- ChatProtocolTest
- Persona
- InMemorySessionRepository
- SelfwardDatabase.kt
- AndroidModelDownloader
- Test
- Role
- ChatProtocol
- TherapyInsights
- OnboardingScreen.kt
- DashboardViewModel.kt
- CloudChatService
- Test
- ExportFiles
- LlamaCppLocalService
- RoomGraphRepositoryTest.kt
- RoomSessionRepositoryTest.kt
- SpiritualTradition
- .isPermanent()
- SilenceClock
- DashboardSection.kt
- NewSessionViewModel.kt
- .of()
- .extract()
- DataModule.kt
- SessionMappersTest.kt
- CompanionPersonality
- SessionStats
- .select()
- ModelRefusal
- .extract()
- .byId()
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

## Communities (140 total, 72 thin omitted)

### Community 0 - "ModelRankingTest"
Cohesion: 0.06
Nodes (8): OpenRouterCatalog, ModelRanking, OpenRouterModel, UnusableModels, HttpOpenRouterCatalog, PrefsUnusableModels, NetworkModule, ModelRankingTest

### Community 1 - "GraphExportTest"
Cohesion: 0.05
Nodes (7): GraphExport, ExportedFile, share(), GraphFormat, GRAPHML, JSON, GraphExportTest

### Community 2 - "CloudChatServiceTest.kt"
Cohesion: 0.06
Nodes (7): TtsRequest, TtsService, TtsServiceException, CloudTtsService, TtsRequestTest, HttpOpenRouterCatalogTest, CloudTtsServiceTest

### Community 3 - "RoomStatsRepositoryTest"
Cohesion: 0.06
Nodes (6): MemoryVectorStore, ScoredId, MemoryVectorStoreTest, ExportFilesTest, MigrationTest, RoomStatsRepositoryTest

### Community 4 - "NarrativeViewModelTest"
Cohesion: 0.07
Nodes (12): NarrativeDocument, NarrativeStore, NarrativeDao, NarrativeEntity, RoomNarrativeStore, NarrativeViewModelTest, FailingChatService, FakeLocalTtsService (+4 more)

### Community 5 - "VoiceAction"
Cohesion: 0.07
Nodes (22): VoiceAction, ArmSilence, CancelSilence, Ended, Failed, Send, Speak, StartListening (+14 more)

### Community 6 - "ChatScreenTest"
Cohesion: 0.06
Nodes (12): ChatScreen(), ChatScreenTest, FakeCatalog, FakeChatService, FakeEmbeddingModelDownloader, FakeIntakeStore, FakeLocalLLMService, FakeLocalTtsService (+4 more)

### Community 7 - "CloudChatServiceTest"
Cohesion: 0.08
Nodes (10): PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel, CloudChatServiceTest, PinViewModelTest (+2 more)

### Community 8 - "ContinuousSpeechRecognizer"
Cohesion: 0.06
Nodes (5): SpeechSource, AndroidSttService, RecognitionListener, ContinuousSpeechRecognizer, RecognitionListener

### Community 9 - "LocalModel"
Cohesion: 0.07
Nodes (7): LocalModel, ModelDownloader, FakeModelDownloader, DownloadedModelDownloader, FakeModelDownloader, FakeModelDownloader, FakeModelDownloader

### Community 10 - "DeviceVoiceRankingTest"
Cohesion: 0.10
Nodes (9): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD, AndroidTtsService, UtteranceProgressListener (+1 more)

### Community 11 - "OnboardingViewModel"
Cohesion: 0.10
Nodes (12): OnboardingStep, ABOUT_YOU, API_KEY, CONCERNS, DISCLAIMER, GOALS, HISTORY, LOCAL_MODEL (+4 more)

### Community 12 - "EmbeddingModelSpec"
Cohesion: 0.09
Nodes (6): EmbeddingModelCatalog, EmbeddingModelSpec, EmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 14 - "SettingsScreenTest.kt"
Cohesion: 0.10
Nodes (11): AboutSection(), DeviceVoicePicker(), DownloadActionButton(), DownloadProgressAndError(), OpenRouterModelRow(), OpenRouterModelSection(), providerLabel(), SettingsScreen() (+3 more)

### Community 15 - "ChatViewModel.kt"
Cohesion: 0.09
Nodes (4): InsightExtractor, GGUFModelCatalog, PersonaHolder, MainApplication

### Community 17 - "CrisisResourcesTest"
Cohesion: 0.12
Nodes (5): CrisisResources, Resource, SafetyGuardrails, CrisisResourcesTest, SafetyGuardrailsTest

### Community 18 - "GraphNode"
Cohesion: 0.10
Nodes (7): GraphEdge, GraphNode, toDomain(), toEntity(), RoomGraphRepository, GraphViewModel, InsightsUiState

### Community 19 - "FakeSecureSettings"
Cohesion: 0.13
Nodes (5): FakeSecureSettings, SettingsViewModelTest, FakeLocalTtsService, FakeOpenRouterCatalog, LocalTtsService

### Community 20 - "ActiveSessionHolder"
Cohesion: 0.19
Nodes (6): ActiveSessionHolder, SessionsScreen(), SessionsViewModel, SessionsScreenTest, FakeSessionRepository, FakeStatsRepository

### Community 21 - "GraphRepository"
Cohesion: 0.10
Nodes (7): GraphRepository, GraphSnapshot, GraphHolderTest, FakeEmbeddingProvider, RecordingGraphRepository, FakeGraphRepository, FakeGraphRepository

### Community 22 - "PinScreen.kt"
Cohesion: 0.10
Nodes (4): AppRoot(), NumberPad(), PinKey(), PinScreen()

### Community 23 - "WordPieceTokenizer"
Cohesion: 0.11
Nodes (4): TokenizedInput, TokenizerConfig, WordPieceTokenizer, WordPieceTokenizerTest

### Community 24 - "NarrativePdfWriter.kt"
Cohesion: 0.12
Nodes (10): Style, BODY, HEADING, SUBTITLE, TITLE, Cursor, NarrativePdfWriter, PdfPageTarget (+2 more)

### Community 25 - "MessageAnalyzerTest"
Cohesion: 0.15
Nodes (7): MessageAnalyzer, EdgeSpec, Extraction, Kind, NodeSpec, Relation, MessageAnalyzerTest

### Community 28 - "ChatViewModelTest"
Cohesion: 0.15
Nodes (5): ManualSilenceClock, ChatViewModelTest, ChunkedChatService, FailingChatService, MissingKeyChatService

### Community 29 - "Provider"
Cohesion: 0.14
Nodes (7): ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings, EncryptedSecureSettings

### Community 30 - "SessionRepository"
Cohesion: 0.10
Nodes (5): Session, SessionRepository, SessionSummary, toSessionEntity(), RoomSessionRepository

### Community 31 - "ModelSettings"
Cohesion: 0.13
Nodes (5): ModelSettings, LocalTtsService, VoiceCatalog, ModelSettingsTest, SpyingLocalTtsService

### Community 33 - "DreamRepository"
Cohesion: 0.09
Nodes (7): Dream, DreamRepository, DreamSymbols, joinToList(), RoomDreamRepository, FakeDreamRepository, FakeDreamRepository

### Community 34 - "Test"
Cohesion: 0.19
Nodes (5): NarrativePrompt, NarrativeSource, NarrativeSources, NarrativePromptTest, NarrativeSourcesTest

### Community 35 - "Message"
Cohesion: 0.13
Nodes (3): ChatServiceException, MissingApiKeyException, Message

### Community 36 - "ChatService"
Cohesion: 0.10
Nodes (10): ChatService, ChatService, ChatService, ChatService, ChatService, FakeCatalog, FakeProviderCatalog, FakeUnusable (+2 more)

### Community 37 - "TherapyGraph"
Cohesion: 0.17
Nodes (3): TherapyGraph, TherapyGraphTest, TherapyGraphUpsertTest

### Community 38 - "SessionsScreen.kt"
Cohesion: 0.15
Nodes (7): ExportOption, DreamRow(), formatDate(), JournalScreen(), NoteRow(), formatDate(), NarrativeScreen()

### Community 39 - "DownloadStatus"
Cohesion: 0.11
Nodes (7): DownloadProgress, DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING

### Community 40 - "Note"
Cohesion: 0.14
Nodes (10): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, splitList() (+2 more)

### Community 41 - "SettingsScreen.kt"
Cohesion: 0.13
Nodes (4): prettifyEnumName(), SelectionChips(), Field(), NewSessionScreen()

### Community 42 - "FakeSessionRepository"
Cohesion: 0.10
Nodes (3): SessionsViewModelTest, FakeSessionRepository, FakeStatsRepository

### Community 44 - "Intake"
Cohesion: 0.16
Nodes (6): Intake, IntakeContext, IntakeStore, AppRootViewModel, IntakeContextTest, FakeIntakeStore

### Community 46 - "Tally"
Cohesion: 0.16
Nodes (5): MessageTally, StatsRepository, Tally, RoomStatsRepository, FailingStatsRepository

### Community 50 - "ChatScreen.kt"
Cohesion: 0.13
Nodes (10): VoicePhase, IDLE, LISTENING, SPEAKING, THINKING, MessageBubble(), ModalityPicker(), ModelBar() (+2 more)

### Community 51 - "Block"
Cohesion: 0.23
Nodes (3): Block, NarrativeTypesetTest, RecordingTarget

### Community 52 - "PinLockout"
Cohesion: 0.15
Nodes (8): LockoutStore, PinAttempt, Incorrect, LockedOut, Success, PinLockout, PinService, PinStore

### Community 54 - "GraphDao"
Cohesion: 0.18
Nodes (3): GraphDao, GraphEdgeEntity, GraphNodeEntity

### Community 56 - ".paginate()"
Cohesion: 0.25
Nodes (5): NarrativeExport, NarrativeFormat, MARKDOWN, PDF, NarrativeExportTest

### Community 57 - "NarrativeViewModel.kt"
Cohesion: 0.16
Nodes (3): LocalLLMService, NarrativeUiState, NarrativeViewModel

### Community 58 - "PairedDownloadTest"
Cohesion: 0.25
Nodes (3): PairedDownload, Half, PairedDownloadTest

### Community 60 - "GraphHolder"
Cohesion: 0.20
Nodes (3): EmbeddingProvider, EmbeddingProviderFactory, GraphHolder

### Community 63 - "PersonaKind"
Cohesion: 0.13
Nodes (11): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, PersonaKind, COMPANION, SPIRITUAL (+3 more)

### Community 66 - "TherapyModality"
Cohesion: 0.12
Nodes (14): TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL, PURPOSE (+6 more)

### Community 67 - "EncryptedPinStore.kt"
Cohesion: 0.15
Nodes (3): EncryptedIntakeStore, EncryptedPinStore, PrefsLockoutStore

### Community 70 - "GraphScreen.kt"
Cohesion: 0.18
Nodes (7): colorFor(), GraphCanvas(), GraphScreen(), kindLabel(), NodeRow(), Section(), timesLabel()

### Community 72 - "FakeChatService"
Cohesion: 0.17
Nodes (3): FakeChatService, FakeIntakeStore, ProgrammableLocalLLMService

### Community 74 - "ModelChoice"
Cohesion: 0.21
Nodes (3): ModelChoice, ProviderCatalog, HttpProviderCatalog

### Community 75 - "NoteEntity"
Cohesion: 0.21
Nodes (3): SessionCountRow, NoteDao, NoteEntity

### Community 76 - "MessageEntity"
Cohesion: 0.21
Nodes (4): MessageCountRow, MessageDao, ModalityCountRow, MessageEntity

### Community 80 - "PinLockoutTest"
Cohesion: 0.33
Nodes (3): PinLockoutTest, Clock, FakeLockoutStore

### Community 81 - "FakeSpeechSource"
Cohesion: 0.15
Nodes (3): FakeSpeechSource, FakeLocalTtsService, LocalTtsService

### Community 85 - "Persona"
Cohesion: 0.33
Nodes (3): Persona, TherapyPromptBuilder, TherapyPromptBuilderTest

### Community 90 - "Role"
Cohesion: 0.23
Nodes (10): AnthropicProtocol, ApiError, ChatRequest, ReqMessage, StreamDelta, StreamEvent, Role, ASSISTANT (+2 more)

### Community 91 - "ChatProtocol"
Cohesion: 0.34
Nodes (12): ChatProtocol, ApiError, ChatRequest, ErrorEnvelope, ErrorMetadata, ReqMessage, StreamChoice, StreamChunk (+4 more)

### Community 92 - "TherapyInsights"
Cohesion: 0.22
Nodes (3): CycleDetector, Cycles, TherapyInsights

### Community 93 - "OnboardingScreen.kt"
Cohesion: 0.35
Nodes (9): AboutYouStep(), ApiKeyStep(), DisclaimerStep(), GoalsStep(), IntakeStep(), LocalModelStep(), OnboardingScreen(), OnDeviceOnlyNote() (+1 more)

### Community 94 - "DashboardViewModel.kt"
Cohesion: 0.31
Nodes (4): Dashboard, GlobalStats, DashboardUiState, DashboardViewModel

### Community 101 - "SpiritualTradition"
Cohesion: 0.18
Nodes (10): SpiritualTradition, BUDDHIST, CHRISTIAN, HINDU, INTERFAITH, ISLAMIC, JEWISH, SECULAR (+2 more)

### Community 104 - "DashboardSection.kt"
Cohesion: 0.38
Nodes (6): Count(), Counts(), DashboardSection(), Modalities(), plural(), Themes()

### Community 110 - "CompanionPersonality"
Cohesion: 0.25
Nodes (7): CompanionPersonality, BOLD, CALM, CHEERFUL, DEEP, PLAYFUL, WARM

### Community 111 - "SessionStats"
Cohesion: 0.46
Nodes (7): SessionStats, ActiveList(), ArchiveList(), formatTimestamp(), plural(), SessionBadges(), SessionRow()

## Knowledge Gaps
- **88 isolated node(s):** `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH`, `STOIC` (+83 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 477 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **72 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Message` connect `Message` to `GraphExportTest`, `CloudChatServiceTest.kt`, `RoomStatsRepositoryTest`, `NarrativeViewModelTest`, `ChatScreenTest`, `CloudChatServiceTest`, `ChatViewModel.kt`, `ChatViewModel`, `GraphNode`, `SessionRepository`, `FakeSessionRepository`, `ChatScreen.kt`, `NarrativeViewModel.kt`, `PersonaKind`, `.buildRequest()`, `TherapyModality`, `ChatScreenTest.kt`, `FakeChatService`, `.buildVm()`, `ChatViewModelTest.kt`, `ChatProtocolTest`, `Persona`, `InMemorySessionRepository`, `Role`, `ChatProtocol`, `CloudChatService`, `LlamaCppLocalService`, `RoomSessionRepositoryTest.kt`, `SessionMappersTest.kt`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **What connects `THERAPIST`, `COMPANION`, `SPIRITUAL` to the rest of the system?**
  _88 weakly-connected nodes found - possible missing edges._
- **Should `ModelRankingTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05669050051072523 - nodes in this community are weakly interconnected._
- **Why does `GraphNode` connect `GraphNode` to `.detect()`, `GraphExportTest`, `TherapyModality`, `RoomGraphRepositoryTest.kt`, `TherapyGraph`, `GraphScreen.kt`, `RoomStatsRepositoryTest`, `ChatScreenTest.kt`, `DashboardTest`, `SessionMappersTest.kt`, `GraphHolder`, `.generate()`, `ChatViewModelTest.kt`, `GraphRepository`, `SessionRepository`, `assertequals`, `TherapyInsights`, `DashboardViewModel.kt`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **Should `GraphExportTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05444596443228454 - nodes in this community are weakly interconnected._
- **Why does `ChatViewModel` connect `ChatViewModel` to `ModelRankingTest`, `CloudChatServiceTest.kt`, `VoiceAction`, `ChatScreenTest`, `ContinuousSpeechRecognizer`, `LocalModel`, `ChatViewModel.kt`, `CrisisResourcesTest`, `ActiveSessionHolder`, `ChatViewModelTest`, `Provider`, `SessionRepository`, `ModelSettings`, `ChatService`, `Intake`, `Test`, `NarrativeViewModel.kt`, `GraphHolder`, `TherapyModality`, `.buildVm()`, `ModelChoice`, `Persona`, `SilenceClock`, `ModelRefusal`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Should `CloudChatServiceTest.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.056338028169014086 - nodes in this community are weakly interconnected._