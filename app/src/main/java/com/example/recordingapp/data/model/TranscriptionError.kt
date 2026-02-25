package com.example.recordingapp.data.model

/**
 * Sealed class hierarchy representing different types of transcription errors.
 */
sealed class TranscriptionError(message: String, val cause: Throwable? = null) : Exception(message, cause) {
    data class NetworkError(override val message: String, override val cause: Throwable? = null) : TranscriptionError(message, cause)
    data class ApiError(val code: Int, override val message: String) : TranscriptionError(message)
    data class FileError(override val message: String) : TranscriptionError(message)
    data class AuthError(override val message: String) : TranscriptionError(message)
    data class AuthenticationError(override val message: String) : TranscriptionError(message)
    data class TimeoutError(override val message: String) : TranscriptionError(message)
    data class ValidationError(override val message: String) : TranscriptionError(message)
    data class UnknownError(override val message: String, override val cause: Throwable? = null) : TranscriptionError(message, cause)
    
    fun toUserMessage(): String {
        return when (this) {
            is NetworkError -> "网络连接失败：$message"
            is ApiError -> "API错误（$code）：$message"
            is FileError -> "文件错误：$message"
            is AuthError -> "认证失败：$message，请检查API密钥"
            is AuthenticationError -> "认证失败：$message，请检查API密钥"
            is TimeoutError -> "请求超时：$message"
            is ValidationError -> "验证失败：$message"
            is UnknownError -> "未知错误：$message"
        }
    }
}
