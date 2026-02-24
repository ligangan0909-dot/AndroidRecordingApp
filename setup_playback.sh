#!/bin/bash

# 创建 playback 包目录
mkdir -p app/src/main/java/com/example/recordingapp/ui/playback

# 创建 fragment_playback.xml
cat > app/src/main/res/layout/fragment_playback.xml << 'XML_END'
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white">

    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="0dp"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/white"
        android:elevation="4dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:navigationIcon="?attr/homeAsUpIndicator"
        app:title="播放"
        app:titleTextColor="@color/black" />

    <TextView
        android:id="@+id/file_name"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="24dp"
        android:textColor="@color/black"
        android:textSize="18sp"
        android:textStyle="bold"
        android:maxLines="2"
        android:ellipsize="end"
        app:layout_constraintTop_toBottomOf="@id/toolbar"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        tools:text="recording_2024_01_15.wav" />

    <FrameLayout
        android:id="@+id/content_container"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginStart="24dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="24dp"
        android:layout_marginBottom="16dp"
        app:layout_constraintTop_toBottomOf="@id/file_name"
        app:layout_constraintBottom_toTopOf="@id/playback_controls"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <com.example.recordingapp.ui.views.WaveformView
            android:id="@+id/waveform_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:visibility="visible" />

        <ScrollView
            android:id="@+id/transcription_scroll"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:visibility="gone">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:id="@+id/transcription_text"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:textColor="@color/black"
                    android:textSize="16sp"
                    android:lineSpacingExtra="4dp"
                    tools:text="这是转写文本内容..." />

                <ProgressBar
                    android:id="@+id/transcription_progress"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:layout_marginTop="32dp"
                    android:visibility="gone" />

                <TextView
                    android:id="@+id/transcription_status"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:layout_marginTop="8dp"
                    android:textColor="#666666"
                    android:textSize="14sp"
                    android:visibility="gone"
                    tools:text="正在转写中..." />
            </LinearLayout>
        </ScrollView>
    </FrameLayout>

    <LinearLayout
        android:id="@+id/playback_controls"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp"
        app:layout_constraintBottom_toTopOf="@id/action_buttons"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <SeekBar
            android:id="@+id/seek_bar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:progressTint="@color/orange_primary"
            android:thumbTint="@color/orange_primary" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="4dp">

            <TextView
                android:id="@+id/current_time"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:textColor="#666666"
                android:textSize="12sp"
                tools:text="00:00" />

            <TextView
                android:id="@+id/total_time"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="#666666"
                android:textSize="12sp"
                tools:text="05:23" />
        </LinearLayout>

        <ImageButton
            android:id="@+id/play_pause_button"
            android:layout_width="64dp"
            android:layout_height="64dp"
            android:layout_gravity="center"
            android:layout_marginTop="16dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@android:drawable/ic_media_play"
            android:tint="@color/orange_primary"
            android:contentDescription="播放/暂停" />
    </LinearLayout>

    <LinearLayout
        android:id="@+id/action_buttons"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp"
        android:gravity="center"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <Button
            android:id="@+id/transcribe_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginEnd="8dp"
            android:text="转写"
            android:textColor="@color/white"
            android:backgroundTint="@color/orange_primary" />

        <Button
            android:id="@+id/toggle_view_button"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="8dp"
            android:text="查看转写"
            android:textColor="@color/orange_primary"
            android:backgroundTint="@color/white"
            android:visibility="gone"
            style="?attr/materialButtonOutlinedStyle" />
    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
XML_END

echo "✅ Created fragment_playback.xml"

# 创建 PlaybackFragment.kt (简化版本，先不依赖 ViewModel)
cat > app/src/main/java/com/example/recordingapp/ui/playback/PlaybackFragment.kt << 'KOTLIN_END'
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
import androidx.navigation.fragment.findNavController
import com.example.recordingapp.R
import com.example.recordingapp.audio.AudioPlayer
import com.example.recordingapp.databinding.FragmentPlaybackBinding
import java.io.File

class PlaybackFragment : Fragment() {
    private var _binding: FragmentPlaybackBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var audioPlayer: AudioPlayer
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var showingTranscription = false
    
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
            Toast.makeText(requireContext(), "转写功能开发中", Toast.LENGTH_SHORT).show()
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
KOTLIN_END

echo "✅ Created PlaybackFragment.kt"
echo "✅ Setup complete!"
