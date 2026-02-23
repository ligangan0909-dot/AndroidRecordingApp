package com.example.recordingapp.ui.transcription

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.recordingapp.R
import com.example.recordingapp.RecordingApp
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.databinding.ActivityTranscriptionBinding
import kotlinx.coroutines.launch

/**
 * Activity for displaying transcription progress and results
 * Simplified version: Only shows transcription text and summary
 */
class TranscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTranscriptionBinding
    private lateinit var viewModel: TranscriptionViewModel

    companion object {
        const val EXTRA_RECORDING_ID = "recording_id"
        const val EXTRA_AUDIO_FILE_PATH = "audio_file_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get recording info from intent
        val recordingId = intent.getStringExtra(EXTRA_RECORDING_ID)
        val audioFilePath = intent.getStringExtra(EXTRA_AUDIO_FILE_PATH)

        if (recordingId == null || audioFilePath == null) {
            Toast.makeText(this, "Invalid recording data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize ViewModel with dependency injection
        val app = application as RecordingApp
        val factory = TranscriptionViewModelFactory(app.transcriptionService)
        viewModel = ViewModelProvider(this, factory)[TranscriptionViewModel::class.java]

        setupUI()
        observeState()

        // Start transcription
        viewModel.startTranscription(recordingId, audioFilePath)
    }

    private fun setupUI() {
        binding.btnCancel.setOnClickListener {
            viewModel.cancelTranscription()
            finish()
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
                binding.tvProgressStatus.text = getString(R.string.uploading, state.progress)
                binding.progressBar.isIndeterminate = false
                binding.progressBar.progress = state.progress
                showResult(false)
                showError(false)
            }

            is TranscriptionState.Processing -> {
                showProgress(true)
                binding.tvProgressStatus.text = getString(R.string.processing)
                binding.progressBar.isIndeterminate = true
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
                binding.tvErrorMessage.text = state.error.toUserMessage()
            }

            is TranscriptionState.Cancelled -> {
                Toast.makeText(this, R.string.transcription_cancelled, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayResult(state: TranscriptionState.Success) {
        val result = state.result

        // Display summary
        if (result.summary.isNotEmpty()) {
            binding.tvSummary.text = result.summary
            binding.layoutSummary.visibility = View.VISIBLE
        } else {
            binding.layoutSummary.visibility = View.GONE
        }

        // Display full text
        binding.tvFullText.text = result.fullText

        // Show cache indicator if applicable
        if (result.isCached) {
            binding.tvCacheIndicator.visibility = View.VISIBLE
        } else {
            binding.tvCacheIndicator.visibility = View.GONE
        }
    }

    private fun showProgress(show: Boolean) {
        binding.layoutProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showResult(show: Boolean) {
        binding.layoutResult.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(show: Boolean) {
        binding.layoutError.visibility = if (show) View.VISIBLE else View.GONE
    }
}
