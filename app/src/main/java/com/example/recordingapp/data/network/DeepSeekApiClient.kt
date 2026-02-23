package com.example.recordingapp.data.network

import android.util.Log
import com.example.recordingapp.data.model.TranscriptionError
import com.example.recordingapp.data.network.dto.TranscriptionResponse
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of DeepSeek API client for audio transcription.
 * 
 * This client handles:
 * - Multipart file upload for audio transcription
 * - API key authentication via OkHttp interceptors
 * - Error handling and mapping to TranscriptionError types
 * - Request cancellation
 * - Timeout configuration (30s connect, 120s read/write)
 * 
 * The client uses Retrofit with OkHttp for network operations and provides
 * explicit error handling through Result<T> return types.
 * 
 * @param okHttpClient Configured OkHttpClient with security interceptors
 * @param baseUrl Base URL for the DeepSeek API (default: https://api.deepseek.com/v1)
 */
class DeepSeekApiClient(
    private val okHttpClient: OkHttpClient,
    private val baseUrl: String = DEFAULT_BASE_URL
) : IDeepSeekApiClient {
    
    private val apiService: DeepSeekApiService
    
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(DeepSeekApiService::class.java)
    }
    
    /**
     * Transcribes an audio file using the DeepSeek API.
     * 
     * This method:
     * 1. Validates the audio file exists and is readable
     * 2. Creates a multipart request with the file and feature flags
     * 3. Uploads the file to the API
     * 4. Parses and returns the transcription response
     * 
     * @param audioFile The audio file to transcribe
     * @param enableDiarization Enable speaker diarization
     * @param enableTaskExtraction Enable task extraction
     * @param enableSummary Enable summary generation
     * @return Result containing TranscriptionResponse or error
     */
    override suspend fun transcribeAudio(
        audioFile: File,
        enableDiarization: Boolean,
        enableTaskExtraction: Boolean,
        enableSummary: Boolean
    ): Result<TranscriptionResponse> {
        return try {
            // Validate file exists and is readable
            if (!audioFile.exists()) {
                return Result.failure(
                    TranscriptionError.FileError("文件不存在: ${audioFile.path}")
                )
            }
            
            if (!audioFile.canRead()) {
                return Result.failure(
                    TranscriptionError.FileError("无法读取文件: ${audioFile.path}")
                )
            }
            
            // Check file size (max 25MB)
            val maxSizeBytes = MAX_FILE_SIZE_MB * 1024 * 1024
            if (audioFile.length() > maxSizeBytes) {
                return Result.failure(
                    TranscriptionError.FileError("文件大小超过${MAX_FILE_SIZE_MB}MB限制")
                )
            }
            
            Log.d(TAG, "Starting transcription for file: ${audioFile.name}, size: ${audioFile.length()} bytes")
            
            // Create multipart request body
            val filePart = createFilePart(audioFile)
            val diarizationPart = enableDiarization.toString().toRequestBody(TEXT_PLAIN_MEDIA_TYPE)
            val taskExtractionPart = enableTaskExtraction.toString().toRequestBody(TEXT_PLAIN_MEDIA_TYPE)
            val summaryPart = enableSummary.toString().toRequestBody(TEXT_PLAIN_MEDIA_TYPE)
            
            // Make API request
            val response = apiService.transcribeAudio(
                file = filePart,
                enableDiarization = diarizationPart,
                enableTaskExtraction = taskExtractionPart,
                enableSummary = summaryPart
            )
            
            // Handle response
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "Transcription successful: ${body.transcriptionId}")
                    Result.success(body)
                } else {
                    Log.e(TAG, "Response body is null")
                    Result.failure(
                        TranscriptionError.ApiError(response.code(), "响应体为空")
                    )
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: response.message()
                Log.e(TAG, "API error: ${response.code()} - $errorMessage")
                Result.failure(mapHttpError(response.code(), errorMessage))
            }
            
        } catch (e: CancellationException) {
            Log.d(TAG, "Transcription cancelled")
            throw e // Re-throw to propagate cancellation
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network error: Unknown host", e)
            Result.failure(
                TranscriptionError.NetworkError("无法连接到服务器，请检查网络连接")
            )
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout error", e)
            Result.failure(
                TranscriptionError.TimeoutError("请求超时，请检查网络连接或稍后重试")
            )
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            Result.failure(
                TranscriptionError.NetworkError("网络错误: ${e.message}")
            )
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error: ${e.code()}", e)
            Result.failure(
                mapHttpError(e.code(), e.message())
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Security error", e)
            Result.failure(
                TranscriptionError.AuthError(e.message ?: "认证失败")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            Result.failure(
                TranscriptionError.NetworkError("未知错误: ${e.message}")
            )
        }
    }
    
    /**
     * Validates an API key by attempting a lightweight API request.
     * 
     * Note: This is a simplified implementation. In a real scenario, you would
     * make a test request to a validation endpoint or a lightweight endpoint.
     * 
     * @param apiKey The API key to validate
     * @return Result containing true if valid, false otherwise
     */
    override suspend fun validateApiKey(apiKey: String): Result<Boolean> {
        // Basic format validation
        if (apiKey.length !in API_KEY_MIN_LENGTH..API_KEY_MAX_LENGTH) {
            return Result.success(false)
        }
        
        if (!apiKey.matches(API_KEY_REGEX)) {
            return Result.success(false)
        }
        
        // In a real implementation, you would make a test API request here
        // For now, we just validate the format
        return Result.success(true)
    }
    
    /**
     * Cancels any in-progress transcription operation.
     * 
     * This is handled by the coroutine cancellation mechanism.
     * When the coroutine is cancelled, the network request will be terminated.
     */
    override fun cancelTranscription() {
        Log.d(TAG, "Cancellation requested - will be handled by coroutine cancellation")
        // Cancellation is handled by the calling coroutine scope
        // The OkHttp client will automatically cancel the request when the coroutine is cancelled
    }
    
    /**
     * Creates a multipart file part for the audio file.
     */
    private fun createFilePart(audioFile: File): MultipartBody.Part {
        val requestBody = audioFile.asRequestBody(AUDIO_WAV_MEDIA_TYPE)
        return MultipartBody.Part.createFormData(
            name = "file",
            filename = audioFile.name,
            body = requestBody
        )
    }
    
    /**
     * Maps HTTP error codes to appropriate TranscriptionError types.
     */
    private fun mapHttpError(code: Int, message: String): TranscriptionError {
        return when (code) {
            401 -> TranscriptionError.AuthError("API密钥无效或已过期")
            403 -> TranscriptionError.AuthError("权限不足，请检查API密钥权限")
            429 -> TranscriptionError.ApiError(code, "请求过于频繁，请稍后再试")
            in 400..499 -> TranscriptionError.ApiError(code, "客户端错误: $message")
            in 500..599 -> TranscriptionError.ApiError(code, "服务器错误，请稍后重试")
            else -> TranscriptionError.ApiError(code, message)
        }
    }
    
    companion object {
        private const val TAG = "DeepSeekApiClient"
        private const val DEFAULT_BASE_URL = "https://api.deepseek.com/v1/"
        private const val MAX_FILE_SIZE_MB = 25
        
        // API key validation
        private const val API_KEY_MIN_LENGTH = 32
        private const val API_KEY_MAX_LENGTH = 128
        private val API_KEY_REGEX = Regex("[A-Za-z0-9_-]+")
        
        // Media types
        private val AUDIO_WAV_MEDIA_TYPE = "audio/wav".toMediaTypeOrNull()
        private val TEXT_PLAIN_MEDIA_TYPE = "text/plain".toMediaTypeOrNull()
    }
}
