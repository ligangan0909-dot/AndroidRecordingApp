package com.example.recordingapp.ui.playback

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recordingapp.R
import com.example.recordingapp.RecordingApp
import com.example.recordingapp.audio.AudioPlayer
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.databinding.FragmentPlaybackBinding
import kotlinx.coroutines.launch
import java.io.File

class PlaybackFragment : Fragment() {
    private var _binding: FragmentPlaybackBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var audioPlayer: AudioPlayer
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var showingTranscription = false
    
    private val viewModel: PlaybackViewModel by lazy {
        val app = requireActivity().application as RecordingApp
        ViewModelProvider(this, PlaybackViewModelFactory(app.transcriptionService))[PlaybackViewModel::class.java]
    }
    
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                updateSeekBar()
                handler.postDelayed(this, 100)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaybackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        audioPlayer = AudioPlayer()
        setupToolbar()
        setupPlaybackControls()
        setupActionButtons()
        loadRecording()
        observeTranscriptionState()
        loadExistingTranscription()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupPlaybackControls() {
        binding.playPauseButton.setOnClickListener {
            togglePlayPause()
        }
        
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioPlayer.mediaPlayer?.seekTo(progress)
                    updateTimeDisplay()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        audioPlayer.onPlaybackComplete = {
            isPlaying = false
            binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            handler.removeCallbacks(updateSeekBarRunnable)
        }
    }
    
    private fun setupActionButtons() {
        binding.transcribeButton.setOnClickListener {
            val recordingId = arguments?.getLong("recordingId", 0L).toString()
            val filePath = arguments?.getString("filePath") ?: return@setOnClickListener
            val file = File(filePath)
            
            if (!file.exists()) {
                Toast.makeText(requireContext(), "文件不存在", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.startTranscription(recordingId, filePath, file)
        }
        
        binding.toggleViewButton.setOnClickListener {
            toggleView()
        }
    }
    
    private fun loadRecording() {
        val filePath = arguments?.getString("filePath") ?: return
        val file = File(filePath)
        binding.fileName.text = file.nameWithoutExtension
    }
    
    private fun togglePlayPause() {
        if (isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }
    
    private fun startPlayback() {
        val filePath = arguments?.getString("filePath") ?: return
        val file = File(filePath)
        
        if (!file.exists()) {
            Toast.makeText(requireContext(), "文件不存在", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (audioPlayer.mediaPlayer == null) {
            audioPlayer.play(file)
            binding.seekBar.max = audioPlayer.getDuration()
            updateTimeDisplay()
        } else {
            audioPlayer.resume()
        }
        
        isPlaying = true
        binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        handler.post(updateSeekBarRunnable)
    }
    
    private fun pausePlayback() {
        audioPlayer.pause()
        isPlaying = false
        binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        handler.removeCallbacks(updateSeekBarRunnable)
    }
    
    private fun updateSeekBar() {
        val currentPosition = audioPlayer.getCurrentPosition()
        binding.seekBar.progress = currentPosition
        updateTimeDisplay()
    }
    
    private fun updateTimeDisplay() {
        val current = audioPlayer.getCurrentPosition()
        val total = audioPlayer.getDuration()
        
        binding.currentTime.text = formatTime(current)
        binding.totalTime.text = formatTime(total)
    }
    
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    
    private fun toggleView() {
        showingTranscription = !showingTranscription
        
        if (showingTranscription) {
            binding.waveformView.visibility = View.GONE
            binding.transcriptionScroll.visibility = View.VISIBLE
            binding.toggleViewButton.text = "查看波形"
        } else {
            binding.waveformView.visibility = View.VISIBLE
            binding.transcriptionScroll.visibility = View.GONE
            binding.toggleViewButton.text = "查看转写"
        }
    }
    
    private fun observeTranscriptionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transcriptionState.collect { state ->
                when (state) {
                    is TranscriptionState.Idle -> {
                        binding.transcriptionProgress.visibility = View.GONE
                        binding.transcriptionStatus.visibility = View.GONE
                        binding.toggleViewButton.visibility = View.GONE
                    }
                    
                    is TranscriptionState.Uploading -> {
                        binding.transcriptionProgress.visibility = View.VISIBLE
                        binding.transcriptionStatus.visibility = View.VISIBLE
                        binding.transcriptionStatus.text = "正在上传..."
                        binding.transcriptionScroll.visibility = View.VISIBLE
                        binding.waveformView.visibility = View.GONE
                    }
                    
                    is TranscriptionState.Processing -> {
                        binding.transcriptionProgress.visibility = View.VISIBLE
                        binding.transcriptionStatus.visibility = View.VISIBLE
                        binding.transcriptionStatus.text = "正在转写..."
                    }
                    
                    is TranscriptionState.Success -> {
                        binding.transcriptionProgress.visibility = View.GONE
                        binding.transcriptionStatus.visibility = View.GONE
                        binding.transcriptionText.text = state.result.fullText
                        binding.toggleViewButton.visibility = View.VISIBLE
                        
                        // Show transcription view
                        binding.transcriptionScroll.visibility = View.VISIBLE
                        binding.waveformView.visibility = View.GONE
                        showingTranscription = true
                        binding.toggleViewButton.text = "查看波形"
                    }
                    
                    is TranscriptionState.Error -> {
                        binding.transcriptionProgress.visibility = View.GONE
                        binding.transcriptionStatus.visibility = View.GONE
                        
                        val errorMessage = when {
                            state.error.message?.contains("File") == true -> "文件错误: ${state.error.message}"
                            state.error.message?.contains("Network") == true -> "网络错误，请检查网络连接"
                            state.error.message?.contains("API") == true -> "API错误，请检查设置"
                            else -> "转写失败: ${state.error.message ?: "未知错误"}"
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        
                        // Switch back to waveform view
                        binding.waveformView.visibility = View.VISIBLE
                        binding.transcriptionScroll.visibility = View.GONE
                        showingTranscription = false
                    }
                    
                    is TranscriptionState.Cancelled -> {
                        binding.transcriptionProgress.visibility = View.GONE
                        binding.transcriptionStatus.visibility = View.GONE
                        Toast.makeText(requireContext(), "转写已取消", Toast.LENGTH_SHORT).show()
                        
                        // Switch back to waveform view
                        binding.waveformView.visibility = View.VISIBLE
                        binding.transcriptionScroll.visibility = View.GONE
                        showingTranscription = false
                    }
                }
            }
        }
        
        // Observe hasTranscription to show/hide toggle button
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasTranscription.collect { hasTranscription ->
                if (hasTranscription) {
                    binding.toggleViewButton.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun loadExistingTranscription() {
        val recordingId = arguments?.getLong("recordingId", 0L).toString()
        val filePath = arguments?.getString("filePath") ?: return
        val file = File(filePath)
        
        if (file.exists()) {
            viewModel.loadExistingTranscription(recordingId, file)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSeekBarRunnable)
        audioPlayer.release()
        _binding = null
    }
}
