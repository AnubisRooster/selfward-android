# Privacy Policy — therAIpist

therAIpist is a private AI companion for self-reflection. This policy describes
exactly what happens to what you write, including the cases where it leaves your
device.

_Last updated: 23 August 2026._

## What we collect

**Nothing.** There is no account, no analytics SDK, no advertising, no telemetry,
and no crash reporting. The developer receives no data about you or your use of
the app, and cannot read your sessions.

## Where your conversations are stored

Your sessions, messages, extracted insights, and memory graph are stored in a
local database in the app's private storage on your device.

That storage is sandboxed from other apps by Android, and on any modern device it
is covered by the operating system's full-disk encryption, which is tied to your
device passcode. **The app does not apply its own separate encryption layer to
this database.** If your threat model includes someone with unlocked access to
your device, treat these conversations as readable.

Your **API keys** are handled differently: they are stored in Android's
`EncryptedSharedPreferences`, backed by the Android Keystore, separately from
conversation data.

## The PIN lock

You can set a six-digit PIN that is asked for each time the app starts. Repeated
wrong entries lock the keypad for progressively longer.

**The PIN is a privacy curtain, not encryption.** It keeps your sessions off the
screen if someone else picks up your unlocked phone. It does **not** encrypt the
database, and it will not stop someone who has your unlocked device and developer
tools from reading what is stored. Please don't rely on it as protection against
a determined person with access to your phone.

The PIN itself is held in Keystore-backed storage on the device, is never
transmitted, and is not included in backups.

## When your data leaves your device

**If you use a cloud provider**, the messages in your conversation — including
anything personal you have shared — are sent to the provider you selected in
Settings, in order to generate a reply:

- OpenAI
- OpenRouter
- Anthropic

Once sent, that data is handled under **that provider's** privacy policy and
retention practices, not this one. Please read the policy of whichever provider
you choose. Your API key is transmitted only to that same provider, to
authenticate the request.

**If you enable cloud text-to-speech**, the text of the assistant's replies is
sent to the same provider to be turned into audio.

**If you write or update your Narrative** while a cloud provider is selected,
that is a larger disclosure than a single message: your notes, dreams and past
conversations are sent to the provider together, in one batch, so it can weave
them into a single account. The app says so on the Narrative screen before you
press the button. Selecting an on-device model keeps the whole process on your
phone.

**If you use an on-device model**, nothing leaves your device. Inference and
embeddings both run locally, and the app works with no network connection.

**Speech-to-text is always on-device.** Your microphone audio is processed by
Android's own speech recognition and is never uploaded by this app in any
configuration.

## What we never do

We do not sell, rent, share, or monetise your data. We do not build profiles. We
do not use your conversations to train any model.

## Crisis handling

If the app detects language suggesting distress or self-harm, it displays
supportive resources — including the 988 Suicide & Crisis Lifeline, the Crisis
Text Line, and emergency services — and encourages you to contact a professional.

This detection runs locally on your device, on your own text, before any network
request. **It does not notify anyone, contact any service, or report anything on
your behalf.** No third party is ever alerted about the content of your messages.

Note also that this detection is pattern matching, not clinical judgement: it
catches common phrasings and will miss others. It is not a monitoring or
safeguarding system.

## Children

therAIpist is not directed at children and should not be used by anyone under 13.

## Your control

- **Delete any session** at any time from within the app.
- **Remove your API key** in Settings to stop all cloud processing.
- **Switch to an on-device model** to stop sending anything off the device.
- **Uninstall the app** to remove all locally stored data, including sessions,
  insights, keys, and any downloaded models.

## Not medical care

therAIpist is not a licensed therapist, psychologist, or medical provider, and is
not a substitute for professional mental-health care. In a crisis, contact
emergency services or a crisis line — in the US, call or text **988**.

## Contact

For privacy questions, contact the developer via the project repository.
