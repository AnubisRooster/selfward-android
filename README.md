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
- Phases 1–3 (data layer, domain services, on-device LLM, TTS/STT, full Compose
  UI, Play Store listing) are planned. See the iOS repo for the target feature set.

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
