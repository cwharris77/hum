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
