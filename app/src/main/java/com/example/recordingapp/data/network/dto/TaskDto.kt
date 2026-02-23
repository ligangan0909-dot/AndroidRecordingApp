package com.example.recordingapp.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Data transfer object for task information extracted from transcription.
 * Represents an action item or to-do identified in the audio content.
 */
data class TaskDto(
    @SerializedName("description")
    val description: String,
    
    @SerializedName("assignee")
    val assignee: String?,
    
    @SerializedName("deadline")
    val deadline: String?,
    
    @SerializedName("priority")
    val priority: String,
    
    @SerializedName("source_timestamp_ms")
    val sourceTimestampMs: Long
)
