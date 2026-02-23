package com.example.recordingapp.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that adds GZIP compression support to requests.
 * 
 * This interceptor adds the Accept-Encoding header to indicate that the client
 * can handle GZIP-compressed responses. This reduces bandwidth usage and improves
 * performance, especially for large transcription responses.
 * 
 * Performance benefits:
 * - Reduces response size (typically 60-80% compression for text)
 * - Faster data transfer over network
 * - Lower bandwidth costs
 * - Particularly effective for JSON responses
 * 
 * Note: OkHttp automatically handles decompression of GZIP responses,
 * so no additional code is needed to decompress the data.
 */
class GzipInterceptor : Interceptor {
    
    /**
     * Intercepts the request and adds GZIP compression header.
     * 
     * @param chain The interceptor chain
     * @return Response from the server (automatically decompressed if GZIP)
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP)
            .build()
        
        Log.d(TAG, "GZIP compression enabled for: ${request.url}")
        
        return chain.proceed(request)
    }
    
    companion object {
        private const val TAG = "GzipInterceptor"
        private const val HEADER_ACCEPT_ENCODING = "Accept-Encoding"
        private const val ENCODING_GZIP = "gzip"
    }
}
