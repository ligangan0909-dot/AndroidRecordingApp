package com.example.recordingapp.data.network

import com.example.recordingapp.data.security.IApiKeyManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for network interceptors.
 * 
 * Tests:
 * - AuthInterceptor: API key injection
 * - HttpsEnforcementInterceptor: HTTPS-only enforcement
 * - LoggingInterceptor: Sensitive data sanitization
 * - GzipInterceptor: Compression header addition
 */
class InterceptorsTest {
    
    private lateinit var apiKeyManager: IApiKeyManager
    private val testApiKey = "sk_test_1234567890abcdefghijklmnopqrstuvwxyz"
    
    @Before
    fun setup() {
        apiKeyManager = mockk<IApiKeyManager>()
        coEvery { apiKeyManager.getApiKey() } returns testApiKey
    }
    
    @Test
    fun `AuthInterceptor adds Authorization header`() = runTest {
        // Given
        val interceptor = AuthInterceptor(apiKeyManager)
        val chain = createMockChain("https://api.deepseek.com/test")
        
        // When
        val response = interceptor.intercept(chain)
        
        // Then
        val request = chain.request()
        assertEquals("Bearer $testApiKey", request.header("Authorization"))
        assertEquals("AndroidRecordingApp/1.0", request.header("User-Agent"))
    }
    
    @Test(expected = SecurityException::class)
    fun `AuthInterceptor throws when API key is missing`() = runTest {
        // Given
        coEvery { apiKeyManager.getApiKey() } returns null
        val interceptor = AuthInterceptor(apiKeyManager)
        val chain = createMockChain("https://api.deepseek.com/test")
        
        // When
        interceptor.intercept(chain)
        
        // Then - should throw SecurityException
    }
    
    @Test
    fun `HttpsEnforcementInterceptor allows HTTPS`() {
        // Given
        val interceptor = HttpsEnforcementInterceptor()
        val chain = createMockChain("https://api.deepseek.com/test")
        
        // When
        val response = interceptor.intercept(chain)
        
        // Then - should not throw
        assertNotNull(response)
    }
    
    @Test(expected = SecurityException::class)
    fun `HttpsEnforcementInterceptor blocks HTTP`() {
        // Given
        val interceptor = HttpsEnforcementInterceptor()
        val chain = createMockChain("http://api.deepseek.com/test")
        
        // When
        interceptor.intercept(chain)
        
        // Then - should throw SecurityException
    }
    
    @Test
    fun `LoggingInterceptor sanitizes Authorization header`() {
        // Given
        val interceptor = LoggingInterceptor()
        val headers = "Authorization: Bearer sk_test_secret123456"
        
        // When
        val sanitized = interceptor.javaClass.getDeclaredMethod(
            "sanitizeHeaders", 
            String::class.java
        ).apply { isAccessible = true }.invoke(interceptor, headers) as String
        
        // Then
        assertFalse("Should not contain actual API key", sanitized.contains("sk_test_secret123456"))
        assertTrue("Should contain masked value", sanitized.contains("***"))
    }
    
    @Test
    fun `GzipInterceptor adds Accept-Encoding header`() {
        // Given
        val interceptor = GzipInterceptor()
        val chain = createMockChain("https://api.deepseek.com/test")
        
        // When
        val response = interceptor.intercept(chain)
        
        // Then
        val request = chain.request()
        assertEquals("gzip", request.header("Accept-Encoding"))
    }
    
    /**
     * Creates a mock interceptor chain for testing.
     */
    private fun createMockChain(url: String): Interceptor.Chain {
        val request = Request.Builder()
            .url(url)
            .build()
        
        return object : Interceptor.Chain {
            private var modifiedRequest = request
            
            override fun request(): Request = modifiedRequest
            
            override fun proceed(request: Request): Response {
                modifiedRequest = request
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_2)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(null, ""))
                    .build()
            }
            
            override fun connection(): Connection? = null
            override fun call(): Call = mockk()
            override fun connectTimeoutMillis(): Int = 30000
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 120000
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 120000
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }
    }
}
