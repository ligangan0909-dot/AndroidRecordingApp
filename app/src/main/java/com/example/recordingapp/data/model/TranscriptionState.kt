package com.example.recordingapp.data.model

/**
 * Sealed class hierarchy representing the state of a transcription operation.
 * Used to track progress and communicate status to the UI layer.
 */
sealed class TranscriptionState {
    /**
     * Initial idle state before transcription starts.
     */
    object Idle : TranscriptionState()
    
    /**
     * Audio file is being uploaded to the API.
     * @param progress Upload progress percentage (0-100)
     */
    data class Uploading(val progress: Int) : TranscriptionState()
    
    /**
     * Audio is being processed by the API (transcription in progress).
     */
    object Processing : TranscriptionState()
    
    /**
     * Transcription completed successfully.
     * @param result The transcription result
     */
    data class Success(val result: TranscriptionResult) : TranscriptionState()
    
    /**
     * An error occurred during transcription.
     * @param error The error that occurred
     */
    data class Error(val error: TranscriptionError) : TranscriptionState()
    
    /**
     * Transcription was cancelled by the user.
     */
    object Cancelled : TranscriptionState()
}
