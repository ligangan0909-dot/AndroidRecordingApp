package com.example.recordingapp.ui.transcription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.domain.ITranscriptionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for transcription screen
 * Manages transcription state and user actions
 */
class TranscriptionViewModel(
    private val transcriptionService: ITranscriptionService
) : ViewModel() {

    private val _transcriptionState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState.asStateFlow()

    private var currentRecordingId: String? = null
    private var currentAudioFilePath: String? = null

    /**
     * Start transcription for a recording
     */
    fun startTranscription(recordingId: String, audioFilePath: String) {
        currentRecordingId = recordingId
        currentAudioFilePath = audioFilePath
        
        viewModelScope.launch(Dispatchers.IO) {
            transcriptionService.transcribe(recordingId, audioFilePath)
                .collect { state ->
                    _transcriptionState.value = state
                }
        }
    }

    /**
     * Cancel the current transcription
     */
    fun cancelTranscription() {
        currentRecordingId?.let { recordingId ->
            transcriptionService.cancelTranscription(recordingId)
        }
    }

    /**
     * Retry transcription after an error
     */
    fun retryTranscription() {
        val recordingId = currentRecordingId
        val audioFilePath = currentAudioFilePath
        
        if (recordingId != null && audioFilePath != null) {
            startTranscription(recordingId, audioFilePath)
        }
    }

    /**
     * Load cached transcription result
     */
    fun loadCachedResult(recordingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedResult = transcriptionService.getCachedResult(recordingId)
            if (cachedResult != null) {
                _transcriptionState.value = TranscriptionState.Success(cachedResult)
            }
        }
    }
}
