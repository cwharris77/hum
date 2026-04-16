# Design System — Hum

## Product Context
- **What this is:** Mobile music production app for singers. Not a DAW. A looper pedal reimagined as a mobile app.
- **Who it's for:** Singers who want to record over beats on their phone without learning a studio.
- **Space/industry:** Music creation, mobile audio. Peers: GarageBand, BandLab, Soundtrap (all DAW-paradigm, Hum is deliberately not).
- **Project type:** Mobile app (Kotlin Multiplatform + Compose Multiplatform, iOS + Android)

## Aesthetic Direction
- **Direction:** Organic/Natural meets Playful. Warm dark mode.
- **Decoration level:** Intentional. Glow effects on active elements. Circles ARE the decoration.
- **Mood:** A cozy vocal booth at night. Warm, approachable, slightly playful. Not a nightclub, not a code editor. The feeling of humming into your phone and being surprised by what comes out.
- **Anti-references:** DAW timelines, mixer faders, waveform displays, studio chrome.

## Typography
- **Display/Hero:** Satoshi Bold — warm geometric sans, modern but not cold. Used for song title, onboarding text.
- **Body:** DM Sans Regular/Medium — clean, highly readable at small sizes. Used for row labels, menu items, status text.
- **Data/Timer:** Geist Mono Medium — tabular-nums for recording timer, take counts. Monospace keeps digits from jumping.
- **Loading:** Google Fonts (Satoshi via Fontsource, DM Sans, Geist Mono).
- **Scale:**
  - 12sp — circle labels, secondary metadata
  - 14sp — row labels, menu items, body text
  - 16sp — song title, primary labels
  - 20sp — recording timer (Geist Mono), onboarding text (Satoshi)
  - 24sp — reserved for future hero/display use

## Color
- **Approach:** Restrained with stem-color accents. Color is used semantically (per-stem identity, state communication), not decoratively.
- **Palette:**

| Token | Hex | Usage |
|-------|-----|-------|
| `--bg-primary` | #1A1A2E | Main background (deep warm charcoal) |
| `--bg-surface` | #25253E | Row backgrounds, cards |
| `--bg-elevated` | #2E2E4A | Menus, overlays, radial menu backdrop |
| `--text-primary` | #EFEFEF | Headings, labels, primary text |
| `--text-secondary` | #9B9BB0 | Hints, timestamps, secondary text |
| `--accent-record` | #FF3B5C | Record button, recording state ring |
| `--stem-blue` | #5B8DEF | Vocals stem circles |
| `--stem-green` | #7BC47F | Harmony stem circles |
| `--stem-amber` | #F0A050 | Percussion/FX stem circles |
| `--stem-coral` | #EF7B7B | Additional stem circles |
| `--stem-beat` | #8B8B9E | Beat row (pinned, de-emphasized) |
| `--success` | #7BC47F | Success states |
| `--warning` | #F0A050 | Warnings (storage, latency) |
| `--error` | #FF3B5C | Errors, destructive actions |

- **Dark mode:** Default and only mode for Phase 1-2. Light mode deferred.
- **Stem color cycling:** Rows beyond 4 cycle through blue → green → amber → coral.

## Spacing
- **Base unit:** 8dp
- **Density:** Comfortable (not cramped, not spacious — fits 5 circles per row)
- **Scale:** 2xs(2) xs(4) sm(8) md(16) lg(24) xl(32) 2xl(48) 3xl(64)
- **Key dimensions:**
  - Circle diameter: 56dp
  - Circle gap: 8dp
  - Row height: 80dp (label 14sp + 56dp circle + 8dp padding)
  - Record button: 72dp diameter
  - Screen edge padding: 16dp
  - Row label left padding: 16dp

## Layout
- **Approach:** Single-screen, organic (not grid-disciplined). Circles are the layout.
- **Structure (top to bottom):**
  1. Song title (top, minimal)
  2. Rows area (scrollable vertical, each row scrolls horizontal)
  3. Record button (pinned bottom, floating above rows)
  4. Share/settings (bottom edge, tertiary)
