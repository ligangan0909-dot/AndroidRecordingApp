package com.example.recordingapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recordingapp.R
import com.example.recordingapp.model.Recording
import com.example.recordingapp.model.TranscriptionStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying recording list with transcription status
 */
class RecordingListAdapter(
    private var recordings: List<Recording>,
    private val onRecordingClick: (Recording) -> Unit,
    private val onTranscribeClick: (Recording) -> Unit,
    private val onViewTranscriptionClick: (Recording) -> Unit
) : RecyclerView.Adapter<RecordingListAdapter.RecordingViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recording, parent, false)
        return RecordingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val recording = recordings[position]
        holder.bind(recording)
    }

    override fun getItemCount(): Int = recordings.size

    fun updateRecordings(newRecordings: List<Recording>) {
        recordings = newRecordings
        notifyDataSetChanged()
    }

    inner class RecordingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvRecordingName)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvRecordingDuration)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvRecordingTimestamp)
        private val ivTranscriptionStatus: ImageView = itemView.findViewById(R.id.ivTranscriptionStatus)
        private val btnTranscribe: View = itemView.findViewById(R.id.btnTranscribe)
        private val btnViewTranscription: View = itemView.findViewById(R.id.btnViewTranscription)

        fun bind(recording: Recording) {
            tvName.text = recording.name
            tvDuration.text = formatDuration(recording.duration)
            tvTimestamp.text = dateFormat.format(Date(recording.timestamp))

            // Update transcription status icon
            when (recording.transcriptionStatus) {
                TranscriptionStatus.NONE -> {
                    ivTranscriptionStatus.visibility = View.GONE
                    btnTranscribe.visibility = View.VISIBLE
                    btnViewTranscription.visibility = View.GONE
                }
                TranscriptionStatus.PROCESSING -> {
                    ivTranscriptionStatus.visibility = View.VISIBLE
                    ivTranscriptionStatus.setImageResource(R.drawable.ic_processing)
                    btnTranscribe.visibility = View.GONE
                    btnViewTranscription.visibility = View.GONE
                }
                TranscriptionStatus.COMPLETED -> {
                    ivTranscriptionStatus.visibility = View.VISIBLE
                    ivTranscriptionStatus.setImageResource(R.drawable.ic_completed)
                    btnTranscribe.visibility = View.GONE
                    btnViewTranscription.visibility = View.VISIBLE
                }
                TranscriptionStatus.FAILED -> {
                    ivTranscriptionStatus.visibility = View.VISIBLE
                    ivTranscriptionStatus.setImageResource(R.drawable.ic_failed)
                    btnTranscribe.visibility = View.VISIBLE
                    btnViewTranscription.visibility = View.GONE
                }
            }

            // Click listeners
            itemView.setOnClickListener { onRecordingClick(recording) }
            btnTranscribe.setOnClickListener { onTranscribeClick(recording) }
            btnViewTranscription.setOnClickListener { onViewTranscriptionClick(recording) }
        }

        private fun formatDuration(durationMs: Long): String {
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            val hours = (durationMs / (1000 * 60 * 60))
            
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }
}
