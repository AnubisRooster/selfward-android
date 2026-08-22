# Privacy Policy — therAIpist

therAIpist is a private AI therapy companion. Your wellbeing and privacy are central
to its design.

## What we collect
- **Nothing by default.** The app stores conversations only on your device in a local
  encrypted database (Android `EncryptedSharedPreferences` + Room).
- **API keys** you enter are stored only in **device-local encrypted storage** and are
  never transmitted to anyone except the AI provider you explicitly choose.
- If you use a **cloud provider** (OpenAI, OpenRouter, Anthropic), your messages are
  sent to that provider to generate responses, subject to that provider's privacy
  policy.

## On-device processing
therAIpist supports **fully on-device** inference via bundled GGUF models and
local embeddings (llama.cpp + ONNX Runtime). When using local models, no conversation
data leaves the device.

## Data we do not sell
We do not sell, share, or monetize your data. There is no analytics SDK and no
advertising in the app.

## Crisis handling
If the app detects crisis language, it surfaces supportive resources (e.g. 988) and
encourages contacting professionals or emergency services. It does not contact
anyone on your behalf.

## Your control
- Delete any session anytime from within the app (local storage).
- Remove your API key from Settings to stop cloud processing.
- Uninstalling the app removes all locally stored data.

## Contact
For privacy questions, contact the developer via the project repository.
