package com.example.recordingapp.data.network

import com.example.recordingapp.data.network.dto.TranscriptionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit service interface for DeepSeek API transcription endpoints.
 * Provides audio transcription with speaker diarization, task extraction, and summarization.
 */
interface DeepSeekApiService {
    
    /**
     * Upload audio file for transcription with optional feature flags.
     * 
     * @param file The audio file to transcribe (WAV format, 44.1kHz, 16-bit, mono)
     * @param enableDiarization Enable speaker diarization feature
     * @param enableTaskExtraction Enable automatic task extraction
     * @param enableSummary Enable content summarization
     * @return Response containing transcription results with speaker segments, tasks, and summary
     */
    @Multipart
    @POST("/audio/transcribe")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("enable_diarization") enableDiarization: RequestBody,
        @Part("enable_task_extraction") enableTaskExtraction: RequestBody,
        @Part("enable_summary") enableSummary: RequestBody
    ): Response<TranscriptionResponse>
}
