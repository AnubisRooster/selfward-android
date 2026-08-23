# MEMORY.md — therAIpist (Android)

Project memory: architecture decisions and status. Keep this in sync with README.

## Stack
- Kotlin 2.0.21, Jetpack Compose (official `org.jetbrains.kotlin.plugin.compose`),
  Hilt 2.51.1, Room 2.6.1, Ktor 2.3.12, ONNX Runtime 1.18.0, security-crypto 1.1.0-alpha06.
- compileSdk 34; `core-ktx` pinned to 1.13.1 (llama lib pom otherwise pulls 1.17.0 → needs 35).
- On-device LLM: `org.codeshipping:llama-kotlin-android:0.1.7` (Kotlin-2.0 compiled, minSdk 24).
- On-device embeddings: ONNX Runtime (`onnxruntime-android:1.18.0`).

## Architecture
- `core/` — pure, testable, engine-free logic (model, safety, prompt, chat, modality,
  graph, local, embedding, voice, PersonaHolder).
- `data/` — Android bindings: Room DAOs/entities, llama + ONNX impls, cloud/local
  voice & chat services, `SecureSettings` (EncryptedSharedPreferences).
- `ui/` — Compose screens + Hilt ViewModels (`ChatViewModel`, `PersonaViewModel`,
  `SettingsViewModel`). `PersonaHolder` is the single source of the active persona.
- DI: `NetworkModule` (HttpClient, ApiConfig from SecureSettings), `DataModule`
  (DB, repositories, LocalLLMService, voice services, ChatService, core object
  providers, SecureSettings).

## Testing
- JVM unit tests (`Dispatchers.setMain` for ViewModels). CI runs `assembleDebug`,
  `lintDebug`, `testDebugUnitTest`. Building locally works fine with JDK 17 + the
  Android SDK (API 34) installed via `sdkmanager`; prefer verifying locally before
  pushing rather than using CI as the first check.
- **Use Ktor `MockEngine` for anything network-facing.** An earlier note here said
  to avoid it and keep HTTP logic engine-free. That was wrong, and it was
  expensive: it meant every service test substituted a fake at the interface
  boundary, so nothing exercised the real Ktor call. A streaming implementation
  that never streamed (`client.post()` buffers the whole body) shipped green, and
  two voice services returned HTTP error bodies as audio and as transcripts, all
  invisible to the suite. `CloudChatServiceTest` and `CloudTtsServiceTest` now
  drive a real client.
- A regression test must be able to fail. When fixing a bug, confirm the new test
  fails against the old code before landing it — several tests here pass against
  both versions and only one actually pins the behaviour.
- Robolectric used for Room tests (`RoomSessionRepositoryTest`) and Compose screen
  tests. Anything touching Keystore, TTS, or DownloadManager sits behind a `core`
  interface so tests can substitute fakes.

## Status
- Feature-complete for a first release; not yet published. 159 JVM tests green.
- **Not verified on physical hardware.** Streaming smoothness, embedding-download
  progress, and cloud TTS all need a device pass.
- `ModalityRouter.promptKey()` reaches 8 of the 15 framework prompts. `cbt`,
  `act`, `psychodynamic`, `somatic`, `narrative`, `ifs`, and `free_form` remain
  unreachable scaffolding; docs must not advertise them.
  `ModalityRouterTest.reachableFrameworksAreTheDocumentedEight` pins the set, so
  changing it is deliberate and forces the docs to be updated with it.
- Remaining: release keystore + AAB, Play store assets (screenshots need a
  device), privacy-policy hosting.

## Conventions
- iOS `TherapyConfig`/personas are source of truth; Android `TherapyConfig.kt` must
  stay in sync. Do NOT run `xcodegen generate` on iOS (breaks linking).
- TDD: write tests before implementation, and verify locally before pushing.
- Docs make claims about a therapy app's safety and privacy, so they get checked
  against the code, not against previous docs. Conversations live in a plain Room
  database protected by the app sandbox and device FDE — the app adds no
  encryption layer of its own, and nothing should say otherwise unless SQLCipher
  is actually added.
