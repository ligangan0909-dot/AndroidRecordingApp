package com.example.recordingapp.data.network

import com.example.recordingapp.data.network.dto.TranscriptionResponse
import java.io.File

/**
 * Abstract interface for transcription API clients.
 * 
 * This interface allows the app to support multiple transcription service providers
 * (e.g., OpenAI Whisper, Doubao/Volcengine, etc.) with a unified API.
 */
interface ITranscriptionApiClient {
    
    /**
     * Transcribes an audio file using the provider's API.
     * 
     * @param audioFile The audio file to transcribe
     * @param enableDiarization Enable speaker diarization (if supported by provider)
     * @param enableTaskExtraction Enable automatic task extraction (if supported)
     * @param enableSummary Enable content summarization (if supported)
     * @return Result containing TranscriptionResponse on success, or error on failure
     */
    suspend fun transcribeAudio(
        audioFile: File,
        enableDiarization: Boolean,
        enableTaskExtraction: Boolean,
        enableSummary: Boolean
    ): Result<TranscriptionResponse>
    
    /**
     * Validates the API credentials by making a test request.
     * 
     * @return Result containing true if valid, false if invalid, or error on failure
     */
    suspend fun validateCredentials(): Result<Boolean>
    
    /**
     * Cancels any in-progress transcription operation.
     */
    fun cancelTranscription()
    
    /**
     * Returns the name of the service provider (e.g., "OpenAI", "Doubao")
     */
    fun getProviderName(): String
}
