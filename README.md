# therAIpist (Android)

A private AI companion for self-reflection — multiple therapeutic framings, a
memory that carries themes across sessions, and the option to run entirely
on-device with no network at all.

This is the Android fork of [therAIpist](https://github.com/AnubisRooster/therAIpist)
(the iOS original). Both share the same therapy "brain": prompts, personas, and
safety patterns live in `app/src/main/java/com/theraipist/config/TherapyConfig.kt`
and are kept in sync with the iOS `TransferModels.swift` / `PersonaService.swift`.

---

## ⚠️ This is not therapy

therAIpist is **not** a licensed therapist, psychologist, or medical provider, and
it is not a substitute for professional mental-health care. It is a journaling and
self-reflection tool.

**If you are in crisis, contact emergency services or a crisis line now:**

- **988** — Suicide & Crisis Lifeline (US, call or text)
- **741741** — Crisis Text Line (text HOME)
- **911** — Emergency services

The app can surface these resources, but it cannot contact anyone on your behalf.

---

## Safety and guardrails

Safety behaviour is deliberate and testable rather than left to the model.

**Crisis detection.** Every message you send is checked against two tiers of
language patterns before it goes anywhere. Explicit self-harm and suicidal
phrasing raises a `critical` level; hopelessness and worthlessness phrasing raises
a `warning`. Either surfaces a crisis-resource banner alongside the conversation.
Detection runs locally on your device, on your text, before any network call.

**Boundary interception.** The model is not permitted to diagnose or prescribe.
Replies are matched against a list of forbidden phrasings (`i diagnose you`,
`i prescribe`, `you need medication`, `start taking`, …). A match replaces the
reply with a message redirecting you to a licensed professional.

This check runs on **every partial chunk as the reply streams in**, not only on
the finished text — so a violating sentence is never rendered even briefly, and
cannot be left on screen if the connection drops mid-reply.

**Re-entry check.** If your previous session contained crisis language, the next
session opens with a gentle check-in rather than picking up as though nothing
happened.

**Honest limits.** These are pattern matches, not clinical judgement. They catch
common phrasings and will miss indirect or metaphorical expressions of distress.
They are a safety net, not a monitoring system, and nothing about them is a
substitute for a human being.

---

## Privacy and data handling

**Nothing is collected.** No analytics SDK, no advertising, no telemetry, no
account. Nobody but you sees your sessions unless you choose a cloud provider.

**Where your conversations live.** Sessions, messages, insights, and the memory
graph are stored in a local SQLite database (Room) in the app's private storage.
That storage is sandboxed from other apps by Android and covered by device-level
full-disk encryption on any modern device. The app does **not** add its own
encryption layer on top of the database — if that distinction matters to your
threat model, treat the database as protected by the device, not by the app.

**Your API keys** are stored separately, in `EncryptedSharedPreferences` backed by
the Android Keystore. They are sent only to the provider you chose.

**When data leaves your device.** Only if you use a cloud provider. In that case
your messages — including whatever you shared — are sent to OpenAI, OpenRouter, or
Anthropic to generate a reply, and are then subject to that provider's privacy
policy, not this one. Optional cloud text-to-speech sends the assistant's reply
text to the same provider.

**When it doesn't.** Select an on-device model and nothing leaves the phone:
inference, embeddings, and speech-to-text all run locally. Speech-to-text is
on-device only in all configurations — audio is never uploaded.

**Your control.** Delete any session from within the app. Remove your API key to
stop cloud processing entirely. Uninstalling removes all local data.

---

## Features

**Conversation modes.** Eight modes shape how the app responds — Talk, Journal,
Dream, Active Imagination, Roleplay, Grounding, Identity, and Audio. A mode is
picked automatically from what you write, and you can override it per message.

Each mode maps onto a therapeutic framing: Dream and Active Imagination use a
Jungian frame, Grounding uses DBT, Identity uses existential, Roleplay uses
Gestalt, Journal uses humanistic, and Talk/Audio use an integrated frame.

> `TherapyConfig` defines fifteen framework prompts, but `ModalityRouter`
> currently reaches only the six above. `cbt`, `act`, `psychodynamic`, `somatic`,
> `narrative`, `ifs`, `adlerian`, `free_form`, and `active_imagination` are
> present in config but unreachable — the Active Imagination mode uses the
> Jungian prompt rather than its own. Treat the other nine as scaffolding, not
> shipped behaviour.

**Streaming replies.** Replies arrive token by token as they are generated, from
both cloud providers and the on-device model, rather than appearing all at once.

**Memory that persists.** Insights are extracted from replies and accumulated
into a knowledge graph linking themes across sessions, rendered as an interactive
node-and-edge view. When the on-device embedding model is present, insights are
also indexed for semantic recall.

**Personas.** Choose a therapist, companion, or spiritual guide, and — for the
spiritual persona — one of nine traditions: Interfaith, Stoic, Buddhist,
Christian, Jewish/Kabbalistic, Islamic/Sufi, Hindu/Vedantic, Taoist, or Secular
Humanism.

**Voice.** Speak your input via on-device speech recognition, and have replies
read aloud using either the device's own text-to-speech or a cloud voice.

**Markdown replies**, session history, and a settings screen for provider, model,
and on-device choices.

---

## On-device vs cloud

|                        | On-device                            | Cloud                                  |
| ---------------------- | ------------------------------------ | -------------------------------------- |
| Where inference runs   | Your phone (llama.cpp)               | OpenAI / OpenRouter / Anthropic        |
| Data leaving device    | None                                 | Your messages go to the provider        |
| Needs an API key       | No                                   | Yes                                     |
| Works offline          | Yes                                  | No                                      |
| Quality                | Lower — small quantised models        | Higher                                  |
| Cost                   | Free, after the download             | Per provider pricing                    |

**On-device models** (GGUF, downloaded on demand, SHA-256 verified): TinyLlama
1.1B (0.7 GB), Qwen2.5 1.5B (1.1 GB), Llama 3.2 3B (2.0 GB), Phi-3.5-mini
(2.4 GB). Downloads run through the OS
download manager, so they survive backgrounding and process death, and a model is
only usable once its checksum matches.

**On-device embeddings**: MiniLM L6 v2 (quantised, ~23 MB) via ONNX Runtime
Mobile, also downloaded on demand and checksum-verified. Without it the memory
graph still records themes; it just doesn't do semantic recall.

If a local model fails to load or produces nothing, the app falls back to your
configured cloud provider rather than failing the message.

---

## Requirements

- Android 8.0 (API 26) or newer; targets API 34
- An API key for OpenAI, OpenRouter, or Anthropic — **or** an on-device model
- Storage for on-device models: 0.7–2.4 GB each, plus ~23 MB for embeddings

**Permissions**: `INTERNET` (cloud inference and voice), `RECORD_AUDIO`
(speech-to-text), `POST_NOTIFICATIONS` (download progress and reminders).

---

## Build and run

Requires JDK 17 and the Android SDK (API 34).

```bash
git clone https://github.com/AnubisRooster/therAIpist-android.git
cd therAIpist-android
./gradlew assembleDebug
```

Open the project in Android Studio and run it on a device or emulator. Then open
**Settings** in the app to add an API key, or enable an on-device model.

---

## Architecture

Layered so the therapy logic stays independent of Android and stays testable:

- **`core/`** — pure Kotlin, no Android dependencies. Safety guardrails, prompt
  building, modality routing, chat and voice protocol shapes, the knowledge graph
  and insight extraction, embeddings arithmetic, and repository interfaces.
- **`data/`** — Android- and network-backed implementations: Room persistence,
  Ktor cloud services, llama.cpp and ONNX Runtime integrations, OS download
  manager, Keystore-backed settings.
- **`ui/`** — Jetpack Compose screens and ViewModels (Chat, Sessions, Persona,
  Graph, Settings, About).
- **`config/TherapyConfig.kt`** — the portable therapy content shared with iOS.

Anything that touches an OS service or the network sits behind a `core` interface,
so tests substitute fakes instead of driving real Keystore, TTS, or download APIs.

**Stack**: Kotlin, Jetpack Compose (Material 3), Hilt, Room, Coroutines/Flow, Ktor
(with SSE streaming), llama.cpp via `llama-kotlin-android`, ONNX Runtime Mobile,
AndroidX Security.

---

## Testing

```bash
./gradlew testDebugUnitTest
```

CI builds, lints, and runs the full suite on every push.

Coverage concentrates where bugs are silent rather than loud: safety guardrails,
prompt assembly, SSE parsing and streaming behaviour, download status and progress
arithmetic, mean pooling, and the chat/settings ViewModels. Network-facing
services are exercised through a real Ktor `MockEngine` rather than a stubbed
interface — a fake at the interface boundary hides exactly the bugs that matter
there.

Compose screens are tested under Robolectric. Native inference and OS download
behaviour are compile-verified by CI but need a real device to exercise fully.

---

## Status

Feature-complete for a first release and not yet published. The build is green and
the suite passes, but the app has **not** been verified end-to-end on physical
hardware — streaming smoothness, download progress, and cloud voice in particular
should be checked on a device before any release.

## License

All rights reserved unless a license is added.
