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
