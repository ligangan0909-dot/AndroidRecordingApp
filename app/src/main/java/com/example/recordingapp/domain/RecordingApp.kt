package com.example.recordingapp

import android.app.Application
import com.example.recordingapp.data.database.TranscriptionDatabase
import com.example.recordingapp.data.network.SecureNetworkManager
import com.example.recordingapp.data.network.TranscriptionProviderManager
import com.example.recordingapp.data.repository.TranscriptionRepository
import com.example.recordingapp.data.security.SecureApiKeyManager
import com.example.recordingapp.domain.TranscriptionService
import com.example.recordingapp.ui.settings.SettingsViewModel
import com.example.recordingapp.ui.transcription.TranscriptionViewModel

/**
 * Application class for manual dependency injection.
 * 
 * Initializes and provides singleton instances of:
 * - Database
 * - ApiKeyManager
 * - NetworkManager
 * - ProviderManager (supports OpenAI and Doubao)
 * - Repository
 * - TranscriptionService (with multi-provider support)
 * - ViewModels
 */
class RecordingApp : Application() class RecordingApp : Application() class TranscriptionDatabase by lazy {
        TranscriptionDatabase.getInstance(this)
    }
    
    // Security
    val apiKeyManager: SecureApiKeyManager by lazy {
        SecureApiKeyManager(this)
    }
    
    // Network
    private val networkManager: SecureNetworkManager by lazy {
        SecureNetworkManager(apiKeyManager)
    }
    
    // Provider Manager (supports OpenAI and Doubao)
    val providerManager: TranscriptionProviderManager by lazy {
        TranscriptionProviderManager(this, networkManager)
    }
    
    // Repository
    val transcriptionRepository: TranscriptionRepository by lazy {
        TranscriptionRepository(database.transcriptionDao())
    }
    
    // Service (with multi-provider support)
    val transcriptionService: TranscriptionService by lazy {
        TranscriptionService(providerManager, transcriptionRepository)
    }
    
    // ViewModels
    val settingsViewModel: SettingsViewModel by lazy {
        SettingsViewModel(apiKeyManager)
    }
    
    val transcriptionViewModel: TranscriptionViewModel by lazy {
        TranscriptionViewModel(transcriptionService)
    }
}
