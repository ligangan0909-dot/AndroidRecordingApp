package com.example.recordingapp.data.network

import android.util.Log
import com.example.recordingapp.data.model.TranscriptionError
import com.example.recordingapp.data.model.TranscriptionException
import com.example.recordingapp.data.network.dto.TranscriptionResponse
import com.example.recordingapp.data.network.dto.WhisperTranscriptionResponse
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
import java.util.UUID

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
    
    override suspend fun transcribeAudio(
        audioFile: File,
        enableDiarization: Boolean,
        enableTaskExtraction: Boolean,
        enableSummary: Boolean
    ): Result<TranscriptionResponse> {
        return try {
            if (!audioFile.exists()) {
                return Result.failure(
                    TranscriptionException(TranscriptionError.FileError("文件不存在: ${audioFile.path}"))
                )
            }
            
            if (!audioFile.canRead()) {
                return Result.failure(
                    TranscriptionException(TranscriptionError.FileError("无法读取文件: ${audioFile.path}"))
                )
            }
            
            val maxSizeBytes = MAX_FILE_SIZE_MB * 1024 * 1024
            if (audioFile.length() > maxSizeBytes) {
                return Result.failure(
                    TranscriptionException(TranscriptionError.FileError("文件大小超过${MAX_FILE_SIZE_MB}MB限制"))
                )
            }
            
            Log.d(TAG, "Starting Whisper transcription for file: ${audioFile.name}")
            
            val filePart = createFilePart(audioFile)
            val modelPart = "whisper-1".toRequestBody(TEXT_PLAIN_MEDIA_TYPE)
            val languagePart = "zh".toRequestBody(TEXT_PLAIN_MEDIA_TYPE)
            
            val response = apiService.transcribeAudio(
                file = filePart,
                model = modelPart,
                language = languagePart
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "Transcription successful")
                    
                    val transcriptionResponse = TranscriptionResponse(
                        transcriptionId = UUID.randomUUID().toString(),
                        fullText = body.text,
                        language = "zh",
                        durationMs = 0L,
                        speakerSegments = emptyList(),
                        tasks = emptyList(),
                        summary = ""
                    )
                    
                    Result.success(transcriptionResponse)
                } else {
                    Log.e(TAG, "Response body is null")
                    Result.failure(
                        TranscriptionException(TranscriptionError.ApiError(response.code(), "响应体为空"))
                    )
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: response.message()
                Log.e(TAG, "API error: ${response.code()} - $errorMessage")
                Result.failure(TranscriptionException(mapHttpError(response.code(), errorMessage)))
            }
            
        } catch (e: CancellationException) {
            Log.d(TAG, "Transcription cancelled")
            throw e
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network error: Unknown host", e)
            Result.failure(
                TranscriptionException(TranscriptionError.NetworkError("无法连接到服务器，请检查网络连接"))
            )
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout error", e)
            Result.failure(
                TranscriptionException(TranscriptionError.TimeoutError("请求超时，请检查网络连接或稍后重试"))
            )
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            Result.failure(
                TranscriptionException(TranscriptionError.NetworkError("网络错误: ${e.message}"))
            )
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error: ${e.code()}", e)
            Result.failure(
                TranscriptionException(mapHttpError(e.code(), e.message()))
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Security error", e)
            Result.failure(
                TranscriptionException(TranscriptionError.AuthError(e.message ?: "认证失败"))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            Result.failure(
                TranscriptionException(TranscriptionError.NetworkError("未知错误: ${e.message}"))
            )
        }
    }
    
    override suspend fun validateApiKey(apiKey: String): Result<Boolean> {
        if (!apiKey.startsWith("sk-")) {
            return Result.success(false)
        }
        
        if (apiKey.length !in API_KEY_MIN_LENGTH..API_KEY_MAX_LENGTH) {
            return Result.success(false)
        }
        
        return Result.success(true)
    }
    
    override fun cancelTranscription() {
        Log.d(TAG, "Cancellation requested")
    }
    
    private fun createFilePart(audioFile: File): MultipartBody.Part {
        val requestBody = audioFile.asRequestBody(AUDIO_WAV_MEDIA_TYPE)
        return MultipartBody.Part.createFormData(
            name = "file",
            filename = audioFile.name,
            body = requestBody
        )
    }
    
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
        private const val DEFAULT_BASE_URL = "https://api.openai.com/v1/"
        private const val MAX_FILE_SIZE_MB = 25
        private const val API_KEY_MIN_LENGTH = 20
        private const val API_KEY_MAX_LENGTH = 200
        private val AUDIO_WAV_MEDIA_TYPE = "audio/wav".toMediaTypeOrNull()
        private val TEXT_PLAIN_MEDIA_TYPE = "text/plain".toMediaTypeOrNull()
    }
}
