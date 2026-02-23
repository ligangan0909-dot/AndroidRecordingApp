package com.example.recordingapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.recordingapp.audio.AudioRecordManager
import com.example.recordingapp.databinding.ActivityRecordingBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 录音界面Activity
 * 显示录音时长和音频电平，提供停止录音功能
 */
class RecordingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordingBinding
    private val audioRecordManager = AudioRecordManager()
    private var timerJob: Job? = null
    private var startTime = 0L
    private lateinit var outputFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecording()
        setupListeners()
    }

    private fun setupRecording() {
        // 创建输出文件
        val recordingsDir = File(filesDir, "recordings").apply {
            if (!exists()) mkdirs()
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        outputFile = File(recordingsDir, "recording_$timestamp.wav")

        // 设置音频电平回调
        audioRecordManager.onAudioLevelChanged = { level ->
            binding.audioLevelBar.progress = level
        }

        // 开始录音
        audioRecordManager.startRecording(outputFile)
        startTimer()
    }

    private fun setupListeners() {
        binding.btnStopRecording.setOnClickListener {
            stopRecording()
        }
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis()
        timerJob = lifecycleScope.launch {
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                updateTimerDisplay(elapsed)
                delay(100)
            }
        }
    }

    private fun updateTimerDisplay(milliseconds: Long) {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000 / 60) % 60
        val hours = milliseconds / 1000 / 3600
        
        binding.tvRecordingTime.text = String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours, minutes, seconds
        )
    }

    private fun stopRecording() {
        timerJob?.cancel()
        audioRecordManager.stopRecording()
        
        Toast.makeText(this, R.string.recording_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        audioRecordManager.release()
    }
}
