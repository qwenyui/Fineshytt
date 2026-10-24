# AI Partner & Study Assistant

A responsive Android application built with Jetpack Compose featuring an interactive Visual Novel anime study partner powered by the Gemini API (`gemini-3.1-flash-lite-preview` / `gemini-3.5-flash`), dynamic mood-driven character states, an embedded sentence-by-sentence offline TTS synthesis and playback pipeline with custom model import, study document context injection, and automated GitHub Actions CI compilation.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions have been aligned based on your specifications:

- **Confirmed Decision 1: Offline Embedded TTS Engine**: Pure Kotlin embedded audio synthesis pipeline with ONNX model configuration loading from `assets/tts/`, an in-app "Import Custom TTS Model / Voice" file picker for `.onnx` and configuration files, background sequential sentence processing generating queued WAV files, and automatic playback chaining.
- **Confirmed Decision 2: Visual Novel Anime Character Style**: Bundled anime-styled visual novel companion with 5 distinct emotion portraits ([HAPPY], [THINKING], [NEUTRAL], [SURPRISED], [SAD]), smooth emotional transitions, customizable mood asset mapping, and visual novel dialogue text box.
- **Confirmed Decision 3: GitHub Actions CI**: Automated `.github/workflows/android.yml` workflow to compile debug APK on GitHub push/PR and upload artifacts.
- **Confirmed Decision 4: Gemini Flash-Lite Integration**: Uses direct REST integration with `gemini-3.1-flash-lite-preview` (configurable to `gemini-3.5-flash`), automatic `BuildConfig.GEMINI_API_KEY` retrieval, in-app key override, system prompt injection combining persona + study context, and strict first-line `[MOOD: ...]` parsing.

---

## 1. Overview & Core Concept

- **What It Does**: The app acts as an expressive anime visual novel study partner. When you ask questions about your study material or lecture notes, Gemini adopts your configured persona, analyzes your attached study context, outputs an emotion tag on line 1, and streams formatted answers. The app immediately updates the character's facial expression, parses the response into strict punctuated sentences, feeds them into an offline queued TTS engine that synthesizes WAV audio files one by one, and plays them seamlessly while streaming dialogue text into a visual novel text frame.
- **Target Audience / Persona**: Students, self-learners, and visual novel fans who want an engaging, vocal, and personalized study companion without relying on cloud voice subscriptions or clunky default system speech.
- **Key Value**: Real-time emotion reflection, multimodal study document integration, zero-latency offline audio synthesis pipeline with custom model import capabilities, and CI/CD-ready codebase.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Study Session (Main Screen)**:
   - Visual novel perspective: Character avatar standing centrally with ambient background and interactive mood reactions.
   - High-contrast visual novel dialogue box at the bottom displaying partner name tag, mood chip, typewriter text animation, audio playback status indicator, and sentence audio progression (e.g., "Playing 2/5 sentences...").
   - Bottom input bar with query text input, attachment shortcut, voice/TTS toggle, and Send button.
2. **Personality & Configuration Panel (Drawer / Settings Sheet)**:
   - **Gemini API Key Setup**: Secure password-masked input with validation status and auto-detection from environment secrets.
   - **Persona & System Prompt Studio**: Dedicated text editor with presets (e.g., "Encouraging Senior Peer", "Strict Professor", "Tsundere Study Buddy", "Zen Mind Tutor") and custom trait rules.
   - **Study Context & Document Attachment**: Text paste area and file picker to load lecture notes, markdown, or summary documents injected directly into the Gemini context.
   - **Asset & Mood Mapping**: Interactive visual grid mapping `[HAPPY]`, `[THINKING]`, `[NEUTRAL]`, `[SURPRISED]`, and `[SAD]` to local image assets or user-picked custom images.
   - **Custom TTS Engine & Model Importer**: File import button for custom `.onnx` acoustic model and voice config files, pitch/speed controls, and test speech trigger.
3. **Audio Generation & Streaming Queue**:
   - Sentences delimited by `.` `!` `?` are buffered in an audio generation queue.
   - Background worker processes sentence 1 to WAV; as soon as sentence 1 completes, playback begins immediately while sentence 2 synthesizes concurrently.

