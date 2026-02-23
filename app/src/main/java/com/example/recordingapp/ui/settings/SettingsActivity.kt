package com.example.recordingapp.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.recordingapp.R
import com.example.recordingapp.RecordingApp
import com.example.recordingapp.data.security.IApiKeyManager
import com.example.recordingapp.databinding.ActivitySettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for API key configuration
 * Allows users to input and save their DeepSeek API key
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var apiKeyManager: IApiKeyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize with dependency injection
        val app = application as RecordingApp
        apiKeyManager = app.apiKeyManager

        setupUI()
        loadCurrentApiKey()
    }

    private fun setupUI() {
        binding.btnSave.setOnClickListener {
            saveApiKey()
        }

        binding.btnClear.setOnClickListener {
            clearApiKey()
        }
    }

    private fun loadCurrentApiKey() {
        lifecycleScope.launch {
            val apiKey = withContext(Dispatchers.IO) {
                apiKeyManager.getApiKey()
            }

            if (apiKey != null) {
                // Show masked API key for security
                val maskedKey = maskApiKey(apiKey)
                binding.etApiKey.setText(maskedKey)
                binding.tvStatus.text = getString(R.string.api_key_configured)
                binding.tvStatus.visibility = View.VISIBLE
            }
        }
    }

    private fun saveApiKey() {
        val apiKey = binding.etApiKey.text.toString().trim()

        if (apiKey.isEmpty()) {
            Toast.makeText(this, R.string.api_key_empty, Toast.LENGTH_SHORT).show()
            return
        }

        // Validate format
        if (!apiKeyManager.validateKeyFormat(apiKey)) {
            Toast.makeText(this, R.string.api_key_invalid_format, Toast.LENGTH_SHORT).show()
            return
        }

        // Save API key
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
                    Toast.makeText(
                        this@SettingsActivity,
                        R.string.api_key_saved,
                        Toast.LENGTH_SHORT
                    ).show()
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

    private fun clearApiKey() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                apiKeyManager.clearApiKey()
            }

            binding.etApiKey.text?.clear()
            binding.tvStatus.visibility = View.GONE
            Toast.makeText(
                this@SettingsActivity,
                R.string.api_key_cleared,
                Toast.LENGTH_SHORT
            ).show()
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
