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
        audioEngine.stopPlayback()
        playingCircleId = null
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
