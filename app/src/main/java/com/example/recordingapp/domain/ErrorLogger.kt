package com.example.recordingapp.domain

import android.util.Log
import com.example.recordingapp.data.model.TranscriptionError

/**
 * Centralized error logging with categorization
 * Integrates with Firebase Crashlytics for critical errors
 */
object ErrorLogger {
    private const val TAG = "TranscriptionError"
    
    /**
     * Log error with appropriate level based on error type
     */
    fun logError(error: TranscriptionError, context: String = "") {
        val message = buildErrorMessage(error, context)
        
        when (error) {
            is TranscriptionError.NetworkError -> {
                Log.e(TAG, "Network Error: $message", error.cause)
                // Log to analytics for monitoring
                logToAnalytics("network_error", message)
            }
            is TranscriptionError.AuthenticationError -> {
                Log.e(TAG, "Authentication Error: $message")
                logToAnalytics("auth_error", message)
            }
            is TranscriptionError.ValidationError -> {
                Log.w(TAG, "Validation Error: $message")
                logToAnalytics("validation_error", message)
            }
            is TranscriptionError.FileError -> {
                Log.e(TAG, "File Error: $message")
                logToAnalytics("file_error", message)
            }
            is TranscriptionError.ApiError -> {
                Log.e(TAG, "API Error [${error.code}]: $message")
                logToAnalytics("api_error", message)
                // Report critical API errors to Crashlytics
                if (error.code >= 500) {
                    reportToCrashlytics(error, message)
                }
            }
            is TranscriptionError.TimeoutError -> {
                Log.e(TAG, "Timeout Error: $message")
                logToAnalytics("timeout_error", message)
            }
            is TranscriptionError.UnknownError -> {
                Log.e(TAG, "Unknown Error: $message", error.cause)
                reportToCrashlytics(error, message)
            }
        }
    }
    
    /**
     * Log timeout event for monitoring
     */
    fun logTimeout(operation: String, durationMs: Long) {
        val message = "Timeout in $operation after ${durationMs}ms"
        Log.w(TAG, message)
        logToAnalytics("timeout_event", message)
    }
    
    /**
     * Convert error to user-friendly message
     */
    fun getUserMessage(error: TranscriptionError): String {
        return when (error) {
            is TranscriptionError.NetworkError -> 
                "网络连接失败，请检查网络设置后重试"
            is TranscriptionError.AuthenticationError -> 
                "API 密钥无效，请在设置中更新密钥"
            is TranscriptionError.ValidationError -> 
                "输入验证失败：${error.message}"
            is TranscriptionError.FileError -> 
                "文件错误：${error.message}"
            is TranscriptionError.ApiError -> 
                when (error.code) {
                    400 -> "请求格式错误，请重试"
                    401 -> "API 密钥无效"
                    403 -> "访问被拒绝，请检查权限"
                    404 -> "服务未找到"
                    429 -> "请求过于频繁，请稍后重试"
                    500, 502, 503 -> "服务器错误，请稍后重试"
                    else -> "API 错误 (${error.code}): ${error.message}"
                }
            is TranscriptionError.TimeoutError -> 
                "请求超时，请检查网络连接后重试"
            is TranscriptionError.UnknownError -> 
                "未知错误：${error.message}"
        }
    }
    
    /**
     * Build detailed error message for logging
     */
    private fun buildErrorMessage(error: TranscriptionError, context: String): String {
        val contextPrefix = if (context.isNotEmpty()) "[$context] " else ""
        return "$contextPrefix${error.message}"
    }
    
    /**
     * Log to analytics (placeholder for Firebase Analytics)
     */
    private fun logToAnalytics(eventName: String, message: String) {
        // TODO: Integrate with Firebase Analytics
        // FirebaseAnalytics.getInstance(context).logEvent(eventName, Bundle().apply {
        //     putString("error_message", message)
        //     putLong("timestamp", System.currentTimeMillis())
        // })
        Log.d(TAG, "Analytics: $eventName - $message")
    }
    
    /**
     * Report critical errors to Crashlytics
     */
    private fun reportToCrashlytics(error: TranscriptionError, message: String) {
        // TODO: Integrate with Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().apply {
        //     setCustomKey("error_type", error::class.simpleName ?: "Unknown")
        //     setCustomKey("error_message", message)
        //     recordException(error.cause ?: Exception(message))
        // }
        Log.d(TAG, "Crashlytics: $message")
    }
}
