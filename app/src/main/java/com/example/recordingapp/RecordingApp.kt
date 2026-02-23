package com.example.recordingapp

import android.app.Application
import com.example.recordingapp.data.database.TranscriptionDatabase
import com.example.recordingapp.data.network.DeepSeekApiClient
import com.example.recordingapp.data.network.SecureNetworkManager
import com.example.recordingapp.data.repository.TranscriptionRepository
import com.example.recordingapp.data.security.SecureApiKeyManager
import com.example.recordingapp.domain.TranscriptionService

/**
 * Application class for manual dependency injection.
 * 
 * Initializes and provides singleton instances of:
 * - Database
 * - ApiKeyManager
 * - NetworkManager
 * - ApiClient
 * - Repository
 * - TranscriptionService
 */
class RecordingApp : Application() {
    
    // Database
    val database: TranscriptionDatabase by lazy {
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
    
    val apiClient: DeepSeekApiClient by lazy {
        DeepSeekApiClient(networkManager.createSecureHttpClient())
    }
    
    // Repository
    val transcriptionRepository: TranscriptionRepository by lazy {
        TranscriptionRepository(database.transcriptionDao())
    }
    
    // Service
    val transcriptionService: TranscriptionService by lazy {
        TranscriptionService(apiClient, transcriptionRepository)
    }
}
