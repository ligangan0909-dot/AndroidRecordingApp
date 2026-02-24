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
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
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
        
        return RecordingFile(
            file = file,
            name = file.name,
            duration = duration,
            size = size,
            date = date
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