- **Record button:** Always visible. Pinned at bottom. Rows scroll underneath it.
- **Max circles visible per row:** 5 before horizontal scroll kicks in.
- **Max rows visible:** ~8 before vertical scroll.
- **Border radius:** Circles are fully round. Rows have sm(8dp) radius. Menus have md(16dp) radius.

## Circle Visual Language

Circles are the core UI element. Every state has a distinct visual treatment.

| State | Fill | Opacity | Ring | Size | Extra |
|-------|------|---------|------|------|-------|
| Ready | Stem color | 100% | None | 56dp | — |
| Playing | Stem color | 100% | Clockwise progress ring (3dp, stem color) | 56dp | Ring draws as audio plays |
| Recording | Stem color | 100% | Red ring, thickness fluctuates with audio level | 56dp | Level meter ring |
| Muted | Stem color | 40% | None | 48dp | Visually recedes |
| Soloed | Stem color | 100% | None | 56dp | Solo badge (small "S" indicator) |

## Interaction Patterns

- **Tap circle:** Play/stop that take
- **Long-press circle:** Radial menu fans out (mute, solo, delete, volume arc)
- **Tap row label:** Select row as recording target
- **Selected row:** Subtle glow on left edge in stem color
- **"+ New stem":** Below last row, tertiary visual weight
- **Share:** Bottom right, standard share icon
- **Playback:** Manual by default (tap circle to hear). Auto-play available as settings toggle.

### Radial Menu
On long-press, options fan out in a circle around the held circle:
- Fan-out distance: 40dp from circle center
- Animation: spring (300ms, slight overshoot)
- Edge-aware: flips to available side near screen edges
- Backdrop: 30% dim overlay on rest of screen
- Options: Mute, Solo, Delete, Volume (curved arc slider)

## Motion
- **Approach:** Intentional. Every animation communicates state, nothing is decorative.
- **Easing:** enter(ease-out) exit(ease-in) move(ease-in-out)
- **Duration:** micro(50-100ms) short(150-250ms) medium(250-400ms) long(400-700ms)

| Animation | Trigger | Spec |
|-----------|---------|------|
| Record pulse | Recording active | Scale 1.0→1.05, 1s ease-in-out cycle, continuous |
| Circle birth | Recording stops | Scale 1.0→1.1→1.0, 100ms, single bounce |
| Radial menu | Long-press | Spring fan-out, 300ms, slight overshoot |
| Playback ring | Circle playing | Linear clockwise, duration matches audio length |
| Level meter | Recording active | Real-time, no easing, direct audio level mapping |
| Row glow | Row selected | Fade in 150ms ease-out |

## Empty States

| Context | What the user sees |
|---------|-------------------|
| First launch | "Tap to start humming." + demo beat in beat row + record button |
| Empty row | "Tap record to add a take" (subtle, secondary text) |
| No beat (Phase 2) | "Import a beat" as tappable action in beat row |

## Accessibility
- All interactive elements have semantic labels (VoiceOver/TalkBack)
- Circle: "Take 3, lead vocal, ready, double-tap to play"
- Record button: "Record, double-tap to start recording"
- Muted circle: "Take 2, harmony, muted, double-tap to unmute"
- Minimum touch target: 56dp (circles), 48dp (buttons)
- States communicated through opacity + size, not color alone
- Recording timer announced periodically for screen reader users

## Decisions Log
| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-04-15 | Initial design system | Created by /design-consultation from /plan-design-review decisions |
| 2026-04-15 | Dark mode only (Phase 1-2) | Performers prefer dark screens. Light mode deferred. |
| 2026-04-15 | Radial menu over bottom sheet | Reinforces circle mental model. Signature interaction. |
| 2026-04-15 | Manual playback default | Respect user control. Auto-play as settings toggle. |
| 2026-04-15 | 56dp circles, 8dp spacing | 44dp+ touch target (Apple HIG), fits 5 per row on 393px screen |
| 2026-04-15 | Satoshi + DM Sans + Geist Mono | Warm but clean type stack. No Material 3 defaults. |
