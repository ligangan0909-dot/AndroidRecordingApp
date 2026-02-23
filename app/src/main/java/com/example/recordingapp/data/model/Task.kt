package com.example.recordingapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Task entity representing an extracted action item from transcription.
 * 
 * Validation rules:
 * - description cannot be empty
 * - sourceTimestampMs >= 0
 * - deadline if present must be a valid ISO 8601 date format
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TranscriptionResult::class,
            parentColumns = ["recordingId"],
            childColumns = ["recordingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recordingId"]),
        Index(value = ["is_completed"]),
        Index(value = ["priority"])
    ]
)
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recordingId: String,
    val description: String,
    val assignee: String?,
    val deadline: String?,
    val priority: TaskPriority,
    @ColumnInfo(name = "source_timestamp_ms")
    val sourceTimestampMs: Long,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
)

/**
 * Priority levels for tasks.
 */
enum class TaskPriority {
    HIGH, MEDIUM, LOW
}
