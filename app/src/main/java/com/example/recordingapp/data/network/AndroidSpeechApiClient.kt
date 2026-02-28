package com.example.recordingapp.data.network

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.recordingapp.data.model.TranscriptionError
import com.example.recordingapp.data.network.dto.TranscriptionResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

class AndroidSpeechApiClient(
    private val context: Context
) : ITranscriptionApiClient {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var mediaPlayer: MediaPlayer? = null
    
    companion object {
        private const val TAG = "AndroidSpeechApiClient"
    }
    
    override suspend fun transcribeAudio(
        audioFile: File,
        enableDiarization: Boolean,
        enableTaskExtraction: Boolean,
        enableSummary: Boolean
    ): Result<TranscriptionResponse> = suspendCancellableCoroutine { continuation ->
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                continuation.resume(
                    Result.failure(
                        TranscriptionError.ValidationError("设备不支持语音识别功能")
                    )
                )
                return@suspendCancellableCoroutine
            }
            
            Log.d(TAG, "Starting transcription for: ${audioFile.name}")
            
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            
            val recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {}
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
                }
                
                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "音频录制错误"
                        SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                        SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                        SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙碌"
                        SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时"
                        else -> "未知错误: $error"
                    }
                    
                    Log.e(TAG, "Recognition error: $errorMessage")
                    cleanup()
                    
                    if (continuation.isActive) {
                        continuation.resume(
                            Result.failure(
                                TranscriptionError.UnknownError(errorMessage)
                            )
                        )
                    }
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val fullText = matches?.joinToString(" ") ?: ""
                    
                    Log.d(TAG, "Recognition results: $fullText")
                    cleanup()
                    
                    if (continuation.isActive) {
                        val response = TranscriptionResponse(
                            transcriptionId = UUID.randomUUID().toString(),
                            fullText = fullText,
                            language = "zh-CN",
                            durationMs = 0,
                            speakerSegments = emptyList(),
                            tasks = emptyList(),
                            summary = if (fullText.length > 100) fullText.take(100) + "..." else fullText
                        )
                        
                        continuation.resume(Result.success(response))
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {}
                
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
            
            speechRecognizer?.setRecognitionListener(recognitionListener)
            
            // Play audio file and recognize
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                
                setOnCompletionListener {
                    Log.d(TAG, "Audio playback completed")
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    cleanup()
                    
                    if (continuation.isActive) {
                        continuation.resume(
                            Result.failure(
                                TranscriptionError.FileError("无法播放音频文件")
                            )
                        )
                    }
                    true
                }
            }
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            // Start recognition
            mediaPlayer?.start()
            speechRecognizer?.startListening(intent)
            
            continuation.invokeOnCancellation {
                Log.d(TAG, "Transcription cancelled")
                cleanup()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during transcription", e)
            cleanup()
            
            if (continuation.isActive) {
                continuation.resume(
                    Result.failure(
                        TranscriptionError.UnknownError("转写失败: ${e.message}", e)
                    )
                )
            }
        }
    }
    
    override suspend fun validateCredentials(): Result<Boolean> {
        return Result.success(SpeechRecognizer.isRecognitionAvailable(context))
    }
    
    override fun cancelTranscription() {
        cleanup()
    }
    
    override fun getProviderName(): String = "Android Speech Recognizer (系统语音识别)"
    
    private fun cleanup() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}
