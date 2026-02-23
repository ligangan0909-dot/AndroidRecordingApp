package com.example.recordingapp.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Data transfer object for the complete transcription response from DeepSeek API.
 * Contains the transcribed text, speaker segments, extracted tasks, and summary.
 */
data class TranscriptionResponse(
    @SerializedName("transcription_id")
    val transcriptionId: String,
    
    @SerializedName("full_text")
    val fullText: String,
    
    @SerializedName("language")
    val language: String,
    
    @SerializedName("duration_ms")
    val durationMs: Long,
    
    @SerializedName("speaker_segments")
    val speakerSegments: List<SpeakerSegmentDto>,
    
    @SerializedName("tasks")
    val tasks: List<TaskDto>,
    
    @SerializedName("summary")
    val summary: String
)
