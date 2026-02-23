package com.example.recordingapp.data.database

import androidx.room.*
import com.example.recordingapp.data.model.SpeakerSegment

/**
 * Data Access Object for SpeakerSegment entity.
 * Provides query methods for speaker segments.
 */
@Dao
interface SpeakerSegmentDao {
    
    /**
     * Insert a new speaker segment.
     * 
     * @param segment The speaker segment to insert
     * @return The row ID of the inserted segment
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(segment: SpeakerSegment): Long
    
    /**
     * Insert multiple speaker segments.
     * 
     * @param segments List of speaker segments to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(segments: List<SpeakerSegment>)
    
    /**
     * Update an existing speaker segment.
     * 
     * @param segment The speaker segment to update
     */
    @Update
    suspend fun update(segment: SpeakerSegment)
    
    /**
     * Delete a speaker segment.
     * 
     * @param segment The speaker segment to delete
     */
    @Delete
    suspend fun delete(segment: SpeakerSegment)
    
    /**
     * Get all speaker segments for a specific recording, ordered by start time.
     * 
     * @param recordingId The recording ID to filter by
     * @return List of speaker segments for the recording
     */
    @Query("SELECT * FROM speaker_segments WHERE recordingId = :recordingId ORDER BY start_time_ms ASC")
    suspend fun getByRecordingId(recordingId: String): List<SpeakerSegment>
    
    /**
     * Get speaker segments for a specific speaker in a recording.
     * 
     * @param recordingId The recording ID to filter by
     * @param speakerId The speaker ID to filter by
     * @return List of speaker segments for the specific speaker
     */
    @Query("SELECT * FROM speaker_segments WHERE recordingId = :recordingId AND speaker_id = :speakerId ORDER BY start_time_ms ASC")
    suspend fun getBySpeaker(recordingId: String, speakerId: String): List<SpeakerSegment>
    
    /**
     * Get speaker segments within a time range.
     * 
     * @param recordingId The recording ID to filter by
     * @param startTime The start time in milliseconds
     * @param endTime The end time in milliseconds
     * @return List of speaker segments within the time range
     */
    @Query("SELECT * FROM speaker_segments WHERE recordingId = :recordingId AND start_time_ms >= :startTime AND end_time_ms <= :endTime ORDER BY start_time_ms ASC")
    suspend fun getByTimeRange(recordingId: String, startTime: Long, endTime: Long): List<SpeakerSegment>
    
    /**
     * Get distinct speaker IDs for a recording.
     * 
     * @param recordingId The recording ID to filter by
     * @return List of unique speaker IDs
     */
    @Query("SELECT DISTINCT speaker_id FROM speaker_segments WHERE recordingId = :recordingId")
    suspend fun getSpeakerIds(recordingId: String): List<String>
    
    /**
     * Delete all speaker segments for a specific recording.
     * This is automatically handled by the foreign key cascade delete,
     * but provided for explicit deletion if needed.
     * 
     * @param recordingId The recording ID to delete segments for
     */
    @Query("DELETE FROM speaker_segments WHERE recordingId = :recordingId")
    suspend fun deleteByRecordingId(recordingId: String)
    
    /**
     * Get the count of speaker segments for a recording.
     * 
     * @param recordingId The recording ID to count segments for
     * @return Number of speaker segments
     */
    @Query("SELECT COUNT(*) FROM speaker_segments WHERE recordingId = :recordingId")
    suspend fun getCountByRecordingId(recordingId: String): Int
    
    /**
     * Get speaker segments with confidence above a threshold.
     * 
     * @param recordingId The recording ID to filter by
     * @param minConfidence Minimum confidence threshold (0.0 to 1.0)
     * @return List of speaker segments with confidence >= minConfidence
     */
    @Query("SELECT * FROM speaker_segments WHERE recordingId = :recordingId AND confidence >= :minConfidence ORDER BY start_time_ms ASC")
    suspend fun getByMinConfidence(recordingId: String, minConfidence: Float): List<SpeakerSegment>
}
