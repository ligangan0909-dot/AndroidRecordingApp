package com.example.recordingapp.ui.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.domain.ITranscriptionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for playback screen with transcription integration
 * Manages transcription state and persistence
 */
class PlaybackViewModel(
    private val transcriptionService: ITranscriptionService
) : ViewModel() {

    private val _transcriptionState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState.asStateFlow()

    private val _hasTranscription = MutableStateFlow(false)
    val hasTranscription: StateFlow<Boolean> = _hasTranscription.asStateFlow()

    /**
     * Load existing transcription from database or .txt file
     */
    fun loadExistingTranscription(recordingId: String, audioFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            // First check database
            val cachedResult = transcriptionService.getCachedResult(recordingId)
            if (cachedResult != null) {
                _transcriptionState.value = TranscriptionState.Success(cachedResult)
                _hasTranscription.value = true
                return@launch
            }

            // Then check for .txt file
            val txtFile = File(audioFile.parent, "${audioFile.nameWithoutExtension}.txt")
            if (txtFile.exists() && txtFile.canRead()) {
                try {
                    val transcriptionText = txtFile.readText()
                    if (transcriptionText.isNotBlank()) {
                        _hasTranscription.value = true
                        // Note: We don't set state to Success here since we don't have full TranscriptionResult
                        // The UI will just know transcription exists and can load from file
                    }
                } catch (e: Exception) {
                    // Ignore file read errors
                }
            }
        }
    }

    /**
     * Start transcription and save to both database and .txt file
     */
    fun startTranscription(recordingId: String, audioFilePath: String, audioFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            transcriptionService.transcribe(recordingId, audioFilePath)
                .collect { state ->
                    _transcriptionState.value = state
                    
                    // Save to file when transcription succeeds
                    if (state is TranscriptionState.Success) {
                        saveTranscriptionToFile(state.result.fullText, audioFile)
                        _hasTranscription.value = true
                    }
                }
        }
    }

    /**
     * Cancel ongoing transcription
     */
    fun cancelTranscription() {
        // Note: We need recordingId to cancel, but we can call the service anyway
        // The service will handle cancellation of any active transcription
        viewModelScope.launch(Dispatchers.IO) {
            // Since we don't store recordingId, we'll need to pass it from the fragment
            // For now, this is a placeholder - the fragment should pass recordingId
        }
    }

    /**
     * Save transcription text to .txt file
     */
    private suspend fun saveTranscriptionToFile(transcriptionText: String, audioFile: File) {
        try {
            val txtFile = File(audioFile.parent, "${audioFile.nameWithoutExtension}.txt")
            txtFile.writeText(transcriptionText)
        } catch (e: Exception) {
            // Log error but don't fail the transcription
            e.printStackTrace()
        }
    }
}
