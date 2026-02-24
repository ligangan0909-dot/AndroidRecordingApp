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
