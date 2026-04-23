package com.hum.app.audio

actual fun createAudioEngine(): AudioEngine = FakeAudioEngine()
