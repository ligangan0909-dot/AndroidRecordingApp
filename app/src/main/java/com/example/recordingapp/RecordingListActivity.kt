package com.example.recordingapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recordingapp.audio.AudioPlayer
import com.example.recordingapp.databinding.ActivityRecordingListBinding
import com.example.recordingapp.databinding.ItemRecordingBinding
import com.example.recordingapp.model.Recording
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 录音列表Activity
 * 显示所有录音文件，支持播放、删除和转写操作
 */
class RecordingListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordingListBinding
    private val recordings = mutableListOf<Recording>()
    private lateinit var adapter: RecordingAdapter
    private val audioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadRecordings()
    }

    private fun setupRecyclerView() {
        adapter = RecordingAdapter(
            recordings = recordings,
            onPlayClick = { recording -> playRecording(recording) },
            onDeleteClick = { recording -> showDeleteConfirmation(recording) },
            onTranscribeClick = { recording -> showTranscriptionPlaceholder(recording) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecordingListActivity)
            adapter = this@RecordingListActivity.adapter
        }
    }

    private fun loadRecordings() {
        val recordingsDir = File(filesDir, "recordings")
        if (!recordingsDir.exists()) {
            showEmptyState()
            return
        }

        val files = recordingsDir.listFiles { file -> file.extension == "wav" }
        if (files.isNullOrEmpty()) {
            showEmptyState()
            return
        }

        recordings.clear()
        files.forEach { file ->
            recordings.add(
                Recording(
                    file = file,
                    name = file.nameWithoutExtension,
                    duration = 0L, // 实际应用中可以读取WAV文件头获取时长
                    timestamp = file.lastModified()
                )
            )
        }

        recordings.sortByDescending { it.timestamp }
        adapter.notifyDataSetChanged()
        
        binding.tvEmptyState.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun playRecording(recording: Recording) {
        if (audioPlayer.isPlaying()) {
            audioPlayer.stop()
        } else {
            audioPlayer.play(recording.file)
            Toast.makeText(this, "播放: ${recording.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation(recording: Recording) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.confirm) { _, _ ->
                deleteRecording(recording)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteRecording(recording: Recording) {
        if (recording.file.delete()) {
            recordings.remove(recording)
            adapter.notifyDataSetChanged()
            
            if (recordings.isEmpty()) {
                showEmptyState()
            }
            
            Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTranscriptionPlaceholder(recording: Recording) {
        AlertDialog.Builder(this)
            .setTitle("转写功能")
            .setMessage(R.string.transcription_placeholder)
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}

/**
 * 录音列表适配器
 */
class RecordingAdapter(
    private val recordings: List<Recording>,
    private val onPlayClick: (Recording) -> Unit,
    private val onDeleteClick: (Recording) -> Unit,
    private val onTranscribeClick: (Recording) -> Unit
) : RecyclerView.Adapter<RecordingAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRecordingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recording = recordings[position]
        
        holder.binding.apply {
            tvFileName.text = recording.name
            
            // 格式化时间戳
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            tvDuration.text = dateFormat.format(Date(recording.timestamp))
            
            btnPlay.setOnClickListener { onPlayClick(recording) }
            btnDelete.setOnClickListener { onDeleteClick(recording) }
            btnTranscribe.setOnClickListener { onTranscribeClick(recording) }
        }
    }

    override fun getItemCount() = recordings.size
}
