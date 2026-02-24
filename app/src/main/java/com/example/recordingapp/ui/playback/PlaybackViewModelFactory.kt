package com.example.recordingapp.ui.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recordingapp.domain.ITranscriptionService

/**
 * Factory for creating PlaybackViewModel with dependencies.
 * 
 * This factory is required because ViewModels with constructor parameters
 * cannot be instantiated directly by ViewModelProvider.
 * 
 * @param transcriptionService The transcription service dependency
 */
class PlaybackViewModelFactory(
    private val transcriptionService: ITranscriptionService
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaybackViewModel::class.java)) {
            return PlaybackViewModel(transcriptionService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
