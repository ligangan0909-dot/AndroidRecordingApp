package com.example.recordingapp.data.model

/**
 * Configuration data class for DeepSeek API settings.
 * 
 * @param apiKey The DeepSeek API key (32-128 characters, alphanumeric with underscores and hyphens)
 * @param endpoint The API endpoint URL (must use HTTPS)
 * @param enableDiarization Whether to enable speaker diarization
 * @param enableTaskExtraction Whether to enable task extraction
 * @param enableSummary Whether to enable summary generation
 */
data class ApiKeyConfig(
    val apiKey: String,
    val endpoint: String = "https://api.deepseek.com/v1",
    val enableDiarization: Boolean = true,
    val enableTaskExtraction: Boolean = true,
    val enableSummary: Boolean = true
) {
    /**
     * Validates the API key configuration.
     * 
     * @return true if the configuration is valid, false otherwise
     * 
     * Validation rules:
     * - API key must be 32-128 characters long
     * - API key must contain only alphanumeric characters, underscores, and hyphens
     * - Endpoint must start with "https://"
     */
    fun isValid(): Boolean {
        return apiKey.length in 32..128 &&
               apiKey.matches(Regex("[A-Za-z0-9_-]+")) &&
               endpoint.startsWith("https://")
    }
}
