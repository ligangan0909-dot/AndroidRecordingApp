package com.example.recordingapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.recordingapp.data.model.SpeakerSegment
import com.example.recordingapp.data.model.Task
import com.example.recordingapp.data.model.TranscriptionResult

/**
 * Room database for storing transcription results and related data.
 * 
 * Database version: 1
 * Entities: TranscriptionResult, SpeakerSegment, Task
 */
@Database(
    entities = [
        TranscriptionResult::class,
        SpeakerSegment::class,
        Task::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TranscriptionDatabase : RoomDatabase() {
    
    /**
     * DAO for transcription result operations.
     */
    abstract fun transcriptionDao(): TranscriptionDao
    
    /**
     * DAO for speaker segment operations.
     */
    abstract fun speakerSegmentDao(): SpeakerSegmentDao
    
    /**
     * DAO for task operations.
     */
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: TranscriptionDatabase? = null
        
        /**
         * Get singleton instance of the database.
         * 
         * @param context Application context
         * @return TranscriptionDatabase instance
         */
        fun getInstance(context: Context): TranscriptionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TranscriptionDatabase::class.java,
                    "transcription_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
