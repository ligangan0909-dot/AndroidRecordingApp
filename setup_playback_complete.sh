#!/bin/bash

echo "🚀 开始设置播放功能..."

# 创建备份目录
mkdir -p backups
echo "📦 创建备份..."

# 备份所有要修改的文件
cp app/src/main/java/com/example/recordingapp/audio/AudioPlayer.kt backups/ 2>/dev/null
cp app/src/main/res/navigation/nav_graph.xml backups/ 2>/dev/null
cp app/src/main/java/com/example/recordingapp/ui/files/RecordingFile.kt backups/ 2>/dev/null
cp app/src/main/java/com/example/recordingapp/ui/files/FilesFragment.kt backups/ 2>/dev/null
cp app/src/main/java/com/example/recordingapp/ui/files/RecordingFileAdapter.kt backups/ 2>/dev/null

echo "✅ 备份完成"

# 1. 更新 AudioPlayer.kt
echo "📝 更新 AudioPlayer.kt..."
cat > app/src/main/java/com/example/recordingapp/audio/AudioPlayer.kt << 'KOTLIN_END'
package com.example.recordingapp.audio

import android.media.MediaPlayer
import java.io.File

class AudioPlayer {
    var mediaPlayer: MediaPlayer? = null
        private set
    private var isPlayingState = false
    
    var onPlaybackComplete: (() -> Unit)? = null
    var onPlaybackProgress: ((Int, Int) -> Unit)? = null

    fun play(file: File) {
        if (isPlayingState) {
            stop()
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            setOnCompletionListener {
                isPlayingState = false
                onPlaybackComplete?.invoke()
            }
            start()
        }
        isPlayingState = true
    }

    fun pause() {
        mediaPlayer?.pause()
        isPlayingState = false
    }

    fun resume() {
        mediaPlayer?.start()
        isPlayingState = true
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlayingState = false
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun isPlaying(): Boolean {
        return isPlayingState
    }

    fun release() {
        stop()
    }
}
KOTLIN_END

# 2. 更新 nav_graph.xml
echo "📝 更新 nav_graph.xml..."
cat > app/src/main/res/navigation/nav_graph.xml << 'XML_END'
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/nav_recording">

    <fragment
        android:id="@+id/nav_recording"
        android:name="com.example.recordingapp.ui.recording.RecordingFragment"
        android:label="@string/nav_recording" />

    <fragment
        android:id="@+id/nav_files"
        android:name="com.example.recordingapp.ui.files.FilesFragment"
        android:label="@string/nav_files">
        <action
            android:id="@+id/action_nav_files_to_nav_playback"
            app:destination="@id/nav_playback" />
    </fragment>

    <fragment
        android:id="@+id/nav_profile"
        android:name="com.example.recordingapp.ui.profile.ProfileFragment"
        android:label="@string/nav_profile" />

    <fragment
        android:id="@+id/nav_playback"
        android:name="com.example.recordingapp.ui.playback.PlaybackFragment"
        android:label="播放">
        <argument
            android:name="recordingId"
            android:argType="long"
            android:defaultValue="0L" />
        <argument
            android:name="filePath"
            android:argType="string" />
    </fragment>
</navigation>
XML_END

# 3. 更新 RecordingFile.kt
echo "📝 更新 RecordingFile.kt..."
cat > app/src/main/java/com/example/recordingapp/ui/files/RecordingFile.kt << 'KOTLIN_END'
package com.example.recordingapp.ui.files

import java.io.File

data class RecordingFile(
    val id: Long = System.currentTimeMillis(),
    val file: File,
    val name: String,
    val duration: String,
    val size: String,
    val date: String,
    val hasTranscription: Boolean = false,
    var isSelected: Boolean = false
) {
    val displayName: String
        get() = name.removeSuffix(".wav")
    
    val transcriptionFile: File
        get() = File(file.parent, "${file.nameWithoutExtension}.txt")
}
KOTLIN_END

# 4. 更新 FilesFragment.kt
echo "📝 更新 FilesFragment.kt..."
cat > app/src/main/java/com/example/recordingapp/ui/files/FilesFragment.kt << 'KOTLIN_END'
package com.example.recordingapp.ui.files

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recordingapp.R
import com.example.recordingapp.databinding.FragmentFilesBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FilesFragment : Fragment() {
    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: RecordingFileAdapter
    private val recordingFiles = mutableListOf<RecordingFile>()
    private var isSelectionMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupMenu()
        setupSwipeToDelete()
        loadRecordings()
    }
    
    private fun setupRecyclerView() {
        adapter = RecordingFileAdapter(
            onItemClick = { file ->
                if (isSelectionMode) {
                    updateSelectionMode()
                } else {
                    // 导航到播放页面
                    val bundle = bundleOf(
                        "recordingId" to file.id,
                        "filePath" to file.file.absolutePath
                    )
                    findNavController().navigate(R.id.action_nav_files_to_nav_playback, bundle)
                }
            },
            onItemLongClick = { file ->
                if (!isSelectionMode) {
                    enterSelectionMode()
                    file.isSelected = true
                    adapter.notifyDataSetChanged()
                }
                true
            },
            onRenameClick = { file ->
                showRenameDialog(file)
            }
        )
        
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }
    
    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (isSelectionMode) {
                    menu.clear()
                    menuInflater.inflate(R.menu.menu_files_selection, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete -> {
                        deleteSelectedFiles()
                        true
                    }
                    R.id.action_select_all -> {
                        selectAllFiles()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val file = recordingFiles[position]
                showDeleteConfirmDialog(file, position)
            }
        })
        
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
    
