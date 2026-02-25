package com.example.recordingapp.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure API key manager using EncryptedSharedPreferences
 * Supports multiple providers: OpenAI and Doubao
 */
class SecureApiKeyManager(context: Context) : IApiKeyManager {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_api_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val SELECTED_PROVIDER_KEY = "selected_provider"
        private const val DEFAULT_PROVIDER = "openai"
        private const val DEFAULT_PROVIDER = "openai"
 provider: String) {
        if (!validateKeyFormat(apiKey)) {
            throw IllegalArgumentException("Invalid API key format for provider: $provider")
        }
        encryptedPrefs.edit().putString("${provider}_api_key", apiKey).apply()
    }
    
    override fun getApiKey(provider: String): String? {
        return encryptedPrefs.getString("${provider}_api_key", null)
    }
    
    override fun clearApiKey(provider: String) {
        encryptedPrefs.edit().remove("${provider}_api_key").apply()
    }
    
    override fun hasApiKey(provider: String): Boolean {
        return encryptedPrefs.contains("${provider}_api_key")
    }
    
    override fun validateKeyFormat(apiKey: String): Boolean {
        // Basic validation: non-empty and reasonable length
        return apiKey.isNotBlank() && apiKey.length >= 20
    }
    
    /**
     * Save selected provider
     */
    fun saveSelectedProvider(provider: String) {
        encryptedPrefs.edit().putString(SELECTED_PROVIDER_KEY, provider).apply()
    }
    
    /**
     * Get selected provider
     */
    fun getSelectedProvider(): String {
        return encryptedPrefs.getString(SELECTED_PROVIDER_KEY, DEFAULT_PROVIDER) ?: DEFAULT_PROVIDER
    }
}
