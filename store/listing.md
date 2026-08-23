# Google Play Store Listing — therAIpist

## App name
therAIpist — AI Therapy Companion

## Short description (≤ 80 chars)
A private, modality-aware AI therapy companion for reflection and growth.

## Long description
therAIpist is a private AI companion for self-reflection, drawing on Jungian,
Adlerian, DBT, Gestalt, existential, and humanistic framings. It helps you
explore your inner life through nine conversation modes — talk, journaling,
dreamwork, active imagination, role-play, grounding, identity, purpose, and
audio.

Features:
- Multiple therapy modalities that adapt to what you share.
- Persistent memory: therAIpist remembers themes across sessions via a local
  knowledge graph and extracted insights (stored on-device).
- Crisis-aware: detects distress and surfaces supportive resources (e.g. 988).
- Privacy-first: no account, no analytics, no ads. Conversations stay in the
  app's private storage on your device; API keys are held in Android Keystore-
  backed encrypted storage and are sent only to the provider you choose.
- On-device option: run GGUF language models and local embeddings entirely offline
  with llama.cpp + ONNX Runtime — no data leaves the phone.
- Voice: speak and listen with text-to-speech and speech-to-text.
- Configurable companion: choose persona, companion gender/personality, and
  spiritual tradition to tailor the experience.

therAIpist is not a substitute for professional mental-health care. In a crisis,
contact local emergency services or a crisis hotline.

## Category
Health & Fitness (Medical subcategory may require sensitive-content declaration).

## Tags / keywords
ai therapy, jungian, adlerian, dbt, gestalt, journaling, dreamwork, mental health, self
reflection, mindfulness, private, on-device, offline, chatbot

## Content rating
Target: Everyone (or "Mature 17+" if crisis/suicide content is flagged). Provide
a content-rating questionnaire answer noting therapeutic crisis-support messaging.

## Required permissions (and justification)
- INTERNET: cloud model inference and optional cloud TTS/STT.
- RECORD_AUDIO: speech-to-text input.
- POST_NOTIFICATIONS: gentle session reminders (Android 13+).

## Data safety form summary
- Data collected by the developer: none. No account, analytics, telemetry, or ads.
- If a cloud provider is selected, message text is sent to that provider (OpenAI,
  OpenRouter, or Anthropic) to generate a reply, per that provider's policy.
  Optional cloud TTS sends reply text to the same provider.
- Microphone audio is processed on-device only and is never uploaded.
- Data encrypted in transit: yes (HTTPS).
- Data encrypted at rest: **declare accurately.** API keys are in Android
  Keystore-backed EncryptedSharedPreferences, but the conversation database is a
  plain Room/SQLite file relying on Android's app sandbox and device-level full-
  disk encryption. Do not declare app-level at-rest encryption for conversations
  unless SQLCipher (or equivalent) is actually added.
- No data shared with third parties beyond the user-selected AI provider.

## Assets still needed
- Feature graphic (1024×500), icon (512×512), at least 2 phone screenshots
  (Chat, Persona, Graph, Settings, About).
- Privacy policy URL (host PRIVACY.md or a static page).

## Release signing setup (do this yourself — nobody else should ever hold this keystore)

`app/build.gradle.kts`'s `release` build type and `.github/workflows/ci.yml`'s
`release` job both read signing config from environment variables / repo secrets
and simply fall back to debug signing (locally) or skip entirely (in CI) until
these exist, so nothing here is required to keep building `debug` as normal.

1. **Generate the keystore once, and keep it somewhere durable and backed up**
   (a password manager's secure-notes/file storage, or an encrypted volume — not
   this repo, not a chat, not anywhere it could be casually lost). Losing this
   file or its passwords means you can never publish an update to the same Play
   Store listing again — Play requires every update to be signed with the same
   key.

   ```bash
   keytool -genkeypair -v -keystore theraipist-release.jks \
     -alias theraipist -keyalg RSA -keysize 2048 -validity 10000
   ```

   `keytool` will prompt for a store password, a key password, and your
   distinguished-name details (org, name, etc. — used only in the certificate
   metadata, not shown to users).

2. **Build a signed release locally** by exporting four env vars before running
   Gradle (the keystore path is the local file, not committed anywhere):

   ```bash
   export RELEASE_KEYSTORE_PATH=/path/to/theraipist-release.jks
   export RELEASE_KEYSTORE_PASSWORD=<store password>
   export RELEASE_KEY_ALIAS=theraipist
   export RELEASE_KEY_PASSWORD=<key password>
   ./gradlew bundleRelease   # -> app/build/outputs/bundle/release/app-release.aab
   ```

3. **Enable it in CI** by adding four repository secrets (Settings → Secrets and
   variables → Actions → New repository secret) — once `RELEASE_KEYSTORE_BASE64`
   exists, the `release` job in `ci.yml` starts running automatically on pushes
   to `main`, builds a signed `.aab`, and uploads it as a workflow artifact:

   ```bash
   base64 -i theraipist-release.jks | gh secret set RELEASE_KEYSTORE_BASE64
   gh secret set RELEASE_KEYSTORE_PASSWORD
   gh secret set RELEASE_KEY_ALIAS
   gh secret set RELEASE_KEY_PASSWORD
   ```

4. **Play Console**: create the app listing there, upload the first `.aab`
   manually to establish the app, then subsequent CI-built `.aab`s can be
   uploaded to a testing/production track (manually, or later via
   `gradle-play-publisher` / `fastlane supply` with a service-account key —
   not set up here, since it needs its own Play Console service account).
