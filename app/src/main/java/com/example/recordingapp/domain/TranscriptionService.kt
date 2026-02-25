package com.example.recordingapp.domain

import android.util.Log
import com.example.recordingapp.data.model.TranscriptionError
import com.example.recordingapp.data.model.TranscriptionResult
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.data.network.TranscriptionProviderManager
import com.example.recordingapp.data.repository.ITranscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import java.io.File

/**
 * Implementation of transcription service with multi-provider support
 * Supports dynamic switching between OpenAI and Doubao providers
 */
class TranscriptionService(
    private val providerManager: TranscriptionProviderManager,
    private val repository: ITranscriptionRepository
) : ITranscriptionService {

    private val activeCancellations = mutableSetOf<String>()
    
    companion object {
        private const val TAG = "TranscriptionService"
    }

    override fun transcribe(recordingId: String, audioFilePath: String): Flow<TranscriptionState> = flow {
        try {
            emit(TranscriptionState.Idle)
            
            // Validate audio file
            val file = File(audioFilePath)
            val validationError = validateAudioFile(file)
            if (validationError != null) {
                ErrorLogger.logError(validationError, "Audio validation")
                emit(TranscriptionState.Error(validationError))
                return@flow
            }
            
            // Check if cancelled
            if (activeCancellations.contains(recordingId)) {
                activeCancellations.remove(recordingId)
                emit(TranscriptionState.Cancelled)
                return@flow
            }
            
            // Get current provider
            val apiClient = providerManager.getProvider()
            val providerType = providerManager.getSelectedProviderType()
            Log.d(TAG, "Using provider: $providerType (${apiClient.getProviderName()})")
            
            // Upload audio
            emit(TranscriptionState.Uploading(0))
            
            // Process transcription (simplified - no diarization, no task extraction)
            emit(TranscriptionState.Processing)
            
            // Use retry logic for API call
            val startTime = System.currentTimeMillis()
            val result = retryWithExponentialBackoff {
                apiClient.transcribeAudio(
                    audioFile = file,
                    enableDiarization = false,
                    enableTaskExtraction = false,
                    enableSummary = true
                )
            }
            val duration = System.currentTimeMillis() - startTime
            
            result.fold(
                onSuccess = { response ->
                    // Check if cancelled during processing
                    if (activeCancellations.contains(recordingId)) {
                        activeCancellations.remove(recordingId)
                        emit(TranscriptionState.Cancelled)
                        return@fold
                    }
                    
                    // Convert response to TranscriptionResult
                    val transcriptionResult = TranscriptionResult(
                        recordingId = recordingId,
                        transcriptionId = response.transcriptionId,
                        fullText = response.fullText,
                        summary = response.summary,
                        language = response.language,
                        createdAt = System.currentTimeMillis(),
                        durationMs = response.durationMs,
                        isCached = false
                    )
                    
                    // Save to repository
                    repository.saveTranscription(transcriptionResult)
                    
                    Log.d(TAG, "Transcription successful with provider: $providerType")
                    emit(TranscriptionState.Success(transcriptionResult))
                },
                onFailure = { error ->
                    val transcriptionError = when (error) {
                        is TranscriptionError -> error
                        else -> TranscriptionError.NetworkError(error.message ?: "Unknown error")
                    }
                    
                    // Log error with context
                    ErrorLogger.logError(transcriptionError, "Transcription API call with provider: $providerType")
                    
                    // Log timeout if duration is excessive
                    if (duration > 120000) { // 2 minutes
                        ErrorLogger.logTimeout("transcribeAudio", duration)
                    }
                    
                    emit(TranscriptionState.Error(transcriptionError))
                }
            )
        } catch (e: Exception) {
            if (activeCancellations.contains(recordingId)) {
                activeCancellations.remove(recordingId)
                emit(TranscriptionState.Cancelled)
            } else {
                val error = when (e) {
                    is TranscriptionError -> e
                    else -> TranscriptionError.UnknownError(e.message ?: "Unknown error", e)
                }
                
                // Log error
                ErrorLogger.logError(error, "Transcription flow")
                
                emit(TranscriptionState.Error(error))
            }
        }
    }

    override fun cancelTranscription(recordingId: String) {
        activeCancellations.add(recordingId)
        // Cancel on current provider
        try {
            val apiClient = providerManager.getProvider()
            apiClient.cancelTranscription()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling transcription", e)
        }
    }

    override suspend fun getCachedResult(recordingId: String): TranscriptionResult? {
        return try {
            repository.getTranscription(recordingId)
        } catch (e: Exception) {
            val error = TranscriptionError.UnknownError("Failed to get cached result", e)
            ErrorLogger.logError(error, "Get cached result")
            null
        }
    }

    /**
     * Retry logic with exponential backoff
     * - Max retries: 3
     * - Initial delay: 1s
     * - Max delay: 10s
     * - Only retries network errors (not auth/validation errors)
     */
    private suspend fun <T> retryWithExponentialBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                
                // Don't retry on auth or validation errors
                if (e is TranscriptionError.AuthenticationError || 
                    e is TranscriptionError.ValidationError) {
                    throw e
                }
                
                // Last attempt - throw the error
                if (attempt == maxRetries - 1) {
                    throw e
                }
                
                // Log retry attempt
                ErrorLogger.logError(
                    TranscriptionError.NetworkError("Retry attempt ${attempt + 1}/$maxRetries"),
                    "Retry logic"
                )
                
                // Wait before retry
                delay(currentDelay)
                
                // Calculate next delay with exponential backoff
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }
        
        // This should never be reached, but needed for compilation
        throw lastException ?: Exception("Retry failed")
    }

    private fun validateAudioFile(file: File): TranscriptionError? {
        return when {
            !file.exists() -> TranscriptionError.FileError("File does not exist")
            !file.canRead() -> TranscriptionError.FileError("Cannot read file")
            file.length() > 25 * 1024 * 1024 -> TranscriptionError.FileError("File size exceeds 25MB limit")
            !file.name.endsWith(".wav", ignoreCase = true) -> TranscriptionError.ValidationError("Only WAV format is supported")
            else -> null
        }
    }
}
