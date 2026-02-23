package com.example.recordingapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * SpeakerSegment entity representing a segment of speech by a specific speaker.
 * 
 * Validation rules:
 * - startTimeMs < endTimeMs
 * - startTimeMs >= 0
 * - text cannot be empty
 * - confidence must be between 0.0 and 1.0
 */
@Entity(
    tableName = "speaker_segments",
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
        Index(value = ["start_time_ms"])
    ]
)
data class SpeakerSegment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordingId: String,
    @ColumnInfo(name = "speaker_id")
    val speakerId: String,
    @ColumnInfo(name = "start_time_ms")
    val startTimeMs: Long,
    @ColumnInfo(name = "end_time_ms")
    val endTimeMs: Long,
    val text: String,
    val confidence: Float
)
