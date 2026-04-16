# TODOS

Captured during /plan-eng-review on 2026-04-15.
Branch: cwharris77/hum-design

## Pre-Implementation

### ~~Create DESIGN.md~~ DONE
Created by /design-consultation on 2026-04-15.

### Radial menu animation specs
Define detailed animation specs for the long-press radial menu: fan-out distance (40dp from circle center), spring animation (300ms, slight overshoot), edge-aware positioning (flip to available side near screen edges), backdrop dim (30% overlay). Tune spring physics during implementation.
- **Why:** The radial menu is Hum's signature interaction. Guessed animations feel wrong.
- **Depends on:** Circle visual language implementation.

## Phase 1 (Spike)

### Empty mix guard
When all circles are muted or deleted and the user taps share/export, `AudioMixer.mixTracks` receives an empty list. Add a guard that disables the share button when no circles are in `Ready` or `Soloed` state. Show "Nothing to export" if somehow reached.
- **Why:** Silent failure or crash on a common interaction path.
- **Depends on:** CircleState enum, AudioMixer implementation.

### iOS audio session category
Configure `AVAudioSession` category to `.playAndRecord` with `.defaultToSpeaker` and `.mixWithOthers` options in the iOS `actual AudioSession`. Without this, the beat stops playing when recording starts.
- **Why:** Required for the core experience to work. Standard iOS boilerplate, easy to forget in KMP.
- **Depends on:** AudioSession iOS actual implementation.

### Bluetooth audio detection
Check audio output route on recording start. If Bluetooth, show warning: "Bluetooth adds delay. Switch to wired headphones for best results." Both `AVAudioEngine` (iOS) and `AudioManager` (Android) expose the current output route.
- **Why:** Most phone users have BT headphones (100-300ms latency). Without warning, they'll think the app is broken.
- **Depends on:** DeviceCapabilities implementation.

## Phase 2 (MVP)

### Storage-full handling during export/compression
Check available disk space before export and WAV-to-M4A compression. Catch IO exceptions during write, clean up partial files on failure, show "Not enough space" with specific MB needed.
- **Why:** Storage-full is common on budget phones (32-64GB). A song with 8 rows of takes could exceed space during final mix. The pre-recording 100MB warning doesn't cover export.
- **Depends on:** AudioMixer implementation.

### SQLDelight schema migration strategy
Define SQLDelight migration versioning from v1. Add migration test that creates v1 data, runs migration to v2, verifies data integrity. Design v1 schema carefully since every future version migrates from it.
- **Why:** Schema migration failures on app update cause crashes and data loss. Losing creative work is the worst possible user experience.
- **Depends on:** Data model finalization.
