package com.example.recordingapp.ui.transcription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recordingapp.domain.ITranscriptionService

class TranscriptionViewModelFactory(
    private val transcriptionService: ITranscriptionService
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranscriptionViewModel::class.java)) {
            return TranscriptionViewModel(transcriptionService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
