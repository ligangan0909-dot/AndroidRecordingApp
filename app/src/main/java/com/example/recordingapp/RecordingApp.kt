package com.example.recordingapp

import android.app.Application
import com.example.recordingapp.data.database.TranscriptionDatabase
import com.example.recordingapp.data.network.SecureNetworkManager
import com.example.recordingapp.data.network.TranscriptionProviderManager
import com.example.recordingapp.data.repository.TranscriptionRepository
import com.example.recordingapp.domain.TranscriptionService

class RecordingApp : Application() {
    
    lateinit var transcriptionService: TranscriptionService
        private set
    
    lateinit var providerManager: TranscriptionProviderManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        val networkManager = SecureNetworkManager()
        providerManager = TranscriptionProviderManager(this, networkManager)
        
        // Force set provider to Doubao on app start
        if (providerManager.getSelectedProviderType() != TranscriptionProviderManager.PROVIDER_DOUBAO) {
            providerManager.setSelectedProviderType(TranscriptionProviderManager.PROVIDER_DOUBAO)
        }
        
        val database = TranscriptionDatabase.getDatabase(this)
        val repository = TranscriptionRepository(database.transcriptionDao())
        
        transcriptionService = TranscriptionService(providerManager, repository)
    }
}
