package com.example.recordingapp

import android.app.Application
import com.example.recordingapp.data.network.SecureNetworkManager
import com.example.recordingapp.data.network.TranscriptionProviderManager
import com.example.recordingapp.data.repository.TranscriptionRepository
import com.example.recordingapp.domain.TranscriptionService

class RecordingApp : Application() {
    
    lateinit var transcriptionService: TranscriptionService
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        val networkManager = SecureNetworkManager()
        val providerManager = TranscriptionProviderManager(this, networkManager)
        
        // Force set provider to Doubao on app start
        if (providerManager.getSelectedProviderType() != TranscriptionProviderManager.PROVIDER_DOUBAO) {
            providerManager.setSelectedProviderType(TranscriptionProviderManager.PROVIDER_DOUBAO)
        }
        
        val repository = TranscriptionRepository(this)
        
        transcriptionService = TranscriptionService(providerManager, repository)
    }
}
