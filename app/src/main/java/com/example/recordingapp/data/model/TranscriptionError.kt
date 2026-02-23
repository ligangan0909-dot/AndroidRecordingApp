package com.example.recordingapp.data.model

/**
 * Sealed class hierarchy representing different types of transcription errors.
 * Each error type includes a message and provides a user-friendly message via toUserMessage().
 */
sealed class TranscriptionError {
    /**
     * Network connectivity error.
     */
    data class NetworkError(val message: String) : TranscriptionError()
    
    /**
     * API error with HTTP status code.
     */
    data class ApiError(val code: Int, val message: String) : TranscriptionError()
    
    /**
     * File-related error (not found, invalid format, etc.).
     */
    data class FileError(val message: String) : TranscriptionError()
    
    /**
     * Authentication/authorization error.
     */
    data class AuthError(val message: String) : TranscriptionError()
    
    /**
     * Request timeout error.
     */
    data class TimeoutError(val message: String) : TranscriptionError()
    
    /**
     * Validation error (invalid input, format, etc.).
     */
    data class ValidationError(val message: String) : TranscriptionError()
    
    /**
     * Converts the error to a user-friendly message in Chinese.
     */
    fun toUserMessage(): String {
        return when (this) {
            is NetworkError -> "网络连接失败：$message"
            is ApiError -> "API错误（$code）：$message"
            is FileError -> "文件错误：$message"
            is AuthError -> "认证失败：$message，请检查API密钥"
            is TimeoutError -> "请求超时：$message"
            is ValidationError -> "验证失败：$message"
        }
    }
}
