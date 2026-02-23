package com.example.recordingapp.data.security

/**
 * Interface for secure API key management.
 * 
 * Provides methods to securely store, retrieve, and validate DeepSeek API keys
 * using encrypted storage mechanisms.
 */
interface IApiKeyManager {
    /**
     * Saves an API key securely with encryption.
     * 
     * @param apiKey The API key to save (32-128 characters, alphanumeric with underscores and hyphens)
     * @return Result indicating success or failure with validation error
     */
    suspend fun saveApiKey(apiKey: String): Result<Unit>
    
    /**
     * Retrieves the stored API key with decryption.
     * 
     * @return The decrypted API key, or null if no key is stored
     */
    suspend fun getApiKey(): String?
    
    /**
     * Validates the format of an API key.
     * 
     * @param apiKey The API key to validate
     * @return true if the key format is valid (32-128 chars, alphanumeric with _ and -)
     */
    fun validateKeyFormat(apiKey: String): Boolean
    
    /**
     * Clears the stored API key.
     * 
     * @return Result indicating success or failure
     */
    suspend fun clearApiKey(): Result<Unit>
    
    /**
     * Checks if an API key is currently stored.
     * 
     * @return true if an API key exists in storage
     */
    suspend fun hasApiKey(): Boolean
}
