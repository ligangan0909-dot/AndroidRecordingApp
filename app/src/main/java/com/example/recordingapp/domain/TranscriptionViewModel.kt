package com.example.recordingapp.ui.transcription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.domain.ErrorLogger
import com.example.recordingapp.domain.ITranscriptionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TranscriptionViewModel(
    private val transcriptionService: ITranscriptionService
) : ViewModel() {

    private val _transcriptionState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState.asStateFlow()

    private val _userMessage = MutableStateFlow<String>("")
    val userMessage: StateFlow<String> = _userMessage.asStateFlow()

    fun startTransc    fun startTransc    fun startTransc    fun startTransc     viewModelScope.launch {
            transcriptionService.transcribe(recordingId, audioFilePath)
                .collect { state ->
                    _transcriptionState.value = state
                    
                    // Update user message for error states
                    if (state is TranscriptionState.Error) {
                        _userMessage.value = ErrorLogger.getUserMessage(state.error)
                    }
                }
        }
    }

    fun cancelTranscription(recordingId: String) {
        transcriptionService.cancelTranscription(recordingId)
    }

    fun retryTranscription(recordingId: String, audioFilePath: String) {
        startTranscription(recordingId, audioFilePath)
    }

    fun loadCachedResult(recordingId: String) {
        viewModelScope.launch {
            val cachedResult = transcriptionService.getCachedResult(recordingId)
            if (cachedResult != null) {
                _transcriptionState.value = TranscriptionState.Success(cachedResult)
            }
        }
    }
}
