package com.example.recordingapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * TranscriptionResult entity representing a completed transcription.
 * 
 * Validation rules:
 * - recordingId cannot be empty
 * - transcriptionId cannot be empty
 * - fullText cannot be empty
 * - durationMs must be greater than 0
 * - language must be a valid language code
 * - createdAt must be a valid timestamp
 */
@Entity(
    tableName = "transcriptions",
    indices = [
        Index(value = ["recordingId"], unique = true),
        Index(value = ["created_at"])
    ]
)
data class TranscriptionResult(
    @PrimaryKey
    val recordingId: String,
    val transcriptionId: String,
    @ColumnInfo(name = "full_text")
    val fullText: String,
    val summary: String,
    val language: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "is_cached")
    val isCached: Boolean = true
)
