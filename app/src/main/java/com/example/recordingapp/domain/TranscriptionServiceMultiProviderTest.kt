package com.example.recordingapp.domain

import com.example.recordingapp.data.model.TranscriptionResult
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.data.network.ITranscriptionApiClient
import com.example.recordingapp.data.network.TranscriptionProviderManager
import com.example.recordingapp.data.network.dto.TranscriptionResponse
import com.example.recordingapp.data.repository.ITranscriptionRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranscriptionServiceMultiProviderTest {

    private lateinit var providerManager: TranscriptionProviderManager
    private lateinit var repository: ITranscriptionRepository
    private late    private lateClient: ITranscriptionApiClient
    private lateinit var service: TranscriptionService

    @Before
    fun setup() {
        providerManager = mock(TranscriptionProviderManager::class.java)
        repository = mock(ITranscriptionRepository::class.java)
        mockApiClient = mock(ITranscriptionApiClient::class.java)
        
        // Setup mock provider manager to return mock API client
        whenever(providerManager.getProvider()).thenReturn(mockApiClient)
        whenever(providerManager.getSelectedProviderType()).thenReturn("openai")
        whenever(mockApiClient.getProviderName()).thenReturn("OpenAI Whisper")
        
        service = TranscriptionService(providerManager, repository)
    }

    @Test
    fun `transcribe uses provider from manager`() = runTest {
        // Given
        val testFile = File.createTempFile("test", ".wav")
        testFile.writeText("test audio data")
        
        val mockResponse = TranscriptionResponse(
            transcriptionId = "test-123",
            fullText = "Test transcription",
            summary = "Test summary",
            language = "en",
            durationMs = 5000
        )
        
        whenever(mockApiClient.transcribeAudio(any(), any(), any(), any()))
            .thenReturn(Result.success(mockResponse))
        
        // When
        val states = service.transcribe("rec-1", testFile.absolutePath).toList()
        
        // Then
        verify(providerManager).getProvider()
        verify(mockApiClient).transcribeAudio(any(), any(), any(), any())
        assertTrue(states.any { it is TranscriptionState.Success })
        
        testFile.delete()
    }

    @Test
    fun `service switches provider when manager changes`() = runTest {
        // Given
        val doubaoClient = mock(ITranscriptionApiClient::class.java)
        whenever(doubaoClient.getProviderName()).thenReturn("Doubao")
        
        // Simulate provider change
        whenever(providerManager.getSelectedProviderType()).thenReturn("doubao")
        whenever(providerManager.getProvider()).thenReturn(doubaoClient)
        
        val testFile = File.createTempFile("test", ".wav")
        testFile.writeText("test audio data")
        
        val mockResponse = TranscriptionResponse(
            transcriptionId = "test-456",
            fullText = "Test transcription with Doubao",
            summary = "Test summary",
            language = "zh",
            durationMs = 5000
        )
        
        whenever(doubaoClient.transcribeAudio(any(), any(), any(), any()))
            .thenReturn(Result.success(mockResponse))
        
        // When
        val states = service.transcribe("rec-2", testFile.absolutePath).toList()
        
        // Then
        verify(providerManager).getProvider()
        verify(doubaoClient).transcribeAudio(any(), any(), any(), any())
        assertTrue(states.any { it is TranscriptionState.Success })
        
        testFile.delete()
    }
}
