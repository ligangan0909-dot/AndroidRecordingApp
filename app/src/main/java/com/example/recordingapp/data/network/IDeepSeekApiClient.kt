package com.example.recordingapp.data.network

import com.example.recordingapp.data.network.dto.TranscriptionResponse
import java.io.File

/**
 * Interface for DeepSeek API client operations.
 * 
 * Provides methods for audio transcription with configurable features:
 * - Speaker diarization: Identifies different speakers in the audio
 * - Task extraction: Automatically extracts action items from transcription
 * - Summary generation: Creates a concise summary of the content
 * 
 * All operations return Result<T> for explicit error handling.
 */
interface IDeepSeekApiClient {
    
    /**
     * Transcribes an audio file using the DeepSeek API.
     * 
     * This method uploads the audio file and requests transcription with optional features.
     * The operation is performed asynchronously using Kotlin coroutines.
     * 
     * @param audioFile The audio file to transcribe (must be WAV format, 44.1kHz, 16-bit, mono)
     * @param enableDiarization Enable speaker diarization to identify different speakers
     * @param enableTaskExtraction Enable automatic task/action item extraction
     * @param enableSummary Enable content summarization
     * @return Result containing TranscriptionResponse on success, or error on failure
     * 
     * Possible errors:
     * - NetworkError: Network connectivity issues
     * - ApiError: API returned an error response (4xx, 5xx)
     * - AuthError: Authentication failed (invalid API key)
     * - FileError: File not found, unreadable, or invalid format
     * - TimeoutError: Request timed out
     */
    suspend fun transcribeAudio(
        audioFile: File,
        enableDiarization: Boolean = true,
        enableTaskExtraction: Boolean = true,
        enableSummary: Boolean = true
    ): Result<TranscriptionResponse>
    
    /**
     * Validates an API key by making a test request to the DeepSeek API.
     * 
     * This method can be used to verify that an API key is valid before saving it.
     * 
     * @param apiKey The API key to validate
     * @return Result containing true if valid, false if invalid, or error on failure
     */
    suspend fun validateApiKey(apiKey: String): Result<Boolean>
    
    /**
     * Cancels any in-progress transcription operation.
     * 
     * This method should be called when the user wants to abort a transcription.
     * It will terminate the network request and clean up resources.
     */
    fun cancelTranscription()
}
