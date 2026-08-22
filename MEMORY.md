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
- JVM unit tests (Turbine-free, `Dispatchers.setMain` for ViewModels). CI runs
  `assembleDebug`, `lintDebug`, `testDebugUnitTest`. No local Android SDK → rely on CI.
- Avoid Ktor `MockEngine` (NoClassDefFoundError). Keep HTTP/JSON logic engine-free.
- Robolectric used for Room tests (`RoomSessionRepositoryTest`).

## Status (as of Phase 9)
- Phases 0–8 complete & green (62 JVM tests). Phase 9 (About screen) added.
- Remaining: voice-in-chat integration, knowledge-graph visualization, insights
  persistence, CI hardening (detekt/ktlint), release/AAB build, Play store assets.

## Conventions
- iOS `TherapyConfig`/personas are source of truth; Android `TherapyConfig.kt` must
  stay in sync. Do NOT run `xcodegen generate` on iOS (breaks linking).
- TDD: write tests before implementation. Verify via CI (no local build).
