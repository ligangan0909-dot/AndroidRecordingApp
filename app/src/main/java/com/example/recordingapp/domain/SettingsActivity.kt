package com.example.recordingapp.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.recordingapp.R
import com.example.recordingapp.RecordingApp
import com.example.recordingapp.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get ViewModel from Application
        viewModel = (application as RecordingApp).settingsViewModel
        
        setupUI()
        observeViewModel()
    }
    
    private f    private f    private f    private f    priv      binding.providerRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioOpenAI -> {
                    viewModel.updateSelectedProvider("openai")
                    showOpenAIConfig()
                }
                R.id.radioDoubao -> {
                    viewModel.updateSelectedProvider("doubao")
                    showDoubaoConfig()
                }
            }
        }
        
        // Save button
        binding.btnSave.setOnClickListener {
            saveConfiguration()
        }
        
        // Clear buttons
        binding.btnClearOpenAI.setOnClickListener {
            viewModel.clearApiKey("openai")
            binding.etOpenAIKey.setText("")
            Toast.makeText(this, "OpenAI API 密钥已清除", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnClearDoubao.setOnClickListener {
            viewModel.clearApiKey("doubao")
            binding.etDoubaoKey.setText("")
            Toast.makeText(this, "豆包 API 密钥已清除", Toast.LENGTH_SHORT).show()
        }
        
        // Feature toggles
        binding.switchDiarization.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateDiarizationEnabled(isChecked)
        }
        
        binding.switchTaskExtraction.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTaskExtractionEnabled(isChecked)
        }
        
        binding.switchSummary.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSummaryEnabled(isChecked)
        }
    }
    
    private fun observeViewModel() {
        // Observe OpenAI API key
        lifecycleScope.launch {
            viewModel.openAiApiKey.collect { key ->
                if (binding.etOpenAIKey.text.toString() != key) {
                    binding.etOpenAIKey.setText(key)
                }
            }
        }
        
        // Observe Doubao API key
        lifecycleScope.launch {
            viewModel.doubaoApiKey.collect { key ->
                if (binding.etDoubaoKey.text.toString() != key) {
                    binding.etDoubaoKey.setText(key)
                }
            }
        }
        
        // Observe selected provider
        lifecycleScope.launch {
            viewModel.selectedProvider.collect { provider ->
                when (provider) {
                    "openai" -> {
                        binding.radioOpenAI.isChecked = true
                        showOpenAIConfig()
                    }
                    "doubao" -> {
                        binding.radioDoubao.isChecked = true
                        showDoubaoConfig()
                    }
                }
            }
        }
        
        // Observe feature toggles
        lifecycleScope.launch {
            viewModel.enableDiarization.collect { enabled ->
                binding.switchDiarization.isChecked = enabled
            }
        }
        
        lifecycleScope.launch {
            viewModel.enableTaskExtraction.collect { enabled ->
                binding.switchTaskExtraction.isChecked = enabled
            }
        }
        
        lifecycleScope.launch {
            viewModel.enableSummary.collect { enabled ->
                binding.switchSummary.isChecked = enabled
            }
        }
        
        // Observe save status
        lifecycleScope.launch {
            viewModel.saveStatus.collect { status ->
                when (status) {
                    is SaveStatus.Idle -> {
                        binding.btnSave.isEnabled = true
                        binding.btnSave.text = "保存配置"
                    }
                    is SaveStatus.Saving -> {
                        binding.btnSave.isEnabled = false
                        binding.btnSave.text = "保存中..."
                    }
                    is SaveStatus.Success -> {
                        binding.btnSave.isEnabled = true
                        binding.btnSave.text = "保存配置"
                        Toast.makeText(this@SettingsActivity, "配置已保存", Toast.LENGTH_SHORT).show()
                        viewModel.resetSaveStatus()
                    }
                    is SaveStatus.Error -> {
                        binding.btnSave.isEnabled = true
                        binding.btnSave.text = "保存配置"
                        Toast.makeText(this@SettingsActivity, "保存失败: ${status.message}", Toast.LENGTH_LONG).show()
                        viewModel.resetSaveStatus()
                    }
                }
            }
        }
        
        // Observe validation errors
        lifecycleScope.launch {
            viewModel.validationError.collect { error ->
                if (error != null) {
                    Toast.makeText(this@SettingsActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun saveConfiguration() {
        // Update API keys from input fields
        val openAiKey = binding.etOpenAIKey.text.toString()
        val doubaoKey = binding.etDoubaoKey.text.toString()
        
        viewModel.updateOpenAiApiKey(openAiKey)
        viewModel.updateDoubaoApiKey(doubaoKey)
        
        // Validate and save
        viewModel.validateAndSave()
    }
    
    private fun showOpenAIConfig() {
        binding.layoutOpenAIConfig.visibility = android.view.View.VISIBLE
        binding.layoutDoubaoConfig.visibility = android.view.View.GONE
    }
    
    private fun showDoubaoConfig() {
        binding.layoutOpenAIConfig.visibility = android.view.View.GONE
        binding.layoutDoubaoConfig.visibility = android.view.View.VISIBLE
    }
}
