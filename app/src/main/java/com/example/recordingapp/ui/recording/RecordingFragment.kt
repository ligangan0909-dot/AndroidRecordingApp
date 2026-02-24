package com.example.recordingapp.ui.recording

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.recordingapp.R
import com.example.recordingapp.audio.AudioRecordManager
import com.example.recordingapp.databinding.FragmentRecordingBinding
import java.io.File
import kotlin.random.Random

class RecordingFragment : Fragment() {
    private var _binding: FragmentRecordingBinding? = null
    private val binding get() = _binding!!
    
    private var audioRecordManager: AudioRecordManager? = null
    private var isRecording = false
    private var recordingStartTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeRunnable: Runnable? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        } else {
            Toast.makeText(requireContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }
    
    private fun setupViews() {
        binding.recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                checkPermissionAndRecord()
            }
        }
    }
    
    private fun checkPermissionAndRecord() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startRecording() {
        try {
            val outputDir = requireContext().getExternalFilesDir(null)
            val outputFile = File(outputDir, "recording_${System.currentTimeMillis()}.wav")
            
            audioRecordManager = AudioRecordManager()
            audioRecordManager?.startRecording(outputFile)
            
            isRecording = true
            binding.recordButton.setRecording(true)
            recordingStartTime = System.currentTimeMillis()
            
            startTimeUpdate()
            startWaveformUpdate()
            
            Toast.makeText(requireContext(), "开始录音", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "录音失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopRecording() {
        try {
            audioRecordManager?.stopRecording()
            audioRecordManager = null
            
            isRecording = false
            binding.recordButton.setRecording(false)
            binding.waveformView.clear()
            
            stopTimeUpdate()
            stopWaveformUpdate()
            
            Toast.makeText(requireContext(), R.string.recording_saved, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "停止录音失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startTimeUpdate() {
        updateTimeRunnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - recordingStartTime
                val seconds = (elapsedMillis / 1000) % 60
                val minutes = (elapsedMillis / 60000) % 60
                val hours = elapsedMillis / 3600000
                
                binding.recordingTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                handler.postDelayed(this, 100)
            }
        }
        handler.post(updateTimeRunnable!!)
    }
    
    private fun stopTimeUpdate() {
        updateTimeRunnable?.let { handler.removeCallbacks(it) }
        binding.recordingTime.text = "00:00:00"
    }
    
    private fun startWaveformUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isRecording) {
                    val amplitude = Random.nextFloat() * 0.5f + 0.3f
                    binding.waveformView.addAmplitude(amplitude)
                    handler.postDelayed(this, 100)
                }
            }
        }, 100)
    }
    
    private fun stopWaveformUpdate() {
        binding.waveformView.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isRecording) {
            stopRecording()
        }
        _binding = null
    }
}
