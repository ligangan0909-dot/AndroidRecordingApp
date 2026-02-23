package com.example.recordingapp.domain

import com.example.recordingapp.data.model.TranscriptionError
import com.example.recordingapp.data.model.TranscriptionResult
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.data.network.IDeepSeekApiClient
import com.example.recordingapp.data.repository.ITranscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * Implementation of transcription service
 * Simplified version: Only transcription and summary (no speaker diarization, no task extraction)
 */
class TranscriptionService(
    private val apiClient: IDeepSeekApiClient,
    private val repository: ITranscriptionRepository
) : ITranscriptionService {

    private val activeCancellations = mutableSetOf<String>()

    override fun transcribe(
        recordingId: String,
        audioFilePath: String
    ): Flow<TranscriptionState> = flow {
        try {
            emit(TranscriptionState.Idle)
            
            // Validate audio file
            val file = File(audioFilePath)
            val validationError = validateAudioFile(file)
            if (validationError != null) {
                emit(TranscriptionState.Error(validationError))
                return@flow
            }
            
            // Check if cancelled
            if (activeCancellations.contains(recordingId)) {
                activeCancellations.remove(recordingId)
                emit(TranscriptionState.Cancelled)
                return@flow
            }
            
            // Upload audio
            emit(TranscriptionState.Uploading(0))
            
            // Process transcription (simplified - no diarization, no task extraction)
            emit(TranscriptionState.Processing)
            
            val result = apiClient.transcribeAudio(
                audioFile = file,
                enableDiarization = false,
                enableTaskExtraction = false,
                enableSummary = true
            )
            
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
                    
                    emit(TranscriptionState.Success(transcriptionResult))
                },
                onFailure = { error ->
                    val transcriptionError = when (error) {
                        is TranscriptionError -> error
                        else -> TranscriptionError.NetworkError(error.message ?: "Unknown error")
                    }
                    emit(TranscriptionState.Error(transcriptionError))
                }
            )
        } catch (e: Exception) {
            if (activeCancellations.contains(recordingId)) {
                activeCancellations.remove(recordingId)
                emit(TranscriptionState.Cancelled)
            } else {
                emit(TranscriptionState.Error(
                    TranscriptionError.NetworkError(e.message ?: "Unknown error")
                ))
            }
        }
    }

    override fun cancelTranscription(recordingId: String) {
        activeCancellations.add(recordingId)
        apiClient.cancelTranscription()
    }

    override suspend fun getCachedResult(recordingId: String): TranscriptionResult? {
        return repository.getTranscription(recordingId)
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
