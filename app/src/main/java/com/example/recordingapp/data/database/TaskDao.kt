package com.example.recordingapp.data.database

import androidx.room.*
import com.example.recordingapp.data.model.Task
import com.example.recordingapp.data.model.TaskPriority

/**
 * Data Access Object for Task entity.
 * Provides query methods with filtering capabilities for tasks.
 */
@Dao
interface TaskDao {
    
    /**
     * Insert a new task.
     * 
     * @param task The task to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)
    
    /**
     * Insert multiple tasks.
     * 
     * @param tasks List of tasks to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)
    
    /**
     * Update an existing task.
     * 
     * @param task The task to update
     */
    @Update
    suspend fun update(task: Task)
    
    /**
     * Delete a task.
     * 
     * @param task The task to delete
     */
    @Delete
    suspend fun delete(task: Task)
    
    /**
     * Get all tasks for a specific recording, ordered by source timestamp.
     * 
     * @param recordingId The recording ID to filter by
     * @return List of tasks for the recording
     */
    @Query("SELECT * FROM tasks WHERE recordingId = :recordingId ORDER BY source_timestamp_ms ASC")
    suspend fun getByRecordingId(recordingId: String): List<Task>
    
    /**
     * Get a task by its ID.
     * 
     * @param taskId The task ID to search for
     * @return The task, or null if not found
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: String): Task?
    
    /**
     * Get incomplete tasks for a recording.
     * 
     * @param recordingId The recording ID to filter by
     * @return List of incomplete tasks
     */
    @Query("SELECT * FROM tasks WHERE recordingId = :recordingId AND is_completed = 0 ORDER BY source_timestamp_ms ASC")
    suspend fun getIncompleteByRecordingId(recordingId: String): List<Task>
    
    /**
     * Get completed tasks for a recording.
     * 
     * @param recordingId The recording ID to filter by
     * @return List of completed tasks
     */
    @Query("SELECT * FROM tasks WHERE recordingId = :recordingId AND is_completed = 1 ORDER BY source_timestamp_ms ASC")
    suspend fun getCompletedByRecordingId(recordingId: String): List<Task>
    
    /**
     * Get tasks by priority for a recording.
     * 
     * @param recordingId The recording ID to filter by
     * @param priority The priority level to filter by
     * @return List of tasks with the specified priority
     */
    @Query("SELECT * FROM tasks WHERE recordingId = :recordingId AND priority = :priority ORDER BY source_timestamp_ms ASC")
    suspend fun getByPriority(recordingId: String, priority: TaskPriority): List<Task>
    
    /**
     * Get tasks assigned to a specific person.
     * 
     * @param recordingId The recording ID to filter by
     * @param assignee The assignee name to filter by
     * @return List of tasks assigned to the person
     */
    @Query("SELECT * FROM tasks WHERE recordingId = :recordingId AND assignee = :assignee ORDER BY source_timestamp_ms ASC")
    suspend fun getByAssignee(recordingId: String, assignee: String): List<Task>
    
    /**
     * Update the completion status of a task.
     * 
     * @param taskId The task ID to update
     * @param isCompleted The new completion status
     */
    @Query("UPDATE tasks SET is_completed = :isCompleted WHERE id = :taskId")
    suspend fun updateCompleted(taskId: String, isCompleted: Boolean)
    
    /**
     * Delete all tasks for a specific recording.
     * This is automatically handled by the foreign key cascade delete,
     * but provided for explicit deletion if needed.
     * 
     * @param recordingId The recording ID to delete tasks for
     */
    @Query("DELETE FROM tasks WHERE recordingId = :recordingId")
    suspend fun deleteByRecordingId(recordingId: String)
    
    /**
     * Get the count of tasks for a recording.
     * 
     * @param recordingId The recording ID to count tasks for
     * @return Number of tasks
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE recordingId = :recordingId")
    suspend fun getCountByRecordingId(recordingId: String): Int
    
    /**
     * Get the count of incomplete tasks for a recording.
     * 
     * @param recordingId The recording ID to count incomplete tasks for
     * @return Number of incomplete tasks
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE recordingId = :recordingId AND is_completed = 0")
    suspend fun getIncompleteCountByRecordingId(recordingId: String): Int
    
    /**
     * Get all incomplete tasks across all recordings.
     * 
     * @return List of all incomplete tasks
     */
    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY source_timestamp_ms ASC")
    suspend fun getAllIncomplete(): List<Task>
    
    /**
     * Get high priority incomplete tasks for a recording.
     * 
     * @param recordingId The recording ID to filter by
     * @return List of high priority incomplete tasks
     */
    @Query("SELECT * FROM tasks WHERE recordingId = :recordingId AND is_completed = 0 AND priority = 'HIGH' ORDER BY source_timestamp_ms ASC")
    suspend fun getHighPriorityIncomplete(recordingId: String): List<Task>
}
