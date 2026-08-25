# Google Play Store Listing — Selfward

## App name (Play limit: 30 characters)
`Selfward: Private AI Therapy` — 28 characters.

The previous title, `therAIpist — AI Therapy Companion`, was 33 and would have
been rejected. The name carries no keywords of its own, so the descriptor after
the colon is what the store has to rank on; keep it in any future edit.

## Short description (≤ 80 chars)
AI therapy, journaling and dreamwork that runs on your phone. No account.

## Long description
Selfward is a private AI companion for self-reflection, drawing on Jungian,
Adlerian, DBT, Gestalt, existential, and humanistic framings. It helps you
explore your inner life through nine conversation modes — talk, journaling,
dreamwork, active imagination, role-play, grounding, identity, purpose, and
audio.

Features:
- Multiple therapy modalities that adapt to what you share.
- Persistent memory: Selfward remembers themes across sessions via a local
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

Selfward is not a substitute for professional mental-health care. In a crisis,
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

## Assets

All in `store/assets/`, all carrying the Selfward name and the muted violet the
app's theme actually uses (`#5F4B8B` to `#4A3269`, taken from the launcher icon
so the artwork and the first screen match):

- `play-icon-512.png` — 512×512 store icon. The mark is a glyph rather than a
  wordmark, so it survived the rename unchanged.
- `feature-graphic-1024x500.png` — Play feature graphic. Leads with the claim
  rather than the name: in a search result nobody knows the name yet, but they
  do already distrust therapy apps with their data.
- `site-banner-1600x500.png` — header for the marketing site. **Mark only, no
  wordmark** — Google Sites draws the site title over this image, so a banner
  containing "Selfward" would show the name twice.
- `banner-1600x500.png` — the same treatment *with* the wordmark, for anywhere
  nothing else supplies the name: README header, social preview cards.
- `screenshots/*.png` — recaptured against the renamed build. Recapture again
  after any further UI work, since onboarding shows the app name in its heading.

Regenerating the artwork: the sources are HTML+SVG rendered through headless
Chrome at exact pixel sizes, which is what gets Lato rendering correctly rather
than falling back to a system face. All three use the real launcher geometry —
two stroked crescents and a filled dot, lifted from `ic_launcher_foreground.xml`.

Still needed:
- **Privacy policy URL** — https://sites.google.com/view/selfward/privacy.
  Paste it into the Play Console at submission.

The app icon is an adaptive icon (`ic_launcher_foreground` / `_background` /
`_monochrome`); the monochrome layer supports Android 13+ themed icons.

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
   keytool -genkeypair -v -keystore selfward-release.jks \
     -alias selfward -keyalg RSA -keysize 2048 -validity 10000
   ```

   `keytool` will prompt for a store password, a key password, and your
   distinguished-name details (org, name, etc. — used only in the certificate
   metadata, not shown to users).

2. **Build a signed release locally** by exporting four env vars before running
   Gradle (the keystore path is the local file, not committed anywhere):

   ```bash
   export RELEASE_KEYSTORE_PATH=/path/to/selfward-release.jks
   export RELEASE_KEYSTORE_PASSWORD=<store password>
   export RELEASE_KEY_ALIAS=selfward
   export RELEASE_KEY_PASSWORD=<key password>
   ./gradlew bundleRelease   # -> app/build/outputs/bundle/release/app-release.aab
   ```

3. **Enable it in CI** by adding four repository secrets (Settings → Secrets and
   variables → Actions → New repository secret) — once `RELEASE_KEYSTORE_BASE64`
   exists, the `release` job in `ci.yml` starts running automatically on pushes
   to `main`, builds a signed `.aab`, and uploads it as a workflow artifact:

   ```bash
   base64 -i selfward-release.jks | gh secret set RELEASE_KEYSTORE_BASE64
   gh secret set RELEASE_KEYSTORE_PASSWORD
   gh secret set RELEASE_KEY_ALIAS
   gh secret set RELEASE_KEY_PASSWORD
   ```

4. **Play Console**: create the app listing there, upload the first `.aab`
   manually to establish the app, then subsequent CI-built `.aab`s can be
   uploaded to a testing/production track (manually, or later via
   `gradle-play-publisher` / `fastlane supply` with a service-account key —
   not set up here, since it needs its own Play Console service account).
