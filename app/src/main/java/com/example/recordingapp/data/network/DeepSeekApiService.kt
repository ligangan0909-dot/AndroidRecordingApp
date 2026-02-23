package com.example.recordingapp.data.network

import com.example.recordingapp.data.network.dto.WhisperTranscriptionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit service interface for OpenAI Whisper API transcription endpoint.
 */
interface DeepSeekApiService {
    
    /**
     * Upload audio file for transcription using Whisper API.
     * 
     * @param file The audio file to transcribe (WAV format, max 25MB)
     * @param model The Whisper model to use (whisper-1)
     * @param language Optional language code (e.g., "zh" for Chinese)
     * @return Response containing transcription text
     */
    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("language") language: RequestBody? = null
    ): Response<WhisperTranscriptionResponse>
}
