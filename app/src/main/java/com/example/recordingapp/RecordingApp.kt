package com.example.recordingapp

import android.app.Application
import com.example.recordingapp.data.database.TranscriptionDatabase
import com.example.recordingapp.data.network.SecureNetworkManager
import com.example.recordingapp.data.network.TranscriptionProviderManager
import com.example.recordingapp.data.repository.TranscriptionRepository
import com.example.recordingapp.data.security.SecureApiKeyManager
import com.example.recordingapp.domain.TranscriptionService

class RecordingApp : Application() {
    
    lateinit var transcriptionService: TranscriptionService
        private set
    
    lateinit var providerManager: TranscriptionProviderManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        val apiKeyManager = SecureApiKeyManager(this)
        val networkManager = SecureNetworkManager(apiKeyManager)
        providerManager = TranscriptionProviderManager(this, networkManager)
        
        // Force set provider to Android Speech on app start (free, no API key needed)
        if (providerManager.getSelectedProviderType() != TranscriptionProviderManager.PROVIDER_ANDROID) {
            providerManager.setSelectedProviderType(TranscriptionProviderManager.PROVIDER_ANDROID)
        }
        
        val database = TranscriptionDatabase.getInstance(this)
        val repository = TranscriptionRepository(database.transcriptionDao())
        
        transcriptionService = TranscriptionService(providerManager, repository)
    }
}
