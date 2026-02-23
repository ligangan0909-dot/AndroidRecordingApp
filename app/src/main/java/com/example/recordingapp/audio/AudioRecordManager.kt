package com.example.recordingapp.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import java.io.File
import kotlin.math.abs
import kotlin.math.log10

/**
 * 音频录制管理器
 * 使用AudioRecord API录制音频并保存为WAV格式
 */
class AudioRecordManager {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val CHANNELS = 1
        private const val BITS_PER_SAMPLE = 16
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var wavWriter: WavFileWriter? = null
    private var isRecording = false
    
    var onAudioLevelChanged: ((Int) -> Unit)? = null

    /**
     * 开始录音
     */
    fun startRecording(outputFile: File) {
        if (isRecording) return

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        wavWriter = WavFileWriter(outputFile, SAMPLE_RATE, CHANNELS, BITS_PER_SAMPLE)
        
        audioRecord?.startRecording()
        isRecording = true

        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(bufferSize)
            
            while (isRecording) {
                val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (readSize > 0) {
                    wavWriter?.write(buffer, readSize)
                    
                    // 计算音频电平
                    val level = calculateAudioLevel(buffer, readSize)
                    withContext(Dispatchers.Main) {
                        onAudioLevelChanged?.invoke(level)
                    }
                }
            }
        }
    }

    /**
     * 停止录音
     */
    fun stopRecording() {
        if (!isRecording) return

        isRecording = false
        recordingJob?.cancel()
        
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        
        wavWriter?.close()
        wavWriter = null
    }

    /**
     * 计算音频电平（0-100）
     */
    private fun calculateAudioLevel(buffer: ByteArray, size: Int): Int {
        var sum = 0.0
        for (i in 0 until size step 2) {
            val sample = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)
            sum += abs(sample.toDouble())
        }
        val average = sum / (size / 2)
        val db = 20 * log10(average / 32768.0)
        
        // 将分贝值映射到0-100范围
        val normalized = ((db + 60) / 60 * 100).coerceIn(0.0, 100.0)
        return normalized.toInt()
    }

    /**
     * 释放资源
     */
    fun release() {
        stopRecording()
    }
}
