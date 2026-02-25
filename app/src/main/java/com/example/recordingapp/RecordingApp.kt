package com.example.recordingapp

import android.app.Application
import com.example.recordingapp.data.database.TranscriptionDatabase
import com.example.recordingapp.data.network.SecureNetworkManager
import com.example.recordingapp.data.network.TranscriptionProviderManager
import com.example.recordingapp.data.repository.TranscriptionRepository
import com.example.recordingapp.data.security.SecureApiKeyManager
import com.example.recordingapp.domain.TranscriptionService

class RecordingApp : Application() {
    
    val database: TranscriptionDatabase by lazy {
        TranscriptionDatabase.getInstance(this)
    }
    
    val apiKeyManager: SecureApiKeyManager by lazy {
        SecureApiKeyManager(this)
    }
    
    private val networkManager: SecureNetworkManager by lazy {
        SecureNetworkManager(apiKeyManager)
    }
    
    val providerManager: TranscriptionProviderManager by lazy {
        TranscriptionProviderManager(this, networkManager)
    }
    
    val transcriptionRepository: TranscriptionRepository by lazy {
        TranscriptionRepository(database.transcriptionDao())
    }
    
    val transcriptionService: TranscriptionService by lazy {
        TranscriptionService(providerManager, transcriptionRepository)
    }
}
