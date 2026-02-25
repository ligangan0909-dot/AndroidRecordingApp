package com.example.recordingapp.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.recordingapp.data.security.SecureApiKeyManager

class TranscriptionProviderManager(
    private val context: Context,
    private val networkManager: SecureNetworkManager
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val apiKeyManager: SecureApiKeyManager = SecureApiKeyManager(context)
    
    private var currentProvider: ITranscriptionApiClient? = null
    
    companion object {
        private const val TAG = "TranscriptionProviderManager"
        private const val PREFS_NAME = "transcription_provider_prefs"
        private const val KEY_SELECTED_PROVIDER = "selected_provider"
        
        const val PROVIDER_OPENAI = "openai"
        const val PROVIDER_DOUBAO = "doubao"
    }
    
    fun getSelectedProviderType(): String {
        return prefs.getString(KEY_SELECTED_PROVIDER, PROVIDER_OPENAI) ?: PROVIDER_OPENAI
    }
    
    fun setSelectedProviderType(providerType: String) {
        prefs.edit().putString(KEY_SELECTED_PROVIDER, providerType).apply()
        currentProvider = null
        Log.d(TAG, "Provider changed to: $providerType")
    }
    
    fun getProvider(): ITranscriptionApiClient {
        if (currentProvider != null) {
            return currentProvider!!
        }
        
        val providerType = getSelectedProviderType()
        currentProvider = when (providerType) {
            PROVIDER_OPENAI -> createOpenAIProvider()
            PROVIDER_DOUBAO -> createDoubaoProvider()
            else -> {
                Log.w(TAG, "Unknown provider type: $providerType, falling back to OpenAI")
                createOpenAIProvider()
            }
        }
        
        Log.d(TAG, "Created provider: ${currentProvider?.getProviderName()}")
        return currentProvider!!
    }
    
    fun getAvailableProviders(): List<ProviderInfo> {
        return listOf(
            ProviderInfo(
                type = PROVIDER_OPENAI,
                name = "OpenAI Whisper",
                description = "OpenAI's Whisper speech recognition API"
            ),
            ProviderInfo(
                type = PROVIDER_DOUBAO,
                name = "Doubao (豆包)",
                description = "Volcengine's speech recognition service"
            )
        )
    }
    
    suspend fun validateProvider(providerType: String): Result<Boolean> {
        val provider = when (providerType) {
            PROVIDER_OPENAI -> createOpenAIProvider()
            PROVIDER_DOUBAO -> createDoubaoProvider()
            else -> return Result.failure(IllegalArgumentException("Unknown provider: $providerType"))
        }
        
        return provider.validateCredentials()
    }
    
    fun getApiKeyManager(): SecureApiKeyManager {
        return apiKeyManager
    }
    
    private fun createOpenAIProvider(): ITranscriptionApiClient {
        return DeepSeekApiClient(
            okHttpClient = networkManager.createSecureHttpClient(),
            baseUrl = "https://api.openai.com/v1/"
        )
    }
    
    private fun createDoubaoProvider(): ITranscriptionApiClient {
        return DoubaoApiClient(
            apiKeyManager = apiKeyManager,
            networkManager = networkManager
        )
    }
    
    data class ProviderInfo(
        val type: String,
        val name: String,
        val description: String
    )
}
