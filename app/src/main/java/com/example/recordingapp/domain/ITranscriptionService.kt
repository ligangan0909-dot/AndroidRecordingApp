package com.example.recordingapp.domain

import com.example.recordingapp.data.model.TranscriptionResult
import com.example.recordingapp.data.model.TranscriptionState
import kotlinx.coroutines.flow.Flow

/**
 * Transcription service interface
 * Coordinates the transcription flow, manages audio upload and result processing
 */
interface ITranscriptionService {
    /**
     * Start transcription for a recording
     * @param recordingId Unique identifier for the recording
     * @param audioFilePath Path to the audio file
     * @return Flow of transcription states
     */
    fun transcribe(
        recordingId: String,
        audioFilePath: String
    ): Flow<TranscriptionState>
    
    /**
     * Cancel an in-progress transcription
     * @param recordingId Recording ID to cancel
     */
    fun cancelTranscription(recordingId: String)
    
    /**
     * Get cached transcription result
     * @param recordingId Recording ID
     * @return Cached result or null if not found
     */
    suspend fun getCachedResult(recordingId: String): TranscriptionResult?
}
