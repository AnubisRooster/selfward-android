# therAIpist (Android)

A private, on-device Android therapy companion — named personas, hands-free voice,
knowledge graph, and 15 therapeutic modalities. No data leaves your phone (when
running on-device).

This is the **Android (Google Play) fork** of
[therAIpist](https://github.com/AnubisRooster/therAIpist) (the iOS original).
The two repos share the same therapy "brain" — prompts, personas, and safety
patterns live in `app/src/main/java/com/theraipist/config/TherapyConfig.kt` and
should be kept in sync with the iOS `TransferModels.swift` / `PersonaService.swift`.

> **Important disclaimer**
> therAIpist is **not** a licensed therapist, psychologist, or medical provider.
> It is a journaling and self-reflection tool only. If you are in crisis, call or
> text 988 (US) or your local emergency line.

---

## Status

- **Phase 0 — scaffold**: Gradle/Compose project, CI, and the portable therapy
  config (`TherapyConfig.kt`) are in place. The app currently shows a placeholder
  screen listing the 15 modalities.
- **Phase 2 — Room persistence** (done): `sessions`, `messages`, `insights`,
  `graph_nodes`, `graph_edges` entities + DAOs, `TherAIpistDatabase`, domain↔entity
  mappers, and `RoomSessionRepository` implementing the now-`suspend`
  `SessionRepository`. Verified with `SessionMappersTest` (pure) and
  `RoomSessionRepositoryTest` (Robolectric + in-memory Room).
- **Phase 3 — domain mechanics** (done): `TherapyModality` + `ModalityRouter`,
  `SseParser` (OpenAI-compatible streaming), `TherapyGraph` (in-memory knowledge
  graph) + `InsightExtractor`, and `SafetyGuardrails.reEntryCheck`.
- **Phase 4 — on-device LLM + embeddings**: `LocalLLMService` interface +
  `LlamaCppLocalService` (llama.cpp via `org.codeshipping:llama-kotlin-android`,
  Kotlin 2.0.21), `GGUFModelCatalog` + `ModelSelector`, `EmbeddingProvider` interface
  + `OnnxEmbeddingProvider` (ONNX Runtime Mobile), and `MemoryVectorStore` (cosine
  retrieval). Kotlin bumped to 2.0.21 + Compose migrated to the official
  `org.jetbrains.kotlin.plugin.compose` plugin to consume the 2.0-compiled library.
  Pure logic verified with `GGUFModelCatalogTest`, `ModelSelectorTest`,
  `CosineSimilarityTest`, `MemoryVectorStoreTest`.
- **Phase 5 — TTS/STT**: `TtsRequest` (engine-free JSON body, tested with
  `encodeDefaults` + `response_format` wire key), `TtsService`/`SttService`
  interfaces, `CloudTtsService` (OpenAI `/audio/speech`, Ktor) + `CloudSttService`
  (Whisper `/audio/transcriptions`, multipart), and on-device `AndroidTtsService`
  (`TextToSpeech`) / `AndroidSttService` (`SpeechRecognizer`). Verified with
  `TtsRequestTest`.
- **Phase 6 — Hilt DI**: `NetworkModule` (`HttpClient`, `ApiConfig`) + `DataModule`
  (database, `RoomSessionRepository`, `LlamaCppLocalService`, `CloudTtsService`,
  `CloudSttService`, `ChatService`, core `object` providers) in `SingletonComponent`.
  Validated by Hilt's kapt step in `assembleDebug`.
- **Phase 7 — UI**: `ChatViewModel` + `PersonaViewModel` (Hilt), `MainScreen` NavHost w/
  bottom nav (Chat/Persona/Graph), `ChatScreen`, `PersonaScreen`, `GraphScreen`
  (placeholder). `PersonaHolder` holds the active persona. Logic verified by
  `ChatViewModelTest`: send flow, blank-input guard, crisis resource banner. (62 tests green.)
- **Phase 8 — Provider config + secure keys**: `SecureSettings`
  (`EncryptedSharedPreferences`, `@Singleton`), `SettingsViewModel` + `SettingsScreen`
  (provider/API-key/model), and `ApiConfig` derived from `SecureSettings` (per-provider
  base URL). `MainScreen` gains a Settings tab.
- **Phase 9 — About screen**: `AboutScreen` (disclaimer + `TherapyConfig.RESOURCE_MESSAGE`
  crisis resources) added as a 5th nav tab.
- **Phase 10/11 — Docs**: `PRIVACY.md`, `MEMORY.md` (architecture/status), and
  `store/listing.md` (Play store listing + data-safety summary).
- **Phase 14 — Memory graph wiring**: `GraphHolder` (`@Singleton`) accumulates
  `InsightExtractor` output; `ChatViewModel` adds insights after each assistant reply
  and exposes `graphNodes`; `GraphScreen` shows live nodes via `GraphViewModel`.
  Verified by `assistantReply_extractsInsightsIntoGraph`.
- **Phase 15 — CI hardening**: ktlint (`org.jlleitschuh.gradle.ktlint` 12.1.1) added
  with `ignoreFailures = true`; CI runs `ktlintCheck` as a non-blocking (continue-on-error)
  step. Will tighten to fail the build once style is cleaned up.
- **Phase 3 — Voice input (STT)**: `AndroidSttService` now takes a `Context` and uses it;
  `ChatScreen` has a "Speak" button that requests `RECORD_AUDIO` (activity-compose
  `RequestPermission`) and fills the input with the transcript via `SpeechRecognizer`.
- **Phase 4 — On-device model selection**: `SecureSettings` stores `useLocalModel`/
  `localModelId` (lazy prefs); `ModelSettings` (`@Singleton`) exposes them as StateFlows;
  `SettingsScreen` lists `GGUFModelCatalog.allModels` with a toggle + radio selection;
  `ChatViewModel` routes to `LocalLLMService.generate(...)` when enabled (falls back to
  cloud if the model isn't loaded).
- **Phase 12 — Voice in chat (TTS)**: `ChatViewModel` gains `ttsEnabled` toggle; when
  on, assistant replies are synthesized via `TtsService` and played with `MediaPlayer`
  from a temp file. `ChatScreen` has a Read-aloud toggle. (STT input is a follow-up
  requiring RECORD_AUDIO permission handling.)
- **Phase 13 — Graph visualization**: `GraphHolder` now chains insights with edges;
  `GraphScreen` renders nodes + edges on a Compose `Canvas` (circular layout).

### Phase 1 — domain core + cloud chat, TDD (done)

Pure-Kotlin, platform-independent core under `app/src/main/java/com/theraipist/core/`,
with JVM unit tests under `app/src/test/java/com/theraipist/core/`:

- `model` — `Message`, `Role`, `Persona`.
- `safety/SafetyGuardrails` — crisis + boundary detection, re-entry check.
- `prompt/TherapyPromptBuilder` — persona-aware system prompts, modality
  instructions, and conversation assembly.
- `chat` — `ChatService` interface + `CloudChatService` (Ktor OpenAI-compatible
  `/chat/completions`, bearer auth, JSON) and engine-free `ChatProtocol` +
  `SseParser` for streaming.
- `modality` — `TherapyModality` enum + `ModalityRouter`.
- `graph` — `TherapyGraph` (in-memory knowledge graph) + `InsightExtractor`.
- `repository` — `SessionRepository` interface + `InMemorySessionRepository`.

Tests: `SafetyGuardrailsTest`, `TherapyPromptBuilderTest`, `ChatProtocolTest`,
`InMemorySessionRepositoryTest`, `ModalityRouterTest`, `SseParserTest`,
`TherapyGraphTest`, `InsightExtractorTest` — **all green in CI**
(`testDebugUnitTest`).

> Note: `CloudChatService` keeps request-building and response-parsing in the
> engine-free `ChatProtocol` object so they can be unit-tested without a Ktor
> engine (the `MockEngine` `NoClassDefFoundError` made engine-based tests
> flaky under the unit-test classpath).

## Build

Requires Android SDK (API 34) and JDK 17.

```bash
git clone https://github.com/AnubisRooster/therAIpist-android.git
cd therAIpist-android
./gradlew assembleDebug
```

Open in Android Studio (Giraffe+) and run on a device or emulator. The CI workflow
(`.github/workflows/ci.yml`) builds, lints, and runs unit tests on every push.

## Tech stack

- Kotlin + Jetpack Compose (Material 3), Hilt, Room, Coroutines/Flow
- Cloud LLM via Ktor (OpenRouter / Anthropic / OpenAI REST, SSE streaming)
- On-device LLM: `llama.cpp` (GGUF — same catalog as iOS)
- On-device embeddings: ONNX Runtime Mobile (MiniLM) — replaces Apple `NLEmbedding`
- TTS: Android `TextToSpeech` + OpenAI/ElevenLabs REST; STT: `SpeechRecognizer`
- Secure keys: AndroidX Security (`EncryptedSharedPreferences`)
- Graph viz: WebView + bundled Cytoscape.js (reused from the iOS app)

## License

All-rights-reserved unless a license is added.
