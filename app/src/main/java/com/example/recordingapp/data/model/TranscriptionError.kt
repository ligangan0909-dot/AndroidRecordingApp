package com.example.recordingapp.data.model

/**
 * Sealed class hierarchy representing different types of transcription errors.
 * Each error type includes a message and provides a user-friendly message via toUserMessage().
 */
sealed class TranscriptionError(message: String) : Exception(message) {
    /**
     * Network connectivity error.
     */
    data class NetworkError(override val message: String) : TranscriptionError(message)
    
    /**
     * API error with HTTP status code.
     */
    data class ApiError(val code: Int, override val message: String) : TranscriptionError(message)
    
    /**
     * File-related error (not found, invalid format, etc.).
     */
    data class FileError(override val message: String) : TranscriptionError(message)
    
    /**
     * Authentication/authorization error.
     */
    data class AuthError(override val message: String) : TranscriptionError(message)
    
    /**
     * Request timeout error.
     */
    data class TimeoutError(override val message: String) : TranscriptionError(message)
    
    /**
     * Validation error (invalid input, format, etc.).
     */
    data class ValidationError(override val message: String) : TranscriptionError(message)
    
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
