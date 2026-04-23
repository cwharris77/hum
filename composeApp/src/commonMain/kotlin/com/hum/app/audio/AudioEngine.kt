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
