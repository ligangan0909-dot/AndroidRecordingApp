package com.example.recordingapp.data.repository

import com.example.recordingapp.data.model.TranscriptionResult

/**
 * Repository interface for managing transcription data persistence.
 * 
 * Provides a clean abstraction over the database layer for transcription operations.
 */
interface ITranscriptionRepository {
    
    /**
     * Saves a transcription result to the database.
     * If a transcription with the same recordingId exists, it will be replaced.
     * 
     * @param transcription The transcription result to save
     * @return Result.success if saved successfully, Result.failure otherwise
     */
    suspend fun saveTranscription(transcription: TranscriptionResult): Result<Unit>
    
    /**
     * Retrieves a transcription result by recording ID.
     * 
     * @param recordingId The recording ID to search for
     * @return The transcription result, or null if not found
     */
    suspend fun getTranscription(recordingId: String): TranscriptionResult?
    
    /**
     * Retrieves all transcription results ordered by creation date (newest first).
     * 
     * @return List of all transcription results
     */
    suspend fun getAllTranscriptions(): List<TranscriptionResult>
    
    /**
     * Deletes a transcription result by recording ID.
     * 
     * @param recordingId The recording ID of the transcription to delete
     * @return Result.success if deleted successfully, Result.failure otherwise
     */
    suspend fun deleteTranscription(recordingId: String): Result<Unit>
    
    /**
     * Checks if a transcription exists for a recording ID.
     * 
     * @param recordingId The recording ID to check
     * @return True if a transcription exists, false otherwise
     */
    suspend fun hasTranscription(recordingId: String): Boolean
}
