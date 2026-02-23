package com.example.recordingapp.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Data transfer object for speaker segment information from DeepSeek API.
 * Represents a segment of audio attributed to a specific speaker.
 */
data class SpeakerSegmentDto(
    @SerializedName("speaker_id")
    val speakerId: String,
    
    @SerializedName("start_time_ms")
    val startTimeMs: Long,
    
    @SerializedName("end_time_ms")
    val endTimeMs: Long,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("confidence")
    val confidence: Float
)
