package com.example.recordingapp.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.recordingapp.R
import com.example.recordingapp.RecordingApp
import com.example.recordingapp.data.network.TranscriptionProviderManager
import com.example.recordingapp.data.security.SecureApiKeyManager
import com.example.recordingapp.databinding.ActivitySettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var providerManager: TranscriptionProviderManager
    private lateinit var apiKeyManager: SecureApiKeyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as RecordingApp
        providerManager = app.providerManager
        apiKeyManager = providerManager.getApiKeyManager()

        setupUI()
        loadCurrentConfig()
    }

    private fun setupUI() {
        binding.rgProvider.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbOpenAI -> showOpenAIConfig()
                R.id.rbDoubao -> showDoubaoConfig()
            }
        }

        binding.btnSave.setOnClickListener { saveConfig() }
        binding.btnValidate.setOnClickListener { validateConfig() }
        binding.btnClear.setOnClickListener { clearConfig() }
    }

    private fun loadCurrentConfig() {
        lifecycleScope.launch {
            val selectedProvider = providerManager.getSelectedProviderType()
            
            when (selectedProvider) {
                TranscriptionProviderManager.PROVIDER_OPENAI -> {
                    binding.rbOpenAI.isChecked = true
                    loadOpenAIConfig()
                }
                TranscriptionProviderManager.PROVIDER_DOUBAO -> {
                    binding.rbDoubao.isChecked = true
                    loadDoubaoConfig()
                }
            }
        }
    }

    private fun showOpenAIConfig() {
        binding.llOpenAIConfig.visibility = View.VISIBLE
        binding.llDoubaoConfig.visibility = View.GONE
        binding.tvStatus.visibility = View.GONE
    }

    private fun showDoubaoConfig() {
        binding.llOpenAIConfig.visibility = View.GONE
        binding.llDoubaoConfig.visibility = View.VISIBLE
        binding.tvStatus.visibility = View.GONE
    }

    private suspend fun loadOpenAIConfig() {
        val apiKey = withContext(Dispatchers.IO) {
            apiKeyManager.getApiKey()
        }

        if (apiKey != null) {
            val maskedKey = maskApiKey(apiKey)
            binding.etApiKey.setText(maskedKey)
            binding.tvStatus.text = getString(R.string.api_key_configured)
            binding.tvStatus.visibility = View.VISIBLE
        }
    }

    private suspend fun loadDoubaoConfig() {
        val config = withContext(Dispatchers.IO) {
            apiKeyManager.getDoubaoConfig()
        }

        if (config != null) {
            binding.etDoubaoAppId.setText(config.appId)
            binding.etDoubaoAccessKeyId.setText(config.accessKeyId)
            binding.etDoubaoSecretKey.setText(maskApiKey(config.secretKey))
            binding.etDoubaoClusterId.setText(config.clusterId)
            binding.tvStatus.text = getString(R.string.api_key_configured)
            binding.tvStatus.visibility = View.VISIBLE
        }
    }

    private fun saveConfig() {
        when {
            binding.rbOpenAI.isChecked -> saveOpenAIConfig()
            binding.rbDoubao.isChecked -> saveDoubaoConfig()
        }
    }

    private fun saveOpenAIConfig() {
        val apiKey = binding.etApiKey.text.toString().trim()

        if (apiKey.isEmpty()) {
            Toast.makeText(this, R.string.api_key_empty, Toast.LENGTH_SHORT).show()
            return
        }

        if (!apiKeyManager.validateKeyFormat(apiKey)) {
            Toast.makeText(this, R.string.api_key_invalid_format, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.btnSave.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            val result = withContext(Dispatchers.IO) {
                apiKeyManager.saveApiKey(apiKey)
            }

            binding.btnSave.isEnabled = true
            binding.progressBar.visibility = View.GONE

            result.fold(
                onSuccess = {
                    providerManager.setSelectedProviderType(TranscriptionProviderManager.PROVIDER_OPENAI)
                    Toast.makeText(this@SettingsActivity, R.string.api_key_saved, Toast.LENGTH_SHORT).show()
                    binding.tvStatus.text = getString(R.string.api_key_configured)
                    binding.tvStatus.visibility = View.VISIBLE
                },
                onFailure = { error ->
                    Toast.makeText(
                        this@SettingsActivity,
                        getString(R.string.api_key_save_failed, error.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun saveDoubaoConfig() {
        val appId = binding.etDoubaoAppId.text.toString().trim()
        val accessKeyId = binding.etDoubaoAccessKeyId.text.toString().trim()
        val secretKey = binding.etDoubaoSecretKey.text.toString().trim()
        val clusterId = binding.etDoubaoClusterId.text.toString().trim()

        if (appId.isEmpty() || accessKeyId.isEmpty() || secretKey.isEmpty() || clusterId.isEmpty()) {
            Toast.makeText(this, R.string.doubao_config_empty, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.btnSave.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            val result = withContext(Dispatchers.IO) {
                apiKeyManager.saveDoubaoConfig(appId, accessKeyId, secretKey, clusterId)
            }

            binding.btnSave.isEnabled = true
            binding.progressBar.visibility = View.GONE

            result.fold(
                onSuccess = {
                    providerManager.setSelectedProviderType(TranscriptionProviderManager.PROVIDER_DOUBAO)
                    Toast.makeText(this@SettingsActivity, R.string.doubao_config_saved, Toast.LENGTH_SHORT).show()
                    binding.tvStatus.text = getString(R.string.api_key_configured)
                    binding.tvStatus.visibility = View.VISIBLE
                },
                onFailure = { error ->
                    Toast.makeText(
                        this@SettingsActivity,
                        getString(R.string.api_key_save_failed, error.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun validateConfig() {
        lifecycleScope.launch {
            binding.btnValidate.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.tvStatus.text = getString(R.string.validating)
            binding.tvStatus.visibility = View.VISIBLE

            val providerType = if (binding.rbOpenAI.isChecked) {
                TranscriptionProviderManager.PROVIDER_OPENAI
            } else {
                TranscriptionProviderManager.PROVIDER_DOUBAO
            }

            val result = withContext(Dispatchers.IO) {
                providerManager.validateProvider(providerType)
            }

            binding.btnValidate.isEnabled = true
            binding.progressBar.visibility = View.GONE

            result.fold(
                onSuccess = { isValid ->
                    if (isValid) {
                        binding.tvStatus.text = getString(R.string.credentials_valid)
                        binding.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                        Toast.makeText(this@SettingsActivity, R.string.credentials_valid, Toast.LENGTH_SHORT).show()
                    } else {
                        binding.tvStatus.text = getString(R.string.credentials_invalid)
                        binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                        Toast.makeText(this@SettingsActivity, R.string.credentials_invalid, Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { error ->
                    binding.tvStatus.text = getString(R.string.credentials_invalid)
                    binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                    Toast.makeText(
                        this@SettingsActivity,
                        getString(R.string.api_key_save_failed, error.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun clearConfig() {
        lifecycleScope.launch {
            when {
                binding.rbOpenAI.isChecked -> {
                    withContext(Dispatchers.IO) {
                        apiKeyManager.clearApiKey()
                    }
                    binding.etApiKey.text?.clear()
                    Toast.makeText(this@SettingsActivity, R.string.api_key_cleared, Toast.LENGTH_SHORT).show()
                }
                binding.rbDoubao.isChecked -> {
                    withContext(Dispatchers.IO) {
                        apiKeyManager.clearDoubaoConfig()
                    }
                    binding.etDoubaoAppId.text?.clear()
                    binding.etDoubaoAccessKeyId.text?.clear()
                    binding.etDoubaoSecretKey.text?.clear()
                    binding.etDoubaoClusterId.text?.clear()
                    Toast.makeText(this@SettingsActivity, R.string.doubao_config_cleared, Toast.LENGTH_SHORT).show()
                }
            }

            binding.tvStatus.visibility = View.GONE
        }
    }

    private fun maskApiKey(apiKey: String): String {
        return if (apiKey.length > 8) {
            "${apiKey.take(4)}${"*".repeat(apiKey.length - 8)}${apiKey.takeLast(4)}"
        } else {
            "*".repeat(apiKey.length)
        }
    }
}
