# Google Play Store Listing — therAIpist

## App name
therAIpist — AI Therapy Companion

## Short description (≤ 80 chars)
A private, modality-aware AI therapy companion for reflection and growth.

## Long description
therAIpist is a private AI therapy companion grounded in Jungian, Adlerian, and
DBT traditions. It helps you explore your inner life through multiple therapeutic
modalities — talk therapy, journaling, dreamwork, active imagination, role-play,
grounding, and identity exploration.

Features:
- Multiple therapy modalities that adapt to what you share.
- Persistent memory: therAIpist remembers themes across sessions via a local
  knowledge graph and extracted insights (stored on-device).
- Crisis-aware: detects distress and surfaces supportive resources (e.g. 988).
- Privacy-first: conversations are stored locally and encrypted; API keys never
  leave your device except to the provider you choose.
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
ai therapy, jungian, adlerian, dbt, journaling, dreamwork, mental health, self
reflection, mindfulness, private, on-device, offline, chatbot

## Content rating
Target: Everyone (or "Mature 17+" if crisis/suicide content is flagged). Provide
a content-rating questionnaire answer noting therapeutic crisis-support messaging.

## Required permissions (and justification)
- INTERNET: cloud model inference and optional cloud TTS/STT.
- RECORD_AUDIO: speech-to-text input.
- POST_NOTIFICATIONS: gentle session reminders (Android 13+).

## Data safety form summary
- Data collected: none by default. If cloud provider used, text sent only to that
  provider per its policy.
- Data encrypted in transit (HTTPS) and at rest (Android EncryptedSharedPreferences).
- No data shared with third parties beyond the user-selected AI provider.
- No ads, no analytics.

## Assets still needed
- Feature graphic (1024×500), icon (512×512), at least 2 phone screenshots
  (Chat, Persona, Graph, Settings, About).
- Privacy policy URL (host PRIVACY.md or a static page).
