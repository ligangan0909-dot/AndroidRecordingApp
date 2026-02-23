package com.example.recordingapp.data.security

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Secure implementation of API key management using EncryptedSharedPreferences.
 * 
 * This class provides encrypted storage for DeepSeek API keys using AES256_GCM encryption.
 * All operations are performed on the IO dispatcher for thread safety.
 * 
 * API key format requirements:
 * - Length: 32-128 characters
 * - Allowed characters: alphanumeric (A-Z, a-z, 0-9), underscores (_), and hyphens (-)
 * 
 * Security features:
 * - AES256_GCM encryption for values
 * - AES256_SIV encryption for keys
 * - Never logs API keys in plain text
 * - Stores timestamp with each key for auditing
 * 
 * @param context Application context for accessing encrypted preferences
 */
class SecureApiKeyManager(private val context: Context) : IApiKeyManager {
    
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Saves an API key securely with format validation.
     * 
     * The key is validated before storage and encrypted using AES256_GCM.
     * A timestamp is stored alongside the key for auditing purposes.
     * 
     * @param apiKey The API key to save
     * @return Result.success if saved successfully, Result.failure with validation error otherwise
     */
    override suspend fun saveApiKey(apiKey: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate format before saving
                if (!validateKeyFormat(apiKey)) {
                    Log.w(TAG, "API key validation failed: invalid format")
                    return@withContext Result.failure(
                        IllegalArgumentException("API key format is invalid. Must be 32-128 characters, alphanumeric with underscores and hyphens.")
                    )
                }
                
                // Save encrypted key and timestamp
                encryptedPrefs.edit()
                    .putString(KEY_API_KEY, apiKey)
                    .putLong(KEY_SAVED_AT, System.currentTimeMillis())
                    .apply()
                
                Log.i(TAG, "API key saved successfully (key length: ${apiKey.length})")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save API key", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Retrieves the stored API key with decryption.
     * 
     * @return The decrypted API key, or null if no key is stored
     */
    override suspend fun getApiKey(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = encryptedPrefs.getString(KEY_API_KEY, null)
                if (apiKey != null) {
                    Log.d(TAG, "API key retrieved successfully (key length: ${apiKey.length})")
                } else {
                    Log.d(TAG, "No API key found in storage")
                }
                apiKey
            } catch (e: Exception) {
                Log.e(TAG, "Failed to retrieve API key", e)
                null
            }
        }
    }
    
    /**
     * Validates the format of an API key using regex.
     * 
     * Valid format:
     * - Length: 32-128 characters
     * - Characters: A-Z, a-z, 0-9, underscore (_), hyphen (-)
     * 
     * @param apiKey The API key to validate
     * @return true if the key format is valid
     */
    override fun validateKeyFormat(apiKey: String): Boolean {
        return apiKey.length in 32..128 && 
               apiKey.matches(API_KEY_REGEX)
    }
    
    /**
     * Clears the stored API key and associated metadata.
     * 
     * @return Result.success if cleared successfully, Result.failure otherwise
     */
    override suspend fun clearApiKey(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                encryptedPrefs.edit()
                    .remove(KEY_API_KEY)
                    .remove(KEY_SAVED_AT)
                    .apply()
                
                Log.i(TAG, "API key cleared successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear API key", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Checks if an API key is currently stored.
     * 
     * @return true if an API key exists in encrypted storage
     */
    override suspend fun hasApiKey(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                encryptedPrefs.contains(KEY_API_KEY)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check API key existence", e)
                false
            }
        }
    }
    
    companion object {
        private const val TAG = "SecureApiKeyManager"
        private const val PREFS_NAME = "deepseek_api_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SAVED_AT = "saved_at"
        
        /**
         * Regex pattern for valid API key format.
         * Matches: 32-128 characters of alphanumeric, underscore, or hyphen
         */
        private val API_KEY_REGEX = Regex("^[A-Za-z0-9_-]+$")
    }
}
