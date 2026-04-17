package com.hum.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
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
            out.write("RIFF".toByteArray())
            out.write(intBytes(36 + dataLen))
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(intBytes(16))
            out.write(shortBytes(1))
            out.write(shortBytes(channels))
            out.write(intBytes(sampleRate))
            out.write(intBytes(byteRate))
            out.write(shortBytes(channels * 2))
            out.write(shortBytes(16))
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
