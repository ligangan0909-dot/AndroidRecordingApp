package com.example.recordingapp.data.network

import com.example.recordingapp.data.security.IApiKeyManager
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Manages secure HTTP client configuration for DeepSeek API communication.
 * 
 * This class creates and configures OkHttpClient instances with security features:
 * - HTTPS-only enforcement
 * - API key authentication via AuthInterceptor
 * - Request/response logging with sensitive data sanitization
 * - Connection pooling for performance
 * - Timeout configurations
 * - GZIP compression support
 * 
 * Security features:
 * - Rejects non-HTTPS connections
 * - Sanitizes API keys in logs (shows "Bearer ***")
 * - Configurable timeouts to prevent hanging requests
 * 
 * Performance optimizations:
 * - Connection pool: 5 max idle connections, 5 minute keep-alive
 * - GZIP compression for reduced bandwidth
 * - Connection reuse for multiple requests
 * 
 * Timeout configuration:
 * - Connect timeout: 30 seconds
 * - Read timeout: 120 seconds (transcription can take time)
 * - Write timeout: 120 seconds (large audio file uploads)
 * 
 * @param apiKeyManager Manager for retrieving API keys securely
 */
class SecureNetworkManager(private val apiKeyManager: IApiKeyManager) {
    
    /**
     * Creates a secure OkHttpClient with all security and performance features enabled.
     * 
     * The client is configured with:
     * - HTTPS enforcement interceptor
     * - Authentication interceptor for API key injection
     * - Logging interceptor with sensitive data sanitization
     * - Connection pool for reuse
     * - Timeout configurations
     * - GZIP compression
     * 
     * @return Configured OkHttpClient instance ready for API requests
     */
    fun createSecureHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // Connection pool for performance
            .connectionPool(
                ConnectionPool(
                    maxIdleConnections = MAX_IDLE_CONNECTIONS,
                    keepAliveDuration = KEEP_ALIVE_DURATION_MINUTES,
                    timeUnit = TimeUnit.MINUTES
                )
            )
            // Timeout configurations
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // Security interceptors (order matters: HTTPS check -> Auth -> Logging)
            .addInterceptor(HttpsEnforcementInterceptor())
            .addInterceptor(AuthInterceptor(apiKeyManager))
            .addInterceptor(LoggingInterceptor())
            // GZIP compression
            .addInterceptor(GzipInterceptor())
            .build()
    }
    
    companion object {
        // Connection pool configuration
        private const val MAX_IDLE_CONNECTIONS = 5
        private const val KEEP_ALIVE_DURATION_MINUTES = 5L
        
        // Timeout configuration (in seconds)
        private const val CONNECT_TIMEOUT_SECONDS = 30L
        private const val READ_TIMEOUT_SECONDS = 120L
        private const val WRITE_TIMEOUT_SECONDS = 120L
    }
}