    private fun loadRecordings() {
        val dir = requireContext().getExternalFilesDir(null)
        val files = dir?.listFiles { file -> file.extension == "wav" }
        
        recordingFiles.clear()
        files?.sortedByDescending { it.lastModified() }?.forEach { file ->
            recordingFiles.add(createRecordingFile(file))
        }
        
        adapter.submitList(recordingFiles.toList())
        updateEmptyView()
    }
    
    private fun createRecordingFile(file: File): RecordingFile {
        val duration = calculateDuration(file)
        val size = formatFileSize(file.length())
        val date = formatDate(file.lastModified())
        val hasTranscription = File(file.parent, "${file.nameWithoutExtension}.txt").exists()
        
        return RecordingFile(
            id = file.lastModified(),
            file = file,
            name = file.name,
            duration = duration,
            size = size,
            date = date,
            hasTranscription = hasTranscription
        )
    }
    
    private fun calculateDuration(file: File): String {
        val durationSeconds = (file.length() / (44100 * 2)).toInt()
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
    
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    private fun enterSelectionMode() {
        isSelectionMode = true
        adapter.isSelectionMode = true
        requireActivity().invalidateOptionsMenu()
    }
    
    private fun exitSelectionMode() {
        isSelectionMode = false
        adapter.isSelectionMode = false
        recordingFiles.forEach { it.isSelected = false }
        adapter.notifyDataSetChanged()
        requireActivity().invalidateOptionsMenu()
    }
    
    private fun updateSelectionMode() {
        val hasSelection = recordingFiles.any { it.isSelected }
        if (!hasSelection && isSelectionMode) {
            exitSelectionMode()
        }
    }
    
    private fun selectAllFiles() {
        recordingFiles.forEach { it.isSelected = true }
        adapter.notifyDataSetChanged()
    }
    
    private fun deleteSelectedFiles() {
        val selectedFiles = recordingFiles.filter { it.isSelected }
        if (selectedFiles.isEmpty()) return
        
        AlertDialog.Builder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定删除选中的 ${selectedFiles.size} 个文件？")
            .setPositiveButton("删除") { _, _ ->
                selectedFiles.forEach { it.file.delete() }
                loadRecordings()
                exitSelectionMode()
                Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showDeleteConfirmDialog(file: RecordingFile, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定删除 ${file.displayName}？")
            .setPositiveButton("删除") { _, _ ->
                file.file.delete()
                loadRecordings()
                Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消") { _, _ ->
                adapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position)
            }
            .show()
    }
    
    private fun showRenameDialog(file: RecordingFile) {
        val input = EditText(requireContext()).apply {
            setText(file.displayName)
            setSelection(text.length)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("重命名")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    renameFile(file, newName)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun renameFile(file: RecordingFile, newName: String) {
        val newFile = File(file.file.parent, "$newName.wav")
        if (file.file.renameTo(newFile)) {
            loadRecordings()
            Toast.makeText(requireContext(), "重命名成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "重命名失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateEmptyView() {
        if (recordingFiles.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
KOTLIN_END

# 5. 更新 RecordingFileAdapter.kt
echo "📝 更新 RecordingFileAdapter.kt..."
cat > app/src/main/java/com/example/recordingapp/ui/files/RecordingFileAdapter.kt << 'KOTLIN_END'
package com.example.recordingapp.ui.files

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recordingapp.databinding.ItemRecordingFileBinding

class RecordingFileAdapter(
    private val onItemClick: (RecordingFile) -> Unit,
    private val onItemLongClick: (RecordingFile) -> Boolean,
    private val onRenameClick: (RecordingFile) -> Unit
) : ListAdapter<RecordingFile, RecordingFileAdapter.ViewHolder>(DiffCallback()) {

    var isSelectionMode = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(private val binding: ItemRecordingFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecordingFile) {
            binding.fileName.text = item.displayName
            binding.fileInfo.text = "${item.duration} | ${item.size} | ${item.date}"
            
            // 显示转写标识
            binding.transcriptionBadge.visibility = if (item.hasTranscription) View.VISIBLE else View.GONE
            
            binding.checkbox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            binding.checkbox.isChecked = item.isSelected
            
            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    item.isSelected = !item.isSelected
                    binding.checkbox.isChecked = item.isSelected
                }
                onItemClick(item)
            }
            
            binding.root.setOnLongClickListener {
                onItemLongClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordingFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<RecordingFile>() {
        override fun areItemsTheSame(oldItem: RecordingFile, newItem: RecordingFile): Boolean {
            return oldItem.file.absolutePath == newItem.file.absolutePath
        }

        override fun areContentsTheSame(oldItem: RecordingFile, newItem: RecordingFile): Boolean {
            return oldItem == newItem
        }
    }
}
KOTLIN_END

echo ""
echo "✅ 所有文件更新完成！"
echo ""
echo "📋 更新摘要："
echo "  ✅ AudioPlayer.kt - 公开 mediaPlayer 访问"
echo "  ✅ nav_graph.xml - 添加播放页面导航"
echo "  ✅ RecordingFile.kt - 添加 ID 和转写状态"
echo "  ✅ FilesFragment.kt - 添加点击跳转到播放页面"
echo "  ✅ RecordingFileAdapter.kt - 显示转写标识"
echo ""
echo "💾 备份文件保存在 backups/ 目录"
echo ""
echo "🎉 设置完成！现在可以运行 ./gradlew build 来构建项目"
