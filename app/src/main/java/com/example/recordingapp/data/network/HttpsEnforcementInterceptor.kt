package com.example.recordingapp.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that enforces HTTPS-only connections.
 * 
 * This interceptor provides a security layer by rejecting any non-HTTPS requests.
 * It prevents accidental transmission of sensitive data (API keys, audio files) over
 * unencrypted HTTP connections.
 * 
 * Security features:
 * - Blocks all HTTP requests
 * - Only allows HTTPS protocol
 * - Throws SecurityException for non-HTTPS attempts
 * - Logs security violations
 * 
 * This is a critical security component that ensures all API communication is encrypted.
 */
class HttpsEnforcementInterceptor : Interceptor {
    
    /**
     * Intercepts the request and validates that it uses HTTPS protocol.
     * 
     * @param chain The interceptor chain
     * @return Response from the server if HTTPS is used
     * @throws SecurityException if the request uses HTTP instead of HTTPS
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val scheme = request.url.scheme
        
        // Enforce HTTPS-only connections
        if (scheme != HTTPS_SCHEME) {
            val url = request.url.toString()
            Log.e(TAG, "Security violation: Attempted non-HTTPS connection to $url")
            throw SecurityException(
                "Only HTTPS connections are allowed. Attempted to connect to: $url"
            )
        }
        
        Log.d(TAG, "HTTPS validation passed for: ${request.url}")
        return chain.proceed(request)
    }
    
    companion object {
        private const val TAG = "HttpsEnforcementInterceptor"
        private const val HTTPS_SCHEME = "https"
    }
}
