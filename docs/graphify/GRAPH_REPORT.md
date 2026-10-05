# Graph Report - selfward-android  (2026-10-05)

## Corpus Check
- Large corpus: 252 files · ~605,577 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder.

## Summary
- 2917 nodes · 7771 edges · 139 communities (70 shown, 69 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 171 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- VoiceConversationTest
- ChatViewModel
- ChatScreenTest
- LocalModel
- RoomStatsRepositoryTest
- ChatViewModelTest
- SessionRepository
- ContinuousSpeechRecognizer
- GraphEdge
- ModelRankingTest
- NarrativeScreen.kt
- DeviceVoiceRankingTest
- ActiveSessionHolder
- EmbeddingModelSpec
- SettingsScreenTest.kt
- CrisisResourcesTest
- ChatViewModel.kt
- GraphNode
- Test
- .generate()
- SelfwardDatabase
- WordPieceTokenizer
- GraphExportTest
- MessageAnalyzerTest
- Provider
- NarrativePdfWriter.kt
- SettingsViewModelTest
- Message
- DreamRepository
- NarrativeViewModelTest
- PriceTiersTest
- SettingsScreen.kt
- Note
- FakeSecureSettings
- Theme.kt
- DashboardTest
- AndroidEmbeddingModelDownloader
- CloudChatServiceTest
- .viewModel()
- Tally
- Test
- Test
- FakeSpeechSource
- Block
- PinLockout
- NarrativeViewModel.kt
- com
- SpiritualTradition
- .detect()
- GraphDao
- SettingsViewModel
- NewSessionViewModelTest
- GraphHolder
- PairedDownloadTest
- FakeUnusable
- JournalViewModelTest
- PersonaKind
- .pool()
- Persona
- GraphScreen.kt
- OnboardingViewModel
- ChatScreen.kt
- .buildRequest()
- TherapyModality
- TtsRequest
- DreamDao
- SessionEntity
- OpenRouterCatalog
- UnusableModels
- DashboardViewModel.kt
- .paginate()
- GraphViewModel.kt
- NoteEntity
- MessageEntity
- NewSessionScreen.kt
- JournalViewModel
- NarrativePdfWriterTest.kt
- ModelChoice
- ProviderDefaultsAdoptionTest
- MemoryVectorStore
- Intake
- asstateflow
- PinLockoutTest
- HttpOpenRouterCatalogTest
- FakeStatsRepository
- PinScreen.kt
- Test
- ChatProtocolTest
- IntakeStore
- SelfwardDatabase.kt
- AndroidModelDownloader
- OnboardingViewModelTest
- ExportFiles
- CompanionPersonality
- CloudChatService
- ChatProtocol
- NarrativeDocument
- NarrativeEntity
- OnboardingScreen.kt
- DashboardSection.kt
- launch
- DataModule.kt
- LlamaCppLocalService
- RoomGraphRepositoryTest.kt
- RoomSessionRepositoryTest.kt
- Test
- LocalLLMService
- .of()
- .extract()
- Role
- HttpOpenRouterCatalog
- CloudTtsService
- AnthropicProtocol
- .select()
- EncryptedPinStore.kt
- OnboardingStep
- SessionMappersTest.kt
- FakeSessionRepository
- SessionStats
- .extract()
- .byId()
- Inject
- TtsServiceException
- FakeLocalTtsService
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

## Communities (139 total, 69 thin omitted)

### Community 0 - "VoiceConversationTest"
Cohesion: 0.06
Nodes (26): ArmSilence, CancelSilence, Ended, Failed, Final, Partial, RecognizerEnded, RecognizerFailed (+18 more)

### Community 1 - "ChatViewModel"
Cohesion: 0.06
Nodes (5): ModelRefusal, Result, UtteranceProgressListener, ChatViewModel, ModelRefusalTest

### Community 2 - "ChatScreenTest"
Cohesion: 0.06
Nodes (10): ChatScreenTest, FakeCatalog, FakeChatService, FakeEmbeddingModelDownloader, FakeIntakeStore, FakeLocalLLMService, FakeLocalTtsService, FakeProviderCatalog (+2 more)

### Community 3 - "LocalModel"
Cohesion: 0.07
Nodes (13): LocalModel, DownloadStatus, DOWNLOADED, DOWNLOADING, FAILED, NOT_DOWNLOADED, VERIFYING, ModelDownloader (+5 more)

### Community 4 - "RoomStatsRepositoryTest"
Cohesion: 0.08
Nodes (3): ExportFilesTest, MigrationTest, RoomStatsRepositoryTest

### Community 5 - "ChatViewModelTest"
Cohesion: 0.11
Nodes (8): ChatViewModelTest, ChunkedChatService, FailingChatService, FakeChatService, FakeTtsService, MissingKeyChatService, ObservingChatService, ProgrammableLocalLLMService

### Community 6 - "SessionRepository"
Cohesion: 0.05
Nodes (7): InMemorySessionRepository, Session, SessionRepository, SessionSummary, RoomSessionRepository, FakeSessionRepository, FakeSessionRepository

### Community 7 - "ContinuousSpeechRecognizer"
Cohesion: 0.06
Nodes (5): SpeechSource, AndroidSttService, RecognitionListener, ContinuousSpeechRecognizer, RecognitionListener

### Community 8 - "GraphEdge"
Cohesion: 0.08
Nodes (9): GraphEdge, GraphRepository, GraphSnapshot, RoomGraphRepository, FakeEmbeddingProvider, GraphHolderTest, RecordingGraphRepository, FakeGraphRepository (+1 more)

### Community 9 - "ModelRankingTest"
Cohesion: 0.14
Nodes (3): ModelRanking, OpenRouterModel, ModelRankingTest

### Community 10 - "NarrativeScreen.kt"
Cohesion: 0.08
Nodes (5): ExportOption, MainScreen(), Tab, formatDate(), NarrativeScreen()

### Community 11 - "DeviceVoiceRankingTest"
Cohesion: 0.10
Nodes (9): DeviceVoice, DeviceVoiceRanking, VoiceTier, ENHANCED, PREMIUM, STANDARD, LocalTtsService, AndroidTtsService (+1 more)

### Community 12 - "ActiveSessionHolder"
Cohesion: 0.16
Nodes (6): ActiveSessionHolder, SessionsScreen(), SessionsViewModel, FakeSessionRepository, FakeStatsRepository, SessionsScreenTest

### Community 13 - "EmbeddingModelSpec"
Cohesion: 0.10
Nodes (6): EmbeddingModelCatalog, EmbeddingModelSpec, EmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader, FakeEmbeddingModelDownloader

### Community 14 - "SettingsScreenTest.kt"
Cohesion: 0.10
Nodes (10): AboutSection(), DeviceVoicePicker(), DownloadActionButton(), DownloadProgressAndError(), OpenRouterModelRow(), OpenRouterModelSection(), SettingsScreen(), FakeLocalTtsService (+2 more)

### Community 15 - "CrisisResourcesTest"
Cohesion: 0.12
Nodes (5): CrisisResources, Resource, SafetyGuardrails, CrisisResourcesTest, SafetyGuardrailsTest

### Community 17 - "GraphNode"
Cohesion: 0.13
Nodes (4): GraphNode, TherapyGraph, TherapyGraphTest, TherapyGraphUpsertTest

### Community 18 - "Test"
Cohesion: 0.12
Nodes (3): VoiceCatalog, VoiceTranscript, VoiceTranscriptTest

### Community 21 - "WordPieceTokenizer"
Cohesion: 0.11
Nodes (4): TokenizedInput, TokenizerConfig, WordPieceTokenizer, WordPieceTokenizerTest

### Community 23 - "MessageAnalyzerTest"
Cohesion: 0.15
Nodes (7): EdgeSpec, Extraction, Kind, MessageAnalyzer, NodeSpec, Relation, MessageAnalyzerTest

### Community 24 - "Provider"
Cohesion: 0.12
Nodes (8): ApiConfig, Provider, ANTHROPIC, OPENAI, OPENROUTER, SecureSettings, EncryptedSecureSettings, providerLabel()

### Community 25 - "NarrativePdfWriter.kt"
Cohesion: 0.12
Nodes (9): Style, BODY, HEADING, SUBTITLE, TITLE, Cursor, PageTarget, PdfPageTarget (+1 more)

### Community 27 - "SettingsViewModelTest"
Cohesion: 0.14
Nodes (4): FakeLocalTtsService, FakeOpenRouterCatalog, SettingsViewModelTest, LocalTtsService

### Community 28 - "Message"
Cohesion: 0.13
Nodes (4): ChatService, ChatServiceException, MissingApiKeyException, Message

### Community 29 - "DreamRepository"
Cohesion: 0.09
Nodes (7): Dream, DreamRepository, DreamSymbols, joinToList(), RoomDreamRepository, FakeDreamRepository, FakeDreamRepository

### Community 30 - "NarrativeViewModelTest"
Cohesion: 0.22
Nodes (4): FailingChatService, FakeNoteRepository, NarrativeViewModelTest, RecordingChatService

### Community 32 - "SettingsScreen.kt"
Cohesion: 0.14
Nodes (4): DreamRow(), formatDate(), JournalScreen(), NoteRow()

### Community 33 - "Note"
Cohesion: 0.14
Nodes (10): Note, NoteRepository, NoteType, JOURNAL, REFLECTION, SESSION_NOTE, RoomNoteRepository, splitList() (+2 more)

### Community 35 - "FakeSecureSettings"
Cohesion: 0.18
Nodes (4): ModelSettings, ModelSettingsTest, SpyingLocalTtsService, FakeSecureSettings

### Community 36 - "Theme.kt"
Cohesion: 0.11
Nodes (3): MainActivity, AppRoot(), SelfwardTheme()

### Community 40 - ".viewModel()"
Cohesion: 0.19
Nodes (3): FakeLockoutStore, FakePinStore, PinViewModelTest

### Community 41 - "Tally"
Cohesion: 0.16
Nodes (5): MessageTally, StatsRepository, Tally, RoomStatsRepository, FailingStatsRepository

### Community 43 - "Test"
Cohesion: 0.24
Nodes (4): NarrativeSource, NarrativeSources, NarrativePromptTest, NarrativeSourcesTest

### Community 44 - "FakeSpeechSource"
Cohesion: 0.11
Nodes (4): FakeSpeechSource, ManualSilenceClock, LocalTtsService, FakeLocalTtsService

### Community 45 - "Block"
Cohesion: 0.23
Nodes (3): Block, NarrativeTypesetTest, RecordingTarget

### Community 46 - "PinLockout"
Cohesion: 0.15
Nodes (8): Incorrect, LockedOut, LockoutStore, PinAttempt, PinLockout, Success, PinService, PinStore

### Community 47 - "NarrativeViewModel.kt"
Cohesion: 0.14
Nodes (7): NarrativePrompt, NarrativePdfWriter, NarrativeFormat, MARKDOWN, PDF, NarrativeUiState, NarrativeViewModel

### Community 50 - "com"
Cohesion: 0.13
Nodes (5): ChatService, FakeEmbeddingModelDownloader, FakeIntakeStore, FakeLocalLLMService, FakeProviderCatalog

### Community 51 - "SpiritualTradition"
Cohesion: 0.11
Nodes (17): CompanionGender, FEMININE, MASCULINE, NONBINARY, UNSPECIFIED, CrisisPattern, SpiritualTradition, BUDDHIST (+9 more)

### Community 52 - ".detect()"
Cohesion: 0.31
Nodes (3): CycleDetector, Cycles, CycleDetectorTest

### Community 53 - "GraphDao"
Cohesion: 0.18
Nodes (3): GraphDao, GraphEdgeEntity, GraphNodeEntity

### Community 56 - "GraphHolder"
Cohesion: 0.18
Nodes (3): EmbeddingProvider, EmbeddingProviderFactory, GraphHolder

### Community 57 - "PairedDownloadTest"
Cohesion: 0.25
Nodes (3): Half, PairedDownload, PairedDownloadTest

### Community 58 - "FakeUnusable"
Cohesion: 0.13
Nodes (6): ChatService, ChatService, ChatService, FakeCatalog, FakeUnusable, RefusesFirstModel

### Community 60 - "PersonaKind"
Cohesion: 0.15
Nodes (6): PersonaKind, COMPANION, SPIRITUAL, THERAPIST, NewSessionUiState, NewSessionViewModel

### Community 62 - "Persona"
Cohesion: 0.24
Nodes (4): Persona, PersonaHolder, TherapyPromptBuilder, TherapyPromptBuilderTest

### Community 63 - "GraphScreen.kt"
Cohesion: 0.17
Nodes (7): colorFor(), GraphCanvas(), GraphScreen(), kindLabel(), NodeRow(), Section(), timesLabel()

### Community 65 - "ChatScreen.kt"
Cohesion: 0.19
Nodes (6): ChatScreen(), MessageBubble(), ModalityPicker(), ModelBar(), ModelRow(), VoiceStatus()

### Community 67 - "TherapyModality"
Cohesion: 0.12
Nodes (14): TherapyModality, ACTIVE_IMAGINATION, AUDIO, DREAM, GROUNDING, IDENTITY, JOURNAL, PURPOSE (+6 more)

### Community 73 - "DashboardViewModel.kt"
Cohesion: 0.19
Nodes (3): Dashboard, DashboardUiState, DashboardViewModel

### Community 75 - "GraphViewModel.kt"
Cohesion: 0.18
Nodes (7): ExportedFile, share(), GraphFormat, GRAPHML, JSON, GraphViewModel, InsightsUiState

### Community 76 - "NoteEntity"
Cohesion: 0.21
Nodes (3): SessionCountRow, NoteDao, NoteEntity

### Community 77 - "MessageEntity"
Cohesion: 0.21
Nodes (4): MessageCountRow, MessageDao, ModalityCountRow, MessageEntity

### Community 78 - "NewSessionScreen.kt"
Cohesion: 0.19
Nodes (4): prettifyEnumName(), SelectionChips(), Field(), NewSessionScreen()

### Community 81 - "ModelChoice"
Cohesion: 0.23
Nodes (3): ModelChoice, ProviderCatalog, HttpProviderCatalog

### Community 83 - "MemoryVectorStore"
Cohesion: 0.19
Nodes (3): MemoryVectorStore, ScoredId, MemoryVectorStoreTest

### Community 84 - "Intake"
Cohesion: 0.26
Nodes (4): Intake, IntakeContext, IntakeContextTest, FakeIntakeStore

### Community 85 - "asstateflow"
Cohesion: 0.17
Nodes (6): PinMode, CONFIRM, SETUP, UNLOCK, PinUiState, PinViewModel

### Community 86 - "PinLockoutTest"
Cohesion: 0.33
Nodes (3): Clock, FakeLockoutStore, PinLockoutTest

### Community 89 - "PinScreen.kt"
Cohesion: 0.18
Nodes (3): NumberPad(), PinKey(), PinScreen()

### Community 92 - "IntakeStore"
Cohesion: 0.19
Nodes (3): IntakeStore, EncryptedIntakeStore, AppRootViewModel

### Community 97 - "CompanionPersonality"
Cohesion: 0.16
Nodes (10): CompanionPersonality, BOLD, CALM, CHEERFUL, DEEP, PLAYFUL, WARM, toDomain() (+2 more)

### Community 99 - "ChatProtocol"
Cohesion: 0.34
Nodes (12): ApiError, ChatProtocol, ChatRequest, ErrorEnvelope, ErrorMetadata, ReqMessage, StreamChoice, StreamChunk (+4 more)

### Community 100 - "NarrativeDocument"
Cohesion: 0.22
Nodes (4): NarrativeDocument, NarrativeStore, RoomNarrativeStore, FakeNarrativeStore

### Community 102 - "OnboardingScreen.kt"
Cohesion: 0.35
Nodes (9): AboutYouStep(), ApiKeyStep(), DisclaimerStep(), GoalsStep(), IntakeStep(), LocalModelStep(), OnboardingScreen(), OnDeviceOnlyNote() (+1 more)

### Community 105 - "DashboardSection.kt"
Cohesion: 0.38
Nodes (7): GlobalStats, Count(), Counts(), DashboardSection(), Modalities(), plural(), Themes()

### Community 116 - "Role"
Cohesion: 0.24
Nodes (5): Role, ASSISTANT, SYSTEM, USER, InMemorySessionRepositoryTest

### Community 119 - "AnthropicProtocol"
Cohesion: 0.44
Nodes (6): AnthropicProtocol, ApiError, ChatRequest, ReqMessage, StreamDelta, StreamEvent

### Community 122 - "OnboardingStep"
Cohesion: 0.22
Nodes (9): OnboardingStep, ABOUT_YOU, API_KEY, CONCERNS, DISCLAIMER, GOALS, HISTORY, LOCAL_MODEL (+1 more)

### Community 125 - "SessionStats"
Cohesion: 0.46
Nodes (7): SessionStats, ActiveList(), ArchiveList(), formatTimestamp(), plural(), SessionBadges(), SessionRow()

## Knowledge Gaps
- **91 isolated node(s):** `CrisisPattern`, `THERAPIST`, `COMPANION`, `SPIRITUAL`, `INTERFAITH` (+86 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 473 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **69 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Message` connect `Message` to `ChatViewModel`, `ChatScreenTest`, `RoomStatsRepositoryTest`, `ChatViewModelTest`, `SessionRepository`, `ChatViewModel.kt`, `NarrativeViewModelTest`, `HttpProviderCatalog.kt`, `CloudChatServiceTest`, `NarrativeViewModel.kt`, `CloudChatServiceTest.kt`, `ChatViewModelTest.kt`, `SpiritualTradition`, `Persona`, `ChatScreen.kt`, `.buildRequest()`, `TherapyModality`, `ChatProtocolTest`, `CompanionPersonality`, `CloudChatService`, `ChatProtocol`, `ChatScreenTest.kt`, `LlamaCppLocalService`, `RoomSessionRepositoryTest.kt`, `LocalLLMService`, `Role`, `AnthropicProtocol`, `SessionMappersTest.kt`, `FakeSessionRepository`?**
  _High betweenness centrality (0.091) - this node is a cross-community bridge._
- **What connects `CrisisPattern`, `THERAPIST`, `COMPANION` to the rest of the system?**
  _91 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `VoiceConversationTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05917721518987342 - nodes in this community are weakly interconnected._
- **Why does `GraphNode` connect `GraphNode` to `CompanionPersonality`, `TherapyModality`, `RoomStatsRepositoryTest`, `DashboardTest`, `ChatScreenTest.kt`, `GraphEdge`, `DashboardViewModel.kt`, `GraphViewModel.kt`, `RoomGraphRepositoryTest.kt`, `ChatViewModelTest.kt`, `GraphExportTest.kt`, `.generate()`, `.detect()`, `GraphExportTest`, `GraphHolder`, `assertequals`, `SessionMappersTest.kt`, `GraphScreen.kt`?**
  _High betweenness centrality (0.063) - this node is a cross-community bridge._
- **Should `ChatViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.057539682539682536 - nodes in this community are weakly interconnected._
- **Why does `ChatViewModel` connect `ChatViewModel` to `VoiceConversationTest`, `ChatScreenTest`, `LocalModel`, `ChatViewModelTest`, `SessionRepository`, `ContinuousSpeechRecognizer`, `ActiveSessionHolder`, `CrisisResourcesTest`, `ChatViewModel.kt`, `Provider`, `FakeSecureSettings`, `Test`, `GraphHolder`, `Persona`, `ChatScreen.kt`, `TherapyModality`, `OpenRouterCatalog`, `UnusableModels`, `ModelChoice`, `IntakeStore`, `launch`, `DataModule.kt`, `LocalLLMService`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Should `ChatScreenTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05701754385964912 - nodes in this community are weakly interconnected._