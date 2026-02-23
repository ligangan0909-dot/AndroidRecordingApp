package com.example.recordingapp.data.network

import android.util.Log
import com.example.recordingapp.data.security.IApiKeyManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that injects API key authentication into requests.
 * 
 * This interceptor automatically adds the Authorization header with the API key
 * retrieved from secure storage. It also adds a User-Agent header for API tracking.
 * 
 * Header format:
 * - Authorization: Bearer {API_KEY}
 * - User-Agent: AndroidRecordingApp/1.0
 * 
 * Error handling:
 * - If no API key is found, throws SecurityException
 * - Logs authentication attempts (without exposing the key)
 * 
 * @param apiKeyManager Manager for retrieving API keys securely
 */
class AuthInterceptor(private val apiKeyManager: IApiKeyManager) : Interceptor {
    
    /**
     * Intercepts the request and adds authentication headers.
     * 
     * @param chain The interceptor chain
     * @return Response from the server
     * @throws SecurityException if API key is not configured
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // Retrieve API key from secure storage
        val apiKey = runBlocking { apiKeyManager.getApiKey() }
        
        // Fail fast if no API key is configured
        if (apiKey == null) {
            Log.e(TAG, "API key not configured - cannot authenticate request")
            throw SecurityException("API key not configured. Please configure your DeepSeek API key in settings.")
        }
        
        // Build request with authentication headers
        val request = chain.request().newBuilder()
            .addHeader(HEADER_AUTHORIZATION, "$BEARER_PREFIX$apiKey")
            .addHeader(HEADER_USER_AGENT, USER_AGENT_VALUE)
            .build()
        
        Log.d(TAG, "Authentication headers added to request: ${request.url}")
        
        return chain.proceed(request)
    }
    
    companion object {
        private const val TAG = "AuthInterceptor"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_USER_AGENT = "User-Agent"
        private const val BEARER_PREFIX = "Bearer "
        private const val USER_AGENT_VALUE = "AndroidRecordingApp/1.0"
    }
}
