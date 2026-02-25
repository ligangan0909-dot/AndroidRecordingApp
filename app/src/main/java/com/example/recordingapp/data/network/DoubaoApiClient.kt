package com.example.recordingapp.data.network

import android.util.Log
import com.example.recordingapp.data.model.TranscriptionError
import com.example.recordingapp.data.network.dto.TranscriptionResponse
import com.example.recordingapp.data.security.SecureApiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.cancellation.CancellationException

class DoubaoApiClient(
    private val apiKeyManager: SecureApiKeyManager,
    private val networkManager: SecureNetworkManager
) : ITranscriptionApiClient {
    
    private var currentCall: okhttp3.Call? = null
    
    companion object {
        private const val TAG = "DoubaoApiClient"
        private const val BASE_URL = "https://openspeech.bytedance.com/api/v1/auc"
    }
    
    override suspend fun transcribeAudio(
        audioFile: File,
        enableDiarization: Boolean,
        enableTaskExtraction: Boolean,
        enableSummary: Boolean
    ): Result<TranscriptionResponse> = withContext(Dispatchers.IO) {
        try {
            val config = apiKeyManager.getDoubaoConfig()
                ?: return@withContext Result.failure(
                    TranscriptionError.AuthError("Doubao credentials not configured")
                )
            
            if (!audioFile.exists() || !audioFile.canRead()) {
                return@withContext Result.failure(
                    TranscriptionError.FileError("Audio file not found or not readable")
                )
            }
            
            Log.d(TAG, "Starting transcription: ${audioFile.name}, size: ${audioFile.length()}")
            
            val client = networkManager.createSecureHttpClient()
            val requestBody = buildMultipartRequest(audioFile, config)
            val request = buildRequest(requestBody, config)
            
            currentCall = client.newCall(request)
            val response = currentCall?.execute()
            
            if (response == null || !response.isSuccessful) {
                val errorBody = response?.body?.string() ?: "Unknown error"
                Log.e(TAG, "Transcription failed: ${response?.code} - $errorBody")
                return@withContext Result.failure(
                    TranscriptionError.ApiError(
                        response?.code ?: 500,
                        "Transcription failed: $errorBody"
                    )
                )
            }
            
            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(
                    TranscriptionError.ApiError(500, "Empty response body")
                )
            
            Log.d(TAG, "Transcription response received")
            
            val transcriptionResponse = parseTranscriptionResponse(responseBody)
            Result.success(transcriptionResponse)
            
        } catch (e: CancellationException) {
            Log.d(TAG, "Transcription cancelled")
            Result.failure(TranscriptionError.NetworkError("Transcription cancelled"))
        } catch (e: IOException) {
            Log.e(TAG, "Network error during transcription", e)
            Result.failure(TranscriptionError.NetworkError(e.message ?: "Network error"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during transcription", e)
            Result.failure(TranscriptionError.NetworkError(e.message ?: "Unknown error"))
        } finally {
            currentCall = null
        }
    }
    
    override suspend fun validateCredentials(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val hasConfig = apiKeyManager.hasDoubaoConfig()
            Result.success(hasConfig)
        } catch (e: Exception) {
            Log.e(TAG, "Error validating credentials", e)
            Result.failure(e)
        }
    }
    
    override fun cancelTranscription() {
        currentCall?.cancel()
        currentCall = null
    }
    
    override fun getProviderName(): String = "Doubao (豆包)"
    
    private fun buildMultipartRequest(
        audioFile: File, 
        config: SecureApiKeyManager.DoubaoConfig
    ): MultipartBody {
        val audioRequestBody = audioFile.asRequestBody("audio/wav".toMediaType())
        
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("appid", config.appId)
            .addFormDataPart("cluster", config.clusterId)
            .addFormDataPart("format", "wav")
            .addFormDataPart("language", "zh-CN")
            .addFormDataPart("show_utterances", "true")
            .addFormDataPart("data", audioFile.name, audioRequestBody)
            .build()
    }
    
    private fun buildRequest(
        body: MultipartBody, 
        config: SecureApiKeyManager.DoubaoConfig
    ): Request {
        val timestamp = System.currentTimeMillis() / 1000
        val nonce = UUID.randomUUID().toString()
        val signature = generateSignature(
            config.accessKeyId, 
            config.secretKey, 
            timestamp, 
            nonce
        )
        
        return Request.Builder()
            .url(BASE_URL)
            .post(body)
            .addHeader("X-Api-Access-Key", config.accessKeyId)
            .addHeader("X-Api-Timestamp", timestamp.toString())
            .addHeader("X-Api-Nonce", nonce)
            .addHeader("X-Api-Signature", signature)
            .build()
    }
    
    private fun generateSignature(
        accessKeyId: String,
        secretKey: String,
        timestamp: Long,
        nonce: String
    ): String {
        val message = "$accessKeyId$timestamp$nonce"
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        mac.init(secretKeySpec)
        val hash = mac.doFinal(message.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    private fun parseTranscriptionResponse(responseBody: String): TranscriptionResponse {
        val json = JSONObject(responseBody)
        val utterances = json.optJSONArray("utterances")
        val fullText = StringBuilder()
        
        if (utterances != null) {
            for (i in 0 until utterances.length()) {
                val utterance = utterances.getJSONObject(i)
                val text = utterance.optString("text", "")
                if (text.isNotBlank()) {
                    fullText.append(text).append(" ")
                }
            }
        }
        
        val transcriptionText = fullText.toString().trim()
        val summary = if (transcriptionText.length > 100) {
            transcriptionText.take(100) + "..."
        } else {
            transcriptionText
        }
        
        return TranscriptionResponse(
            transcriptionId = UUID.randomUUID().toString(),
            fullText = transcriptionText,
            language = "zh-CN",
            durationMs = json.optLong("duration", 0),
            speakerSegments = emptyList(),
            tasks = emptyList(),
            summary = summary
        )
    }
}
