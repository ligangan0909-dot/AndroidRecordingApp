package com.example.recordingapp.ui.transcription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recordingapp.domain.ITranscriptionService

/**
 * Factory for creating TranscriptionViewModel with dependencies.
 * 
 * This factory is required because ViewModels with constructor parameters
 * cannot be instantiated directly by ViewModelProvider.
 * 
 * @param transcriptionService The transcription service dependency
 */
class TranscriptionViewModelFactory(
    private val transcriptionService: ITranscriptionService
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranscriptionViewModel::class.java)) {
            return TranscriptionViewModel(transcriptionService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
