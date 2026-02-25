package com.example.recordingapp.ui.transcription

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.recordingapp.R
import com.example.recordingapp.RecordingApp
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.databinding.ActivityTranscriptionBinding
import com.example.recordingapp.domain.ErrorLogger
import kotlinx.coroutines.launch

class TranscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTranscriptionBinding
    private lateinit var viewModel: TranscriptionViewModel
    private var recordingId: String? = null
    private var audioFilePath: String? = null

    companion object {
        const val EXTRA_RECORDING_ID = "recording_id"
        const val EXTRA_AUDIO_FILE_PATH = "audio_file_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recordingId = intent.getStringExtra(EXTRA_RECORDING_ID)
        audioFilePath = intent.getStringExtra(EXTRA_AUDIO_FILE_PATH)

        if (recordingId == null || audioFilePath == null) {
            Toast.makeText(this, "无效的录音数据", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize ViewModel
        val app = application as RecordingApp
        val factory = TranscriptionViewModelFactory(app.transcriptionService)
        viewModel = ViewModelProvider(this, factory)[TranscriptionViewModel::class.java]

        setupUI()
        observeState()

        // Start transcription
        viewModel.startTranscription(recordingId!!, audioFilePath!!)
    }

    private fun setupUI() {
        binding.btnCancel.setOnClickListener {
            showCancelConfirmationDialog()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.retryTranscription()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.transcriptionState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: TranscriptionState) {
        when (state) {
            is TranscriptionState.Idle -> {
                showProgress(false)
                showResult(false)
                showError(false)
            }

            is TranscriptionState.Uploading -> {
                showProgress(true)
                binding.tvProgressStatus.text = "上传中: ${state.progress}%"
                binding.progressBar.isIndeterminate = false
                binding.progressBar.progress = state.progress
                binding.btnCancel.isEnabled = true
                showResult(false)
                showError(false)
            }

            is TranscriptionState.Processing -> {
                showProgress(true)
                binding.tvProgressStatus.text = "处理中..."
                binding.progressBar.isIndeterminate = true
                binding.btnCancel.isEnabled = true
                showResult(false)
                showError(false)
            }

            is TranscriptionState.Success -> {
                showProgress(false)
                showResult(true)
                showError(false)
                displayResult(state)
            }

            is TranscriptionState.Error -> {
                showProgress(false)
                showResult(false)
                showError(true)
                
                val userMessage = ErrorLogger.getUserMessage(state.error)
                binding.tvErrorMessage.text = userMessage
            }

            is TranscriptionState.Cancelled -> {
                Toast.makeText(this, "转写已取消", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayResult(state: TranscriptionState.Success) {
        val result = state.result

        if (result.summary?.isNotEmpty() == true) {
            binding.tvSummary.text = result.summary
            binding.layoutSummary.visibility = View.VISIBLE
        } else {
            binding.layoutSummary.visibility = View.GONE
        }

        binding.tvFullText.text = result.fullText

        if (result.isCached) {
            binding.tvCacheIndicator.visibility = View.VISIBLE
        } else {
            binding.tvCacheIndicator.visibility = View.GONE
        }

        binding.btnClose.visibility = View.VISIBLE
    }

    private fun showCancelConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("取消转写")
            .setMessage("确定要取消当前转写吗？")
            .setPositiveButton("确定") { _, _ ->
                viewModel.cancelTranscription()
                finish()
            }
            .setNegativeButton("继续", null)
            .show()
    }

    private fun showProgress(show: Boolean) {
        binding.layoutProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showResult(show: Boolean) {
        binding.layoutResult.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnClose.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(show: Boolean) {
        binding.layoutError.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        val currentState = viewModel.transcriptionState.value
        if (currentState is TranscriptionState.Uploading || currentState is TranscriptionState.Processing) {
            showCancelConfirmationDialog()
        } else {
            super.onBackPressed()
        }
    }
}
