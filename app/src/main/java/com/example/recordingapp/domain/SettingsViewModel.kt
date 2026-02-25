package com.example.recordingapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordingapp.data.model.ApiKeyConfig
import com.example.recordingapp.data.security.IApiKeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing transcription API settings
 */
class SettingsViewModel(
    private val apiKeyManager: IApiKeyManager
) : ViewModel() {

    // API configuration state
    private val _openAiApiKey = MutableStateFlow("")
    val openAiApiKey: StateFlow<String> = _openAiApiKey.asStateFlow()

    private val _doubaoApiKey = MutableStateFlow("")
    val doubaoApiKey: StateFlow<String> = _doubaoApiKey.asStateFlow()

    private val _selectedProvider = MutableStateFlow("openai")
    val selectedProvider: StateFlow<String> = _selectedProvider.asStateFlow()

    // Feapackage com.example.recordingapp.ule
import androidx.lifecycle.ViewModel
importal import androidx.lifecycle.viewModeanimport com.example.recordingaeFlow()

    private val _enableTaskExtraction = MutableStateFlow(false)
    val enableTaskExtraction: StateFlow<Boolean> = _enableTaskExtraction.asStateFlow()

    private val _enableSummary = MutableStateFlow(true)
    val enableSummary: StateFlow<Boolean> = _enableSummary.asStateFlow()

    // UI state
    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    init {
        loadConfiguration()
    }

    /**
     * Load saved configuration
     */
    private fun loadConfiguration() {
        viewModelScope.launch {
            try {
                // Load OpenAI API key
                val openAiKey = apiKeyManager.getApiKey("openai")
                _openAiApiKey.value = openAiKey ?: ""

                // Load Doubao API key
                val doubaoKey = apiKeyManager.getApiKey("doubao")
                _doubaoApiKey.value = doubaoKey ?: ""

                // Load selected provider
                val provider = apiKeyManager.getSelectedProvider()
                _selectedProvider.value = provider

                // Load feature toggles (from SharedPreferences or default values)
                // For now, using default values
                _enableDiarization.value = false
                _enableTaskExtraction.value = false
                _enableSummary.value = true
            } catch (e: Exception) {
                _validationError.value = "加载配置失败：${e.message}"
            }
        }
    }

    /**
     * Update OpenAI API key
     */
    fun updateOpenAiApiKey(key: String) {
        _openAiApiKey.value = key
        _validationError.value = null
    }

    /**
     * Update Doubao API key
     */
    fun updateDoubaoApiKey(key: String) {
        _doubaoApiKey.value = key
        _validationError.value = null
    }

    /**
     * Update selected provider
     */
    fun updateSelectedProvider(provider: String) {
        _selectedProvider.value = provider
        _validationError.value = null
    }

    /**
     * Update feature toggles
     */
    fun updateDiarizationEnabled(enabled: Boolean) {
        _enableDiarization.value = enabled
    }

    fun updateTaskExtractionEnabled(enabled: Boolean) {
        _enableTaskExtraction.value = enabled
    }

    fun updateSummaryEnabled(enabled: Boolean) {
        _enableSummary.value = enabled
    }

    /**
     * Validate and save configuration
     */
    fun validateAndSave() {
        viewModelScope.launch {
            try {
                _saveStatus.value = SaveStatus.Saving
                _validationError.value = null

                // Validate selected provider has API key
                val currentProvider = _selectedProvider.value
                val currentKey = when (currentProvider) {
                    "openai" -> _openAiApiKey.value
                    "doubao" -> _doubaoApiKey.value
                    else -> ""
                }

                if (currentKey.isBlank()) {
                    _validationError.value = "请输入 API 密钥"
                    _saveStatus.value = SaveStatus.Error("请输入 API 密钥")
                    return@launch
                }

                // Validate API key format
                if (!apiKeyManager.validateKeyFormat(currentKey)) {
                    _validationError.value = "API 密钥格式无效"
                    _saveStatus.value = SaveStatus.Error("API 密钥格式无效")
                    return@launch
                }

                // Save OpenAI API key if provided
                if (_openAiApiKey.value.isNotBlank()) {
                    apiKeyManager.saveApiKey(_openAiApiKey.value, "openai")
                }

                // Save Doubao API key if provided
                if (_doubaoApiKey.value.isNotBlank()) {
                    apiKeyManager.saveApiKey(_doubaoApiKey.value, "doubao")
                }

                // Save selected provider
                apiKeyManager.saveSelectedProvider(_selectedProvider.value)

                // TODO: Save feature toggles to SharedPreferences

                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _validationError.value = "保存失败：${e.message}"
                _saveStatus.value = SaveStatus.Error(e.message ?: "保存失败")
            }
        }
    }

    /**
     * Clear API key for specific provider
     */
    fun clearApiKey(provider: String) {
        viewModelScope.launch {
            try {
                apiKeyManager.clearApiKey(provider)
                when (provider) {
                    "openai" -> _openAiApiKey.value = ""
                    "doubao" -> _doubaoApiKey.value = ""
                }
                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _validationError.value = "清除失败：${e.message}"
                _saveStatus.value = SaveStatus.Error(e.message ?: "清除失败")
            }
        }
    }

    /**
     * Reset save status
     */
    fun resetSaveStatus() {
        _saveStatus.value = SaveStatus.Idle
    }

    /**
     * Clear validation error
     */
    fun clearValidationError() {
        _validationError.value = null
    }
}

/**
 * Save status sealed class
 */
sealed class SaveStatus {
    object Idle : SaveStatus()
    object Saving : SaveStatus()
    object Success : SaveStatus()
    data class Error(val message: String) : SaveStatus()
}
