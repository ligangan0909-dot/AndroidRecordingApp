package com.example.recordingapp.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Data transfer object for Whisper API transcription response.
 * Whisper API returns a simple response with just the transcribed text.
 */
data class WhisperTranscriptionResponse(
    @SerializedName("text")
    val text: String
)
