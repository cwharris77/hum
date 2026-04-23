# Hum Weekend Spike Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Single-screen KMP app that plays a beat, records vocals over it with real-time monitoring, displays circles for each take, and plays them back — validating that low-latency audio feels good on a real phone.

**Architecture:** Compose Multiplatform for shared UI + business logic. Platform audio via expect/actual: AVAudioEngine (iOS), AudioRecord/AudioTrack (Android). In-memory state only (no persistence). Fake audio engine enables UI development without platform audio.

**Tech Stack:** Kotlin 2.2.20, Compose Multiplatform 1.10.3, AGP 8.7.3, Gradle 8.11.1

---

## File Structure

```
hum/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── libs.versions.toml
├── composeApp/
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/hum/app/
│       │   ├── App.kt
│       │   ├── SongState.kt
│       │   ├── model/
│       │   │   ├── Circle.kt
│       │   │   └── CircleState.kt
│       │   ├── audio/
│       │   │   ├── AudioEngine.kt
│       │   │   ├── FakeAudioEngine.kt
│       │   │   └── BeatGenerator.kt
│       │   └── ui/
│       │       ├── theme/
│       │       │   └── Theme.kt
│       │       ├── CircleView.kt
│       │       ├── StemRow.kt
│       │       ├── RecordButton.kt
│       │       └── CircleScreen.kt
│       ├── commonTest/kotlin/com/hum/app/
│       │   └── model/
│       │       └── CircleStateTest.kt
│       ├── androidMain/
│       │   ├── AndroidManifest.xml
│       │   ├── res/values/themes.xml
│       │   └── kotlin/com/hum/app/
│       │       ├── MainActivity.kt
│       │       ├── Platform.kt
│       │       └── audio/
│       │           └── AndroidAudioEngine.kt
│       └── iosMain/kotlin/com/hum/app/
│           ├── MainViewController.kt
│           ├── Platform.kt
│           └── audio/
│               └── IosAudioEngine.kt
└── iosApp/
    └── iosApp/
        ├── iOSApp.swift
        └── Info.plist
```

---

## Task 1: Project Scaffold

**Files:**
- Create: `gradle/libs.versions.toml`
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `composeApp/build.gradle.kts`
- Create: `composeApp/src/androidMain/AndroidManifest.xml`
- Create: `composeApp/src/androidMain/res/values/themes.xml`
- Create: `composeApp/src/androidMain/kotlin/com/hum/app/MainActivity.kt`
- Create: `composeApp/src/androidMain/kotlin/com/hum/app/Platform.kt`
- Create: `composeApp/src/iosMain/kotlin/com/hum/app/MainViewController.kt`
- Create: `composeApp/src/iosMain/kotlin/com/hum/app/Platform.kt`
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/App.kt`

- [ ] **Step 1: Generate Gradle wrapper**

Run:
```bash
cd /Users/cwharris/conductor/workspaces/hum/bangalore
gradle wrapper --gradle-version 8.11.1
```

If `gradle` is not installed, install it first:
```bash
brew install gradle
```

Expected: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, and `gradle/wrapper/gradle-wrapper.properties` are created.

- [ ] **Step 2: Create version catalog**

Create `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.2.20"
compose-multiplatform = "1.10.3"
agp = "8.7.3"
androidx-activity = "1.9.3"
kotlinx-coroutines = "1.9.0"

[libraries]
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
android-application = { id = "com.android.application", version.ref = "agp" }
```

- [ ] **Step 3: Create root build.gradle.kts**

Create `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
}
```

- [ ] **Step 4: Create settings.gradle.kts**

Create `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "hum"
include(":composeApp")
```

- [ ] **Step 5: Create gradle.properties**

Create `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2048M -Dkotlin.daemon.jvm.options=-Xmx2048M
android.useAndroidX=true
kotlin.code.style=official
org.gradle.configuration-cache=true
```

- [ ] **Step 6: Create composeApp/build.gradle.kts**

Create `composeApp/build.gradle.kts`:

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.application)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "com.hum.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hum.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

- [ ] **Step 7: Create AndroidManifest.xml**

Create `composeApp/src/androidMain/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <application
        android:allowBackup="false"
        android:label="Hum"
        android:supportsRtl="true"
        android:theme="@style/Theme.Hum">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 8: Create Android themes.xml**

Create `composeApp/src/androidMain/res/values/themes.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Hum" parent="android:Theme.Material.NoActionBar">
        <item name="android:statusBarColor">#1A1A2E</item>
        <item name="android:navigationBarColor">#1A1A2E</item>
        <item name="android:windowBackground">#1A1A2E</item>
    </style>
</resources>
```

- [ ] **Step 9: Create Android entry point**

Create `composeApp/src/androidMain/kotlin/com/hum/app/MainActivity.kt`:

```kotlin
package com.hum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformContext.appContext = applicationContext
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
```

Create `composeApp/src/androidMain/kotlin/com/hum/app/Platform.kt`:

```kotlin
package com.hum.app

import android.content.Context

object PlatformContext {
    lateinit var appContext: Context
}
```

- [ ] **Step 10: Create iOS entry point**

Create `composeApp/src/iosMain/kotlin/com/hum/app/MainViewController.kt`:

```kotlin
package com.hum.app

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }
```

Create `composeApp/src/iosMain/kotlin/com/hum/app/Platform.kt`:

