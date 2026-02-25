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
 * This class provides encrypted storage for multiple transcription service credentials
 * using AES256_GCM encryption. All operations are performed on the IO dispatcher for thread safety.
 * 
 * Supported services:
 * - OpenAI Whisper (API key format: sk-*)
 * - Doubao/Volcengine (AppId, AccessKeyId, SecretKey, ClusterId)
 * 
 * Security features:
 * - AES256_GCM encryption for values
 * - AES256_SIV encryption for keys
 * - Never logs credentials in plain text
 * - Stores timestamp with each credential for auditing
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
    
    // ========== OpenAI/Whisper API Key Methods ==========
    
    override suspend fun saveApiKey(apiKey: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (!validateKeyFormat(apiKey)) {
                    Log.w(TAG, "API key validation failed: invalid format")
                    return@withContext Result.failure(
                        IllegalArgumentException("API key format is invalid. Must start with 'sk-' and be 20-200 characters long.")
                    )
                }
                
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
    
    override fun validateKeyFormat(apiKey: String): Boolean {
        return apiKey.startsWith("sk-") && 
               apiKey.length in API_KEY_MIN_LENGTH..API_KEY_MAX_LENGTH
    }
    
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
    
    // ========== Doubao Configuration Methods ==========
    
    suspend fun saveDoubaoConfig(
        appId: String,
        accessKeyId: String,
        secretKey: String,
        clusterId: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (appId.isBlank() || accessKeyId.isBlank() || 
                    secretKey.isBlank() || clusterId.isBlank()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("All Doubao configuration fields are required")
                    )
                }
                
                encryptedPrefs.edit()
                    .putString(KEY_DOUBAO_APP_ID, appId)
                    .putString(KEY_DOUBAO_ACCESS_KEY_ID, accessKeyId)
                    .putString(KEY_DOUBAO_SECRET_KEY, secretKey)
                    .putString(KEY_DOUBAO_CLUSTER_ID, clusterId)
                    .putLong(KEY_DOUBAO_SAVED_AT, System.currentTimeMillis())
                    .apply()
                
                Log.i(TAG, "Doubao config saved successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save Doubao config", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun getDoubaoConfig(): DoubaoConfig? {
        return withContext(Dispatchers.IO) {
            try {
                val appId = encryptedPrefs.getString(KEY_DOUBAO_APP_ID, null)
                val accessKeyId = encryptedPrefs.getString(KEY_DOUBAO_ACCESS_KEY_ID, null)
                val secretKey = encryptedPrefs.getString(KEY_DOUBAO_SECRET_KEY, null)
                val clusterId = encryptedPrefs.getString(KEY_DOUBAO_CLUSTER_ID, null)
                
                if (appId != null && accessKeyId != null && 
                    secretKey != null && clusterId != null) {
                    Log.d(TAG, "Doubao config retrieved successfully")
                    DoubaoConfig(appId, accessKeyId, secretKey, clusterId)
                } else {
                    Log.d(TAG, "Doubao config incomplete or not found")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to retrieve Doubao config", e)
                null
            }
        }
    }
    
    suspend fun hasDoubaoConfig(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                encryptedPrefs.contains(KEY_DOUBAO_APP_ID) &&
                encryptedPrefs.contains(KEY_DOUBAO_ACCESS_KEY_ID) &&
                encryptedPrefs.contains(KEY_DOUBAO_SECRET_KEY) &&
                encryptedPrefs.contains(KEY_DOUBAO_CLUSTER_ID)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check Doubao config existence", e)
                false
            }
        }
    }
    
    suspend fun clearDoubaoConfig(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                encryptedPrefs.edit()
                    .remove(KEY_DOUBAO_APP_ID)
                    .remove(KEY_DOUBAO_ACCESS_KEY_ID)
                    .remove(KEY_DOUBAO_SECRET_KEY)
                    .remove(KEY_DOUBAO_CLUSTER_ID)
                    .remove(KEY_DOUBAO_SAVED_AT)
                    .apply()
                
                Log.i(TAG, "Doubao config cleared successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear Doubao config", e)
                Result.failure(e)
            }
        }
    }
    
    data class DoubaoConfig(
        val appId: String,
        val accessKeyId: String,
        val secretKey: String,
        val clusterId: String
    )
    
    companion object {
        private const val TAG = "SecureApiKeyManager"
        private const val PREFS_NAME = "transcription_api_prefs"
        
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SAVED_AT = "saved_at"
        private const val API_KEY_MIN_LENGTH = 20
        private const val API_KEY_MAX_LENGTH = 200
        
        private const val KEY_DOUBAO_APP_ID = "doubao_app_id"
        private const val KEY_DOUBAO_ACCESS_KEY_ID = "doubao_access_key_id"
        private const val KEY_DOUBAO_SECRET_KEY = "doubao_secret_key"
        private const val KEY_DOUBAO_CLUSTER_ID = "doubao_cluster_id"
        private const val KEY_DOUBAO_SAVED_AT = "doubao_saved_at"
    }
}
