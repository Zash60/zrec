# Zrec

> Screen recorder for Android — built with Kotlin & Jetpack Compose.

[![CI](https://github.com/zash60/Zrec/actions/workflows/ci.yml/badge.svg)](https://github.com/zash60/Zrec/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Min SDK](https://img.shields.io/badge/minSdk-26-brightgreen.svg)]()
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg)]()

Zrec is a clean, terminal-inspired screen recording app for Android. It uses the MediaProjection API and MediaRecorder to capture your screen with optional audio from the microphone or internal audio source.

## Screenshots

<!-- TODO: Add actual screenshots -->

```
┌─────────────────────────────────┐
│ > Zrec                    [≡]   │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ $ recording ready           │ │
│ │                             │ │
│ │           ●                 │ │
│ │                             │ │
│ │     Audio: Microphone ▾     │ │
│ └─────────────────────────────┘ │
│                                 │
│ # Screen Recorder               │
│ Min SDK: API 26 (Android 8.0)   │
│ Engine: MediaProjection + MR    │
│ Output: Movies/Zrec/*.mp4       │
│ Codec: H.264 / AAC              │
│                                 │
│ → View Recordings               │
└─────────────────────────────────┘
```

## Features

- **Screen Recording** — Capture your screen using MediaProjection API
- **Audio Capture** — Microphone and/or internal audio recording
- **Pause & Resume** — Full control over recording sessions
- **Foreground Service** — Persistent notification with playback controls
- **Video Library** — Browse and play all recordings from Movies/Zrec
- **Dark Theme** — Terminal-inspired warm dark UI (light theme supported)
- **MVVM Architecture** — Clean architecture with ViewModel + StateFlow

## Tech Stack

| Area | Technology |
|------|------------|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + StateFlow |
| Screen Capture | MediaProjection API + MediaRecorder |
| Video Playback | ExoPlayer (Media3) |
| Navigation | Compose Navigation |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 (Android 15) |

## Build Instructions

### Prerequisites

- Android Studio Ladybug (2024.2) or later
- JDK 17
- Android SDK 35

### Build

```bash
# Clone the repository
git clone https://github.com/zash60/Zrec.git
cd Zrec

# Build debug APK
./gradlew assembleDebug

# Run lint checks
./gradlew lint

# Run unit tests
./gradlew test
```

The debug APK will be available at `app/build/outputs/apk/debug/app-debug.apk`.

### Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Permissions

Zrec requests the following permissions at runtime:

| Permission | Purpose |
|---|---|
| `RECORD_AUDIO` | Capture microphone audio |
| `FOREGROUND_SERVICE` | Keep recording service alive |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Screen capture in background |
| `FOREGROUND_SERVICE_MICROPHONE` | Microphone access in foreground service |
| `POST_NOTIFICATIONS` | Show recording notification (Android 13+) |
| `READ_MEDIA_VIDEO` | List recorded videos (Android 13+) |

## Architecture

```
app/src/main/java/com/zash60/zrec/
├── data/
│   ├── model/          # Data classes (VideoFile)
│   └── repository/     # VideoRepository (MediaStore queries)
├── service/
│   └── ScreenRecordingService.kt  # Foreground service
├── ui/
│   ├── theme/          # Color, Typography, Theme, Spacing
│   ├── components/     # Reusable Compose components
│   └── screens/        # Home, Recordings, Player screens
├── viewmodel/
│   └── RecordingViewModel.kt       # MVVM state management
└── util/
    └── PermissionHelper.kt         # Runtime permissions
```

## Design

The UI follows the design system defined in [DESIGN.md](DESIGN.md) — a warm near-black terminal aesthetic inspired by OpenCode, using monospace typography throughout, minimal 4px border radius, and Apple HIG semantic colors.

## CI/CD

GitHub Actions runs on every push and PR to `main` and `develop`:

- **Build**: `./gradlew assembleDebug`
- **Lint**: `./gradlew lint`
- **Test**: `./gradlew test`
- **Artifacts**: Debug APK uploaded as workflow artifact

See [.github/workflows/ci.yml](.github/workflows/ci.yml) for details.

## License

MIT License — see [LICENSE](LICENSE) for details.