```kotlin
package com.hum.app

object PlatformContext
```

- [ ] **Step 11: Create placeholder App.kt**

Create `composeApp/src/commonMain/kotlin/com/hum/app/App.kt`:

```kotlin
package com.hum.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hum",
            color = Color(0xFFEFEFEF),
            fontSize = 24.sp
        )
    }
}
```

- [ ] **Step 12: Verify Android build compiles**

Run:
```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

Expected: BUILD SUCCESSFUL. If version conflicts occur, check `gradle/libs.versions.toml` versions against the [Compose Multiplatform compatibility table](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html) and adjust.

- [ ] **Step 13: Commit**

```bash
git add gradle/ build.gradle.kts settings.gradle.kts gradle.properties composeApp/
git commit -m "feat: KMP project scaffold with Compose Multiplatform

Sets up Kotlin 2.2.20 + Compose Multiplatform 1.10.3 project
targeting Android and iOS with shared UI."
```

---

## Task 2: Data Model & CircleState

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/model/CircleState.kt`
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/model/Circle.kt`
- Test: `composeApp/src/commonTest/kotlin/com/hum/app/model/CircleStateTest.kt`

- [ ] **Step 1: Write the failing test**

Create `composeApp/src/commonTest/kotlin/com/hum/app/model/CircleStateTest.kt`:

```kotlin
package com.hum.app.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class CircleStateTest {

    @Test
    fun recordingCanOnlyTransitionToReady() {
        val state = CircleState.Recording
        assertTrue(state.canTransitionTo(CircleState.Ready))
        assertFalse(state.canTransitionTo(CircleState.Muted))
        assertFalse(state.canTransitionTo(CircleState.Soloed))
        assertFalse(state.canTransitionTo(CircleState.Recording))
    }

    @Test
    fun readyCanTransitionToMutedOrSoloed() {
        val state = CircleState.Ready
        assertTrue(state.canTransitionTo(CircleState.Muted))
        assertTrue(state.canTransitionTo(CircleState.Soloed))
        assertFalse(state.canTransitionTo(CircleState.Recording))
        assertFalse(state.canTransitionTo(CircleState.Ready))
    }

    @Test
    fun mutedCanTransitionToReadyOrSoloed() {
        val state = CircleState.Muted
        assertTrue(state.canTransitionTo(CircleState.Ready))
        assertTrue(state.canTransitionTo(CircleState.Soloed))
        assertFalse(state.canTransitionTo(CircleState.Recording))
        assertFalse(state.canTransitionTo(CircleState.Muted))
    }

    @Test
    fun soloedCanTransitionToReadyOrMuted() {
        val state = CircleState.Soloed
        assertTrue(state.canTransitionTo(CircleState.Ready))
        assertTrue(state.canTransitionTo(CircleState.Muted))
        assertFalse(state.canTransitionTo(CircleState.Recording))
        assertFalse(state.canTransitionTo(CircleState.Soloed))
    }

    @Test
    fun transitionToReturnsTargetState() {
        assertEquals(
            CircleState.Ready,
            CircleState.Recording.transitionTo(CircleState.Ready)
        )
        assertEquals(
            CircleState.Muted,
            CircleState.Ready.transitionTo(CircleState.Muted)
        )
    }

    @Test
    fun invalidTransitionThrows() {
        assertFailsWith<IllegalArgumentException> {
            CircleState.Recording.transitionTo(CircleState.Muted)
        }
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run:
```bash
./gradlew :composeApp:allTests
```

Expected: FAIL — `CircleState` not found.

- [ ] **Step 3: Create CircleState**

Create `composeApp/src/commonMain/kotlin/com/hum/app/model/CircleState.kt`:

```kotlin
package com.hum.app.model

enum class CircleState {
    Recording,
    Ready,
    Muted,
    Soloed;

    fun canTransitionTo(target: CircleState): Boolean = when (this) {
        Recording -> target == Ready
        Ready -> target == Muted || target == Soloed
        Muted -> target == Ready || target == Soloed
        Soloed -> target == Ready || target == Muted
    }

    fun transitionTo(target: CircleState): CircleState {
        require(canTransitionTo(target)) {
            "Invalid state transition: $this -> $target"
        }
        return target
    }
}
```

- [ ] **Step 4: Create Circle data class**

Create `composeApp/src/commonMain/kotlin/com/hum/app/model/Circle.kt`:

```kotlin
package com.hum.app.model

data class Circle(
    val id: String,
    val filePath: String,
    val durationMs: Long = 0,
    val state: CircleState = CircleState.Ready,
)
```

- [ ] **Step 5: Run tests to verify they pass**

Run:
```bash
./gradlew :composeApp:allTests
```

Expected: All 6 tests PASS.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/hum/app/model/ composeApp/src/commonTest/
git commit -m "feat: Circle data model and CircleState state machine

Recording -> Ready -> Muted/Soloed transitions with validation.
Invalid transitions throw IllegalArgumentException."
```

---

## Task 3: Theme & Design Tokens

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/ui/theme/Theme.kt`

- [ ] **Step 1: Create theme with colors and typography from DESIGN.md**

Create `composeApp/src/commonMain/kotlin/com/hum/app/ui/theme/Theme.kt`:

```kotlin
package com.hum.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object HumColors {
    val BgPrimary = Color(0xFF1A1A2E)
    val BgSurface = Color(0xFF25253E)
    val BgElevated = Color(0xFF2E2E4A)
    val TextPrimary = Color(0xFFEFEFEF)
    val TextSecondary = Color(0xFF9B9BB0)
    val AccentRecord = Color(0xFFFF3B5C)
    val StemBeat = Color(0xFF8B8B9E)
    val StemBlue = Color(0xFF5B8DEF)
    val StemGreen = Color(0xFF7BC47F)
    val StemAmber = Color(0xFFF0A050)
    val StemCoral = Color(0xFFEF7B7B)
}

val StemColors = listOf(
    HumColors.StemBlue,
    HumColors.StemGreen,
    HumColors.StemAmber,
    HumColors.StemCoral,
)

private val HumDarkScheme = darkColorScheme(
    primary = HumColors.AccentRecord,
    background = HumColors.BgPrimary,
    surface = HumColors.BgSurface,
    surfaceVariant = HumColors.BgElevated,
    onPrimary = HumColors.TextPrimary,
    onBackground = HumColors.TextPrimary,
    onSurface = HumColors.TextPrimary,
    onSurfaceVariant = HumColors.TextSecondary,
)

@Composable
fun HumTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HumDarkScheme,
        content = content,
    )
}
```

- [ ] **Step 2: Verify build**

Run:
```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/hum/app/ui/theme/
git commit -m "feat: Hum dark theme with design system tokens

Colors from DESIGN.md: warm charcoal backgrounds, stem colors
for circles, warm red accent for record button."
```

---

## Task 4: Audio Engine Interface, Fake Implementation & Beat Generator

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/audio/AudioEngine.kt`
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/audio/FakeAudioEngine.kt`
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/audio/BeatGenerator.kt`
- Create: `composeApp/src/androidMain/kotlin/com/hum/app/audio/AndroidAudioEngine.kt` (stub returning fake)
- Create: `composeApp/src/iosMain/kotlin/com/hum/app/audio/IosAudioEngine.kt` (stub returning fake)

- [ ] **Step 1: Create AudioEngine interface and expect factory**

Create `composeApp/src/commonMain/kotlin/com/hum/app/audio/AudioEngine.kt`:

```kotlin
package com.hum.app.audio

interface AudioEngine {
    fun loadBeat(data: ByteArray)
    fun playBeat()
    fun stopBeat()
    fun isBeatPlaying(): Boolean

    fun startRecording(): String
    fun stopRecording()
    fun isRecording(): Boolean

    fun playRecording(id: String)
    fun stopPlayback()
    fun isPlaying(): Boolean

    fun release()
}

expect fun createAudioEngine(): AudioEngine
```

- [ ] **Step 2: Create FakeAudioEngine for UI development**

Create `composeApp/src/commonMain/kotlin/com/hum/app/audio/FakeAudioEngine.kt`:

```kotlin
package com.hum.app.audio

class FakeAudioEngine : AudioEngine {
    private var beatPlaying = false
    private var recording = false
    private var playing = false
    private var nextId = 0

    override fun loadBeat(data: ByteArray) {}
    override fun playBeat() { beatPlaying = true }
    override fun stopBeat() { beatPlaying = false }
    override fun isBeatPlaying() = beatPlaying

    override fun startRecording(): String {
        recording = true
        return "fake_take_${nextId++}"
    }

    override fun stopRecording() { recording = false }
    override fun isRecording() = recording

    override fun playRecording(id: String) { playing = true }
    override fun stopPlayback() { playing = false }
    override fun isPlaying() = playing

    override fun release() {
        beatPlaying = false
        recording = false
        playing = false
    }
}
```

- [ ] **Step 3: Create platform stubs (both return FakeAudioEngine for now)**

Create `composeApp/src/androidMain/kotlin/com/hum/app/audio/AndroidAudioEngine.kt`:

```kotlin
package com.hum.app.audio

actual fun createAudioEngine(): AudioEngine = FakeAudioEngine()
```

Create `composeApp/src/iosMain/kotlin/com/hum/app/audio/IosAudioEngine.kt`:

```kotlin
package com.hum.app.audio

actual fun createAudioEngine(): AudioEngine = FakeAudioEngine()
```

- [ ] **Step 4: Create BeatGenerator (pure Kotlin metronome WAV)**

Create `composeApp/src/commonMain/kotlin/com/hum/app/audio/BeatGenerator.kt`:

```kotlin
package com.hum.app.audio

import kotlin.math.PI
import kotlin.math.sin

object BeatGenerator {

    fun generateMetronome(
        bpm: Int = 90,
        bars: Int = 8,
        sampleRate: Int = 44100,
    ): ByteArray {
        val beatsPerBar = 4
        val totalBeats = bars * beatsPerBar
        val samplesPerBeat = (sampleRate * 60.0 / bpm).toInt()
        val totalSamples = totalBeats * samplesPerBeat
        val clickSamples = (sampleRate * 0.04).toInt()

        val samples = ShortArray(totalSamples)

        for (beat in 0 until totalBeats) {
            val offset = beat * samplesPerBeat
            val freq = if (beat % beatsPerBar == 0) 1200.0 else 800.0
            val amp = if (beat % beatsPerBar == 0) 0.8 else 0.5

            for (i in 0 until clickSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = 1.0 - (i.toDouble() / clickSamples)
                val value = sin(2.0 * PI * freq * t) * amp * envelope
                samples[offset + i] = (value * Short.MAX_VALUE).toInt().toShort()
            }
        }

        return encodeWav(samples, sampleRate, channels = 1)
    }

    private fun encodeWav(samples: ShortArray, sampleRate: Int, channels: Int): ByteArray {
        val dataSize = samples.size * 2
        val result = ByteArray(44 + dataSize)

        fun writeInt(offset: Int, value: Int) {
            result[offset] = (value and 0xFF).toByte()
            result[offset + 1] = ((value shr 8) and 0xFF).toByte()
            result[offset + 2] = ((value shr 16) and 0xFF).toByte()
            result[offset + 3] = ((value shr 24) and 0xFF).toByte()
        }

        fun writeShort(offset: Int, value: Int) {
            result[offset] = (value and 0xFF).toByte()
            result[offset + 1] = ((value shr 8) and 0xFF).toByte()
        }

        "RIFF".encodeToByteArray().copyInto(result, 0)
        writeInt(4, 36 + dataSize)
        "WAVE".encodeToByteArray().copyInto(result, 8)
        "fmt ".encodeToByteArray().copyInto(result, 12)
        writeInt(16, 16)
        writeShort(20, 1)
        writeShort(22, channels)
        writeInt(24, sampleRate)
        writeInt(28, sampleRate * channels * 2)
        writeShort(32, channels * 2)
        writeShort(34, 16)
        "data".encodeToByteArray().copyInto(result, 36)
        writeInt(40, dataSize)

        for (i in samples.indices) {
            val s = samples[i].toInt()
            result[44 + i * 2] = (s and 0xFF).toByte()
            result[44 + i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
        }

        return result
    }
}
```

- [ ] **Step 5: Verify build and tests still pass**

Run:
```bash
./gradlew :composeApp:allTests
```

Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/hum/app/audio/ composeApp/src/androidMain/kotlin/com/hum/app/audio/ composeApp/src/iosMain/kotlin/com/hum/app/audio/
git commit -m "feat: AudioEngine interface, fake impl, and metronome beat generator

expect/actual factory pattern. FakeAudioEngine for UI development.
BeatGenerator creates a pure-Kotlin WAV metronome (no dependencies)."
```

---

## Task 5: Song State Manager

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/SongState.kt`

- [ ] **Step 1: Create SongState**

Create `composeApp/src/commonMain/kotlin/com/hum/app/SongState.kt`:

```kotlin
package com.hum.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.hum.app.audio.AudioEngine
import com.hum.app.model.Circle
import com.hum.app.model.CircleState
import com.hum.app.ui.theme.HumColors
import com.hum.app.ui.theme.StemColors

class StemRow(
    val name: String,
    val color: Color,
) {
    val circles = mutableStateListOf<Circle>()
}

class SongState(private val audioEngine: AudioEngine) {
    val rows = mutableStateListOf<StemRow>()
    var selectedRowIndex by mutableStateOf(1)
    var isRecording by mutableStateOf(false)
    var playingCircleId: String? by mutableStateOf(null)
    private var currentRecordingId: String? = null

    init {
        rows.add(StemRow("Beat", HumColors.StemBeat))
        rows.add(StemRow("Vocals", StemColors[0]))
    }

    fun toggleRecording() {
        if (isRecording) stopRecording() else startRecording()
    }

    private fun startRecording() {
        if (isRecording) return
        val id = audioEngine.startRecording()
        currentRecordingId = id
        isRecording = true
        audioEngine.playBeat()
    }

    private fun stopRecording() {
        if (!isRecording) return
        audioEngine.stopRecording()
        audioEngine.stopBeat()
        isRecording = false
        val id = currentRecordingId ?: return
        rows[selectedRowIndex].circles.add(
            Circle(id = id, filePath = id, state = CircleState.Ready)
        )
        currentRecordingId = null
    }

    fun toggleCircle(id: String) {
        if (playingCircleId == id) {
            audioEngine.stopPlayback()
            playingCircleId = null
        } else {
            audioEngine.stopPlayback()
            audioEngine.playRecording(id)
            playingCircleId = id
        }
    }

    fun selectRow(index: Int) {
        if (index in rows.indices && index != 0) {
            selectedRowIndex = index
        }
    }

    fun addRow() {
        val colorIndex = (rows.size - 1) % StemColors.size
        rows.add(StemRow("Stem ${rows.size}", StemColors[colorIndex]))
    }
}
```

- [ ] **Step 2: Verify build**

Run:
```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/hum/app/SongState.kt
git commit -m "feat: SongState manager for recording, playback, and row management

Manages StemRows with circles, recording toggle, circle playback,
row selection, and row creation. Uses Compose mutableState for reactivity."
```

---

## Task 6: UI Components — CircleView, StemRow, RecordButton

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/ui/CircleView.kt`
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/ui/StemRow.kt`
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/ui/RecordButton.kt`

- [ ] **Step 1: Create CircleView**

Create `composeApp/src/commonMain/kotlin/com/hum/app/ui/CircleView.kt`:

```kotlin
package com.hum.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hum.app.model.CircleState

@Composable
fun CircleView(
    index: Int,
    state: CircleState,
    color: Color,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val size = if (state == CircleState.Muted) 48.dp else 56.dp
    val alpha = if (state == CircleState.Muted) 0.4f else 1f

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
            .clickable(onClick = onClick)
            .then(
                if (isPlaying) {
                    Modifier.drawBehind {
                        drawCircle(
                            color = color,
                            style = Stroke(width = 3.dp.toPx()),
                            radius = this.size.minDimension / 2,
                        )
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${index + 1}",
            color = Color.White,
            fontSize = 12.sp,
        )
    }
}
```

- [ ] **Step 2: Create StemRowView**

Create `composeApp/src/commonMain/kotlin/com/hum/app/ui/StemRow.kt`:

```kotlin
package com.hum.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hum.app.model.Circle
import com.hum.app.ui.theme.HumColors

@Composable
fun StemRowView(
    name: String,
    color: Color,
    circles: List<Circle>,
    isSelected: Boolean,
    playingCircleId: String?,
    onRowTap: () -> Unit,
    onCircleTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            color = if (isSelected) color else HumColors.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier
                .width(64.dp)
                .clickable(onClick = onRowTap),
        )

        if (circles.isEmpty()) {
            Text(
                text = "Tap record to add a take",
                color = HumColors.TextSecondary.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                itemsIndexed(circles) { index, circle ->
                    CircleView(
                        index = index,
                        state = circle.state,
                        color = color,
                        isPlaying = circle.id == playingCircleId,
                        onClick = { onCircleTap(circle.id) },
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Create RecordButton**

Create `composeApp/src/commonMain/kotlin/com/hum/app/ui/RecordButton.kt`:

```kotlin
package com.hum.app.ui

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hum.app.ui.theme.HumColors

@Composable
fun RecordButton(
    isRecording: Boolean,
    elapsedSeconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Box(
        modifier = modifier
            .size(72.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(HumColors.AccentRecord)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isRecording) {
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            Text(
                text = "%d:%02d".format(minutes, seconds),
                color = Color.White,
                fontSize = 16.sp,
            )
        }
    }
}
```

- [ ] **Step 4: Verify build**

Run:
```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/hum/app/ui/CircleView.kt composeApp/src/commonMain/kotlin/com/hum/app/ui/StemRow.kt composeApp/src/commonMain/kotlin/com/hum/app/ui/RecordButton.kt
git commit -m "feat: CircleView, StemRowView, and RecordButton UI components

56dp circles with state-based opacity/size (DESIGN.md spec).
RecordButton with breathing pulse animation during recording.
StemRowView with horizontal scrolling circle list."
```

---

## Task 7: CircleScreen & App Integration

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/hum/app/ui/CircleScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/hum/app/App.kt`

- [ ] **Step 1: Create CircleScreen**

Create `composeApp/src/commonMain/kotlin/com/hum/app/ui/CircleScreen.kt`:

```kotlin
package com.hum.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hum.app.SongState
import com.hum.app.ui.theme.HumColors
import kotlinx.coroutines.delay

@Composable
fun CircleScreen(
    songState: SongState,
    modifier: Modifier = Modifier,
) {
    var recordingSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(songState.isRecording) {
        recordingSeconds = 0
        while (songState.isRecording) {
            delay(1000)
            recordingSeconds++
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HumColors.BgPrimary),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Untitled",
                color = HumColors.TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp),
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 100.dp),
            ) {
                itemsIndexed(songState.rows) { index, row ->
                    StemRowView(
                        name = row.name,
                        color = row.color,
                        circles = row.circles,
                        isSelected = index == songState.selectedRowIndex,
                        playingCircleId = songState.playingCircleId,
                        onRowTap = { songState.selectRow(index) },
                        onCircleTap = { songState.toggleCircle(it) },
                    )
                }

                item {
                    TextButton(
                        onClick = { songState.addRow() },
                        modifier = Modifier.padding(start = 16.dp),
                    ) {
                        Text(
                            text = "+ New stem",
                            color = HumColors.TextSecondary,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }

        RecordButton(
            isRecording = songState.isRecording,
            elapsedSeconds = recordingSeconds,
            onClick = { songState.toggleRecording() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        )
    }
}
```

- [ ] **Step 2: Update App.kt to wire everything together**

Replace the contents of `composeApp/src/commonMain/kotlin/com/hum/app/App.kt` with:

```kotlin
package com.hum.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hum.app.audio.BeatGenerator
import com.hum.app.audio.createAudioEngine
import com.hum.app.ui.CircleScreen
import com.hum.app.ui.theme.HumTheme

@Composable
fun App() {
    val audioEngine = remember { createAudioEngine() }
    val songState = remember {
        val beat = BeatGenerator.generateMetronome(bpm = 90, bars = 8)
        audioEngine.loadBeat(beat)
        SongState(audioEngine)
    }

    HumTheme {
        CircleScreen(songState = songState)
    }
}
```

- [ ] **Step 3: Verify build and tests**

Run:
```bash
./gradlew :composeApp:allTests && ./gradlew :composeApp:compileDebugKotlinAndroid
```

Expected: All tests pass, build succeeds. The app can now be launched on an Android emulator and will show the full UI with the fake audio engine (tapping record creates circles, tapping circles toggles "playback" state).

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/hum/app/ui/CircleScreen.kt composeApp/src/commonMain/kotlin/com/hum/app/App.kt
git commit -m "feat: CircleScreen main screen and App wiring

Full single-screen layout: song title, stem rows with circles,
floating record button with timer, '+ New stem' action.
Wired to SongState with FakeAudioEngine for UI testing."
```

---

## Task 8: Android Audio Engine

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/hum/app/audio/AndroidAudioEngine.kt`

This task replaces the fake engine with real Android audio. Uses `MediaPlayer` for beat playback, `AudioRecord` for recording with real-time monitoring via `AudioTrack`, and `MediaPlayer` for recording playback.

- [ ] **Step 1: Implement AndroidAudioEngine**

Replace `composeApp/src/androidMain/kotlin/com/hum/app/audio/AndroidAudioEngine.kt` with:

```kotlin
package com.hum.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.hum.app.PlatformContext
import java.io.File
import java.io.FileOutputStream

actual fun createAudioEngine(): AudioEngine = AndroidAudioEngine()

class AndroidAudioEngine : AudioEngine {
    private var beatPlayer: MediaPlayer? = null
    private var recorder: AudioRecord? = null
    private var monitorTrack: AudioTrack? = null
    private var recordingThread: Thread? = null
    private var playbackPlayer: MediaPlayer? = null

    private var _isRecording = false
    private var _isPlaying = false

    private val sampleRate = 44100
    private val channelIn = AudioFormat.CHANNEL_IN_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelIn, encoding)

    private val recordingsDir: File
        get() = File(PlatformContext.appContext.filesDir, "recordings").also { it.mkdirs() }

    override fun loadBeat(data: ByteArray) {
        val tempFile = File(PlatformContext.appContext.cacheDir, "beat.wav")
        tempFile.writeBytes(data)
        beatPlayer = MediaPlayer().apply {
            setDataSource(tempFile.absolutePath)
            prepare()
            isLooping = true
        }
    }

    override fun playBeat() {
        beatPlayer?.start()
    }

    override fun stopBeat() {
        beatPlayer?.pause()
        beatPlayer?.seekTo(0)
    }

    override fun isBeatPlaying() = beatPlayer?.isPlaying == true

    override fun startRecording(): String {
        val id = "take_${System.currentTimeMillis()}"
        val pcmFile = File(recordingsDir, "$id.pcm")

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelIn,
            encoding,
            bufferSize,
        )

        monitorTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(encoding)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()

        recorder?.startRecording()
        monitorTrack?.play()
        _isRecording = true

        recordingThread = Thread {
            val buffer = ByteArray(bufferSize)
            FileOutputStream(pcmFile).use { fos ->
                while (_isRecording) {
                    val read = recorder?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        fos.write(buffer, 0, read)
                        monitorTrack?.write(buffer, 0, read)
                    }
                }
            }
            val wavFile = File(recordingsDir, "$id.wav")
            writePcmToWav(pcmFile, wavFile)
            pcmFile.delete()
        }.also { it.start() }

        return id
    }

    override fun stopRecording() {
        _isRecording = false
        recordingThread?.join(2000)
        recorder?.stop()
        recorder?.release()
        recorder = null
        monitorTrack?.stop()
        monitorTrack?.release()
        monitorTrack = null
    }

    override fun isRecording() = _isRecording

    override fun playRecording(id: String) {
        stopPlayback()
        val wavFile = File(recordingsDir, "$id.wav")
        if (!wavFile.exists()) return

        playbackPlayer = MediaPlayer().apply {
            setDataSource(wavFile.absolutePath)
            setOnCompletionListener { _isPlaying = false }
            prepare()
            start()
        }
        _isPlaying = true
    }

    override fun stopPlayback() {
        playbackPlayer?.release()
        playbackPlayer = null
        _isPlaying = false
    }

    override fun isPlaying() = _isPlaying

    override fun release() {
        stopRecording()
        stopPlayback()
        beatPlayer?.release()
        beatPlayer = null
    }

    private fun writePcmToWav(pcmFile: File, wavFile: File) {
        val pcmData = pcmFile.readBytes()
        val dataLen = pcmData.size
        val channels = 1
        val byteRate = sampleRate * channels * 2

        FileOutputStream(wavFile).use { out ->
            // RIFF header
            out.write("RIFF".toByteArray())
            out.write(intBytes(36 + dataLen))
            out.write("WAVE".toByteArray())
            // fmt chunk
            out.write("fmt ".toByteArray())
            out.write(intBytes(16))
            out.write(shortBytes(1))
            out.write(shortBytes(channels))
            out.write(intBytes(sampleRate))
            out.write(intBytes(byteRate))
            out.write(shortBytes(channels * 2))
            out.write(shortBytes(16))
            // data chunk
            out.write("data".toByteArray())
            out.write(intBytes(dataLen))
            out.write(pcmData)
        }
    }

    private fun intBytes(v: Int) = byteArrayOf(
        (v and 0xFF).toByte(),
        ((v shr 8) and 0xFF).toByte(),
        ((v shr 16) and 0xFF).toByte(),
        ((v shr 24) and 0xFF).toByte(),
    )

    private fun shortBytes(v: Int) = byteArrayOf(
        (v and 0xFF).toByte(),
        ((v shr 8) and 0xFF).toByte(),
    )
}
```

- [ ] **Step 2: Add runtime permission request**

Create `composeApp/src/androidMain/kotlin/com/hum/app/RequestPermission.kt`:

```kotlin
package com.hum.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat

@Composable
actual fun RequestMicrophonePermission() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            PlatformContext.appContext,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
```

Create `composeApp/src/commonMain/kotlin/com/hum/app/RequestPermission.kt`:

```kotlin
package com.hum.app

import androidx.compose.runtime.Composable

@Composable
expect fun RequestMicrophonePermission()
```

Create `composeApp/src/iosMain/kotlin/com/hum/app/RequestPermission.kt`:

```kotlin
package com.hum.app

import androidx.compose.runtime.Composable

@Composable
actual fun RequestMicrophonePermission() {
    // iOS requests permission automatically when accessing the mic.
    // NSMicrophoneUsageDescription must be set in Info.plist.
}
```

- [ ] **Step 3: Add permission request to App.kt**

In `composeApp/src/commonMain/kotlin/com/hum/app/App.kt`, add the permission request call inside `HumTheme`:

```kotlin
package com.hum.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hum.app.audio.BeatGenerator
import com.hum.app.audio.createAudioEngine
import com.hum.app.ui.CircleScreen
import com.hum.app.ui.theme.HumTheme

@Composable
fun App() {
    val audioEngine = remember { createAudioEngine() }
    val songState = remember {
        val beat = BeatGenerator.generateMetronome(bpm = 90, bars = 8)
        audioEngine.loadBeat(beat)
        SongState(audioEngine)
    }

    HumTheme {
        RequestMicrophonePermission()
        CircleScreen(songState = songState)
    }
}
```

- [ ] **Step 4: Verify build**

Run:
```bash
./gradlew :composeApp:assembleDebug
```

Expected: BUILD SUCCESSFUL. APK generated at `composeApp/build/outputs/apk/debug/composeApp-debug.apk`.

- [ ] **Step 5: Test on device or emulator**

Install and run on a connected Android device (real device preferred for audio latency testing):

```bash
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

**Manual test checklist:**
1. App launches with dark background, "Untitled" title, Beat row, Vocals row, record button
2. Grant microphone permission when prompted
3. Tap record button → button pulses, timer counts, metronome beat plays
4. Sing or speak into the mic
5. Tap record button again → recording stops, circle appears in Vocals row
6. Tap the circle → hear your recording play back
7. Record again → second circle appears
8. Tap "+ New stem" → new row appears

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/androidMain/kotlin/com/hum/app/audio/AndroidAudioEngine.kt composeApp/src/androidMain/kotlin/com/hum/app/RequestPermission.kt composeApp/src/commonMain/kotlin/com/hum/app/RequestPermission.kt composeApp/src/iosMain/kotlin/com/hum/app/RequestPermission.kt composeApp/src/commonMain/kotlin/com/hum/app/App.kt
git commit -m "feat: Android audio engine with recording, monitoring, and playback

MediaPlayer for beat, AudioRecord + AudioTrack (low-latency mode)
for recording with real-time monitoring, WAV export for playback.
Runtime RECORD_AUDIO permission request on Android."
```

---

## Task 9: iOS Audio Engine (Stretch Goal)

**Files:**
- Modify: `composeApp/src/iosMain/kotlin/com/hum/app/audio/IosAudioEngine.kt`
- Create: `iosApp/iosApp/iOSApp.swift`
- Create: `iosApp/iosApp/Info.plist`

> **Note:** This task requires a Mac with Xcode installed. If building for iOS is not feasible this weekend, skip this task — all shared code already compiles for iOS. The audio engine can be wired in later.

- [ ] **Step 1: Implement IosAudioEngine**

Replace `composeApp/src/iosMain/kotlin/com/hum/app/audio/IosAudioEngine.kt` with:

```kotlin
package com.hum.app.audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioPlayerNodeBufferLoops
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.setActive
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual fun createAudioEngine(): AudioEngine = IosAudioEngine()

@OptIn(ExperimentalForeignApi::class)
class IosAudioEngine : AudioEngine {
    private val engine = AVAudioEngine()
    private var beatNode: AVAudioPlayerNode? = null
    private var beatBuffer: AVAudioPCMBuffer? = null
    private var playbackPlayer: AVAudioPlayer? = null

    private var _isRecording = false
    private var _isPlaying = false
    private var currentRecordingId: String? = null

    private val recordingsDir: String
        get() {
            val paths = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory, NSUserDomainMask, true
            )
            @Suppress("UNCHECKED_CAST")
            val dir = "${(paths as List<String>).first()}/recordings"
            NSFileManager.defaultManager.createDirectoryAtPath(
                dir, withIntermediateDirectories = true, attributes = null, error = null
            )
            return dir
        }

    override fun loadBeat(data: ByteArray) {
        val tempPath = "${NSTemporaryDirectory()}beat.wav"
        val nsData = data.toNSData()
        nsData.writeToFile(tempPath, atomically = true)

        val url = NSURL.fileURLWithPath(tempPath)
        val audioFile = AVAudioFile(forReading = url, error = null) ?: return
        val format = audioFile.processingFormat

        beatNode = AVAudioPlayerNode().also { node ->
            engine.attachNode(node)
            engine.connect(node, to = engine.mainMixerNode, format = format)
        }

        val buffer = AVAudioPCMBuffer(
            PCMFormat = format,
            frameCapacity = audioFile.length.toUInt(),
        ) ?: return
        audioFile.readIntoBuffer(buffer, error = null)
        beatBuffer = buffer
    }

    override fun playBeat() {
        configureSession()
        if (!engine.isRunning) engine.startAndReturnError(null)
        val buf = beatBuffer ?: return
        beatNode?.scheduleBuffer(buf, atTime = null, options = AVAudioPlayerNodeBufferLoops, completionHandler = null)
        beatNode?.play()
    }

    override fun stopBeat() {
        beatNode?.stop()
    }

    override fun isBeatPlaying() = beatNode?.isPlaying == true

    override fun startRecording(): String {
        configureSession()
        val id = "take_${NSDate().timeIntervalSince1970.toLong()}"
        currentRecordingId = id
        val filePath = "$recordingsDir/$id.wav"
        val url = NSURL.fileURLWithPath(filePath)

        val inputNode = engine.inputNode
        val format = inputNode.outputFormatForBus(0u)

        val file = AVAudioFile(forWriting = url, settings = format.settings, error = null)

        inputNode.installTapOnBus(0u, bufferSize = 1024u, format = format) { buffer, _ ->
            file?.writeFromBuffer(buffer!!, error = null)
        }

        if (!engine.isRunning) engine.startAndReturnError(null)
        _isRecording = true
        return id
    }

    override fun stopRecording() {
        engine.inputNode.removeTapOnBus(0u)
        _isRecording = false
    }

    override fun isRecording() = _isRecording

    override fun playRecording(id: String) {
        stopPlayback()
        val filePath = "$recordingsDir/$id.wav"
        val url = NSURL.fileURLWithPath(filePath)
        playbackPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
        playbackPlayer?.play()
        _isPlaying = true
    }

    override fun stopPlayback() {
        playbackPlayer?.stop()
        playbackPlayer = null
        _isPlaying = false
    }

    override fun isPlaying() = _isPlaying

    override fun release() {
        stopRecording()
        stopPlayback()
        engine.stop()
    }

    private fun configureSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            withOptions = AVAudioSessionCategoryOptionDefaultToSpeaker or
                AVAudioSessionCategoryOptionMixWithOthers,
            error = null,
        )
        session.setActive(true, error = null)
    }

    private fun ByteArray.toNSData(): platform.Foundation.NSData {
        return kotlinx.cinterop.memScoped {
            val pinned = this@toNSData.toUByteArray()
            platform.Foundation.NSData.create(
                bytes = kotlinx.cinterop.allocArrayOf(pinned),
                length = this@toNSData.size.toULong(),
            )
        }
    }
}
```

> **Caveat:** The Kotlin/Native interop with AVFAudio APIs may need adjustments based on the actual header mappings available in Kotlin 2.2.20. The exact method signatures (especially for `installTapOnBus`, `scheduleBuffer`, and `NSData` creation) may differ. If compilation fails, check the Kotlin/Native ObjC interop documentation and adjust method names/signatures.

- [ ] **Step 2: Create iOS app wrapper**

Create `iosApp/iosApp/iOSApp.swift`:

```swift
import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

Create `iosApp/iosApp/Info.plist`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>NSMicrophoneUsageDescription</key>
    <string>Hum needs microphone access to record your vocals.</string>
</dict>
</plist>
```

- [ ] **Step 3: Verify iOS compilation**

Run:
```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

Expected: BUILD SUCCESSFUL. Full Xcode project setup and running on simulator is a separate step requiring Xcode configuration.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/iosMain/kotlin/com/hum/app/audio/IosAudioEngine.kt iosApp/
git commit -m "feat: iOS audio engine with AVAudioEngine

AVAudioEngine for beat playback, recording via input node tap,
AVAudioPlayer for recording playback. PlayAndRecord session
with speaker output and mix-with-others."
```

---

## Spike Validation Checklist

After completing Tasks 1-8 (Android), test on a **real phone** (not emulator — emulator audio latency is not representative):

- [ ] App opens to dark screen with Beat row, Vocals row, record button
- [ ] Tap record → metronome plays, timer counts on button
- [ ] Speak/sing → can hear yourself through phone speaker or headphones (monitoring)
- [ ] Tap record again → circle appears in Vocals row
- [ ] Tap circle → recording plays back
- [ ] Record multiple takes → multiple circles appear
- [ ] Monitoring latency feels acceptable (< 50ms is good, < 20ms is target)
- [ ] No audio glitches or dropouts during recording

## Key Findings to Record

After testing, document in `.context/notes.md`:
1. **Monitoring latency:** Approximate round-trip latency (ms). Acceptable or not?
2. **Audio quality:** Any artifacts, clipping, or issues?
3. **Beat sync:** Does the recording align with the metronome on playback?
4. **Device tested:** Make, model, Android version
5. **Verdict:** Does vocal recording over a beat feel good on this phone?

## Upgrade Path (Post-Spike)

If the spike validates the concept, these are the next improvements:
- Replace `AudioRecord`/`AudioTrack` with **Oboe** (C++ via JNI) for lower Android latency
- Evaluate **MWEngine** as a higher-level alternative to raw Oboe
- Add beat + recording simultaneous playback (mix)
- Add mute/solo/delete via long-press radial menu
- Add volume control per circle
- Persist songs with SQLDelight