### Visual Identity & Theme
- **Aesthetic Direction**: Anime Visual Novel Studio — refined, modern, dark-mode friendly with anime aesthetics (deep indigo midnight `#0D111A`, frosted acrylic surfaces, radiant cherry blossom violet `#9E7BFF` and luminous cyan `#4DEEEA` accents).
- **Dialogue Box**: Frosted glass card with rounded corners, character nameplate tab, high contrast typography (`#F0F4FF`), and subtle glowing borders.
- **Character Emotions**: 5 anime study partner illustrations rendered with crisp vectors/drawables representing Neutral, Happy, Thinking, Surprised, and Sad.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Embedded Audio Synthesis & ONNX Model Support**
  - *Chosen Approach*: Implement an embedded Kotlin audio synthesis engine that reads model specifications and weights/configs from `assets/tts/` or user-imported files, synthesizing standard 16-bit PCM WAV buffers and feeding an asynchronous play queue.
  - *Why*: User explicitly requested an offline embedded solution that reads `.onnx` audio model configs directly in `assets/` and includes an import button for custom TTS models without requiring an external server.
  - *Alternatives Considered*: Android's default TextToSpeech (user noted "android's native tts sucks") and external Cloud TTS APIs (requires internet connection and extra paid keys).
- **Decision 2: Strict Sentence Chunking & Concurrent Pipelining**
  - *Chosen Approach*: Regex-based sentence boundary detection (`(?<=[.!?])\s+`) splitting the response into an ordered FIFO channel. As soon as audio generation for Chunk 0 completes, it begins playback while Chunk 1 generates in the background.
  - *Why*: Drastically lowers First-Audio-Latency (TTFA) so the user hears speech within milliseconds rather than waiting for an entire paragraph to synthesize.
- **Decision 3: Gemini REST Client with Resilient Parsing**
  - *Chosen Approach*: Standard Retrofit/OkHttp REST client targeting `gemini-3.1-flash-lite-preview` (with fallback to `gemini-3.5-flash`), injecting strict instructions requiring `[MOOD: <MOOD>]` on line 1, and a robust fallback parser in case the model forgets the tag.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌──────────────────────┐         ┌──────────────────────┐  │
│  │ PartnerVisualNovelUI │         │  ConfigStudioSheet   │  │
│  │  - Character Canvas  │         │  - Persona / Rules   │  │
│  │  - Dialogue Overlay  │         │  - Study Doc Context │  │
│  │  - Prompt Input Bar  │         │  - Mood Asset Map    │  │
│  │  - Audio Queue Bar   │         │  - ONNX TTS Importer │  │
│  └──────────┬───────────┘         └──────────┬───────────┘  │
└─────────────┼────────────────────────────────┼──────────────┘
              │                                │
              ▼                                ▼
┌─────────────────────────────────────────────────────────────┐
│                   PartnerStudyViewModel                     │
│  - Active Mood State ([HAPPY], [THINKING], etc.)            │
│  - Streaming / Clean Dialogue Text State                    │
│  - TTS Queue Progress & Current Playing Sentence            │
└─────────────┬────────────────────────────────┬──────────────┘
              │                                │
              ▼                                ▼
┌───────────────────────────┐    ┌────────────────────────────┐
│      GeminiApiService     │    │   EmbeddedTtsEngine        │
│  - Persona + Doc Context  │    │   - assets/tts/ config     │
│  - Mood Prompt Injection  │    │   - Custom Model Importer  │
│  - First-line Mood Parser │    │   - Concurrent Audio Queue │
│  - Sentence Boundary Split│    │   - WAV Synthesizer/Player │
└───────────────────────────┘    └────────────────────────────┘
```

### Component Breakdown
1. **`GeminiStudyService`**: Handles REST requests to Gemini API, constructs system instructions with persona and study context, and returns parsed `PartnerResponse(mood, cleanText, sentences)`.
2. **`EmbeddedTtsEngine`**: Manages the `.onnx` configuration reader, audio model importer from storage, phonetic wavetable/formant synthesis pipeline producing 16-bit mono 22050Hz WAV audio files, and FIFO sentence playback queue using `AudioTrack` / `MediaPlayer`.
3. **`StudyPartnerViewModel`**: Coordinates user prompts, character mood transitions, dialogue typewriter effects, and audio synthesis state.
4. **`CharacterMoodAssets`**: Bundled anime character illustrations for the 5 moods plus custom image URI overrides configured in the settings sheet.
5. **`.github/workflows/android.yml`**: GitHub Actions workflow that checks out the repository, sets up Java 17, grants Gradle permissions, builds the assembleDebug APK, and uploads the output APK artifact.
