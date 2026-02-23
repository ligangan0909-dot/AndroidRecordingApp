package com.example.recordingapp.data.database

import androidx.room.*
import com.example.recordingapp.data.model.TranscriptionResult

/**
 * Data Access Object for TranscriptionResult entity.
 * Provides CRUD operations for transcription results.
 */
@Dao
interface TranscriptionDao {
    
    /**
     * Insert a new transcription result.
     * If a transcription with the same recordingId exists, it will be replaced.
     * 
     * @param transcription The transcription result to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transcription: TranscriptionResult)
    
    /**
     * Insert multiple transcription results.
     * 
     * @param transcriptions List of transcription results to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transcriptions: List<TranscriptionResult>)
    
    /**
     * Update an existing transcription result.
     * 
     * @param transcription The transcription result to update
     */
    @Update
    suspend fun update(transcription: TranscriptionResult)
    
    /**
     * Delete a transcription result.
     * 
     * @param transcription The transcription result to delete
     */
    @Delete
    suspend fun delete(transcription: TranscriptionResult)
    
    /**
     * Delete a transcription by recording ID.
     * 
     * @param recordingId The recording ID of the transcription to delete
     */
    @Query("DELETE FROM transcriptions WHERE recordingId = :recordingId")
    suspend fun deleteByRecordingId(recordingId: String)
    
    /**
     * Get a transcription result by recording ID.
     * 
     * @param recordingId The recording ID to search for
     * @return The transcription result, or null if not found
     */
    @Query("SELECT * FROM transcriptions WHERE recordingId = :recordingId")
    suspend fun getById(recordingId: String): TranscriptionResult?
    
    /**
     * Get all transcription results ordered by creation date (newest first).
     * 
     * @return List of all transcription results
     */
    @Query("SELECT * FROM transcriptions ORDER BY created_at DESC")
    suspend fun getAll(): List<TranscriptionResult>
    
    /**
     * Get transcriptions created after a specific timestamp.
     * 
     * @param timestamp The timestamp to filter by
     * @return List of transcription results created after the timestamp
     */
    @Query("SELECT * FROM transcriptions WHERE created_at > :timestamp ORDER BY created_at DESC")
    suspend fun getAfterTimestamp(timestamp: Long): List<TranscriptionResult>
    
    /**
     * Check if a transcription exists for a recording ID.
     * 
     * @param recordingId The recording ID to check
     * @return True if a transcription exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM transcriptions WHERE recordingId = :recordingId)")
    suspend fun exists(recordingId: String): Boolean
    
    /**
     * Get the count of all transcriptions.
     * 
     * @return Total number of transcriptions
     */
    @Query("SELECT COUNT(*) FROM transcriptions")
    suspend fun getCount(): Int
}
