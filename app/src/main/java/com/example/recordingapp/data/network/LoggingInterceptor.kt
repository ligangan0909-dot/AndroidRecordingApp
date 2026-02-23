package com.example.recordingapp.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp interceptor that logs HTTP requests and responses with sensitive data sanitization.
 * 
 * This interceptor provides detailed logging for debugging while protecting sensitive information:
 * - Logs request method, URL, and headers
 * - Logs response code, message, and timing
 * - Sanitizes Authorization headers (shows "Bearer ***" instead of actual key)
 * - Logs errors and exceptions
 * 
 * Security features:
 * - API keys are never logged in plain text
 * - Authorization header values are masked
 * - Only shows "Bearer ***" for authentication
 * 
 * Performance tracking:
 * - Measures and logs request duration in milliseconds
 * - Helps identify slow API calls
 * 
 * Log levels:
 * - DEBUG: Request/response details
 * - INFO: Successful requests with timing
 * - WARN: Slow requests (>5 seconds)
 * - ERROR: Failed requests and exceptions
 */
class LoggingInterceptor : Interceptor {
    
    /**
     * Intercepts the request, logs details, and measures execution time.
     * 
     * @param chain The interceptor chain
     * @return Response from the server
     * @throws IOException if the request fails
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        // Log request details
        Log.d(TAG, "→ Request: ${request.method} ${request.url}")
        Log.d(TAG, "→ Headers: ${sanitizeHeaders(request.headers.toString())}")
        
        val response: Response
        try {
            // Proceed with the request
            response = chain.proceed(request)
            
            val duration = System.currentTimeMillis() - startTime
            
            // Log response details
            Log.d(TAG, "← Response: ${response.code} ${response.message}")
            Log.d(TAG, "← URL: ${response.request.url}")
            Log.i(TAG, "← Duration: ${duration}ms")
            
            // Warn about slow requests
            if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                Log.w(TAG, "Slow request detected: ${duration}ms for ${request.url}")
            }
            
            return response
            
        } catch (e: IOException) {
            val duration = System.currentTimeMillis() - startTime
            Log.e(TAG, "← Request failed after ${duration}ms: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Sanitizes headers by masking sensitive Authorization values.
     * 
     * Replaces actual API keys with "***" to prevent exposure in logs.
     * 
     * Example:
     * - Input: "Authorization: Bearer sk_test_abc123"
     * - Output: "Authorization: Bearer ***"
     * 
     * @param headers Raw headers string
     * @return Sanitized headers string with masked API keys
     */
    private fun sanitizeHeaders(headers: String): String {
        return headers.replace(
            regex = Regex("(Authorization:\\s*Bearer\\s+)[A-Za-z0-9_-]+"),
            replacement = "$1$MASKED_VALUE"
        )
    }
    
    companion object {
        private const val TAG = "LoggingInterceptor"
        private const val MASKED_VALUE = "***"
        private const val SLOW_REQUEST_THRESHOLD_MS = 5000L
    }
}
