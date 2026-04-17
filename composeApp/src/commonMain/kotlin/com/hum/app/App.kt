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
