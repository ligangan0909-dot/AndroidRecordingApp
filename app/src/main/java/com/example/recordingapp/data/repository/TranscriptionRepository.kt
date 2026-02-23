package com.example.recordingapp.data.repository

import android.util.Log
import com.example.recordingapp.data.database.TranscriptionDao
import com.example.recordingapp.data.model.TranscriptionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of TranscriptionRepository for managing transcription data.
 * 
 * This repository provides a clean abstraction over the Room database layer,
 * handling all database operations on the IO dispatcher for thread safety.
 * 
 * Features:
 * - Save/retrieve transcription results
 * - Query all transcriptions
 * - Delete transcriptions
 * - Check transcription existence
 * 
 * All operations are performed on Dispatchers.IO to avoid blocking the main thread.
 * 
 * @param transcriptionDao The DAO for database operations
 */
class TranscriptionRepository(
    private val transcriptionDao: TranscriptionDao
) : ITranscriptionRepository {
    
    /**
     * Saves a transcription result to the database.
     * 
     * @param transcription The transcription result to save
     * @return Result.success if saved successfully, Result.failure otherwise
     */
    override suspend fun saveTranscription(transcription: TranscriptionResult): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                transcriptionDao.insert(transcription)
                Log.d(TAG, "Transcription saved: ${transcription.recordingId}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save transcription: ${transcription.recordingId}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Retrieves a transcription result by recording ID.
     * 
     * @param recordingId The recording ID to search for
     * @return The transcription result, or null if not found
     */
    override suspend fun getTranscription(recordingId: String): TranscriptionResult? {
        return withContext(Dispatchers.IO) {
            try {
                val result = transcriptionDao.getById(recordingId)
                if (result != null) {
                    Log.d(TAG, "Transcription retrieved: $recordingId")
                } else {
                    Log.d(TAG, "No transcription found for: $recordingId")
                }
                result
            } catch (e: Exception) {
                Log.e(TAG, "Failed to retrieve transcription: $recordingId", e)
                null
            }
        }
    }
    
    /**
     * Retrieves all transcription results ordered by creation date (newest first).
     * 
     * @return List of all transcription results
     */
    override suspend fun getAllTranscriptions(): List<TranscriptionResult> {
        return withContext(Dispatchers.IO) {
            try {
                val results = transcriptionDao.getAll()
                Log.d(TAG, "Retrieved ${results.size} transcriptions")
                results
            } catch (e: Exception) {
                Log.e(TAG, "Failed to retrieve all transcriptions", e)
                emptyList()
            }
        }
    }
    
    /**
     * Deletes a transcription result by recording ID.
     * 
     * @param recordingId The recording ID of the transcription to delete
     * @return Result.success if deleted successfully, Result.failure otherwise
     */
    override suspend fun deleteTranscription(recordingId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                transcriptionDao.deleteByRecordingId(recordingId)
                Log.d(TAG, "Transcription deleted: $recordingId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete transcription: $recordingId", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Checks if a transcription exists for a recording ID.
     * 
     * @param recordingId The recording ID to check
     * @return True if a transcription exists, false otherwise
     */
    override suspend fun hasTranscription(recordingId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                transcriptionDao.exists(recordingId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check transcription existence: $recordingId", e)
                false
            }
        }
    }
    
    companion object {
        private const val TAG = "TranscriptionRepository"
    }
}
