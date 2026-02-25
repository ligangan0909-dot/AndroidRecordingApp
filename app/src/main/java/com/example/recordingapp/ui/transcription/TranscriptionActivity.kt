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

        val app = application as RecordingApp
        val factory = TranscriptionViewModelFactory(app.transcriptionService)
        viewModel = ViewModelProvider(this, factory)[TranscriptionViewModel::class.java]

        setupUI()
        observeState()

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
                val detailedMessage = buildDetailedErrorMessage(state.error)
                
                binding.tvErrorMessage.text = detailedMessage
                
                // 显示详细错误对话框
                showDetailedErrorDialog(userMessage, detailedMessage)
            }

            is TranscriptionState.Cancelled -> {
                Toast.makeText(this, "转写已取消", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun buildDetailedErrorMessage(error: com.example.recordingapp.data.model.TranscriptionError): String {
        return when (error) {
            is com.example.recordingapp.data.model.TranscriptionError.ApiError -> {
                "API 错误\n" +
                "错误码: ${error.code}\n" +
                "详情: ${error.message}\n\n" +
                "可能的原因:\n" +
                when (error.code) {
                    400 -> "• 请求参数错误\n• 检查 App ID 和 Cluster ID 是否正确"
                    401 -> "• Access Key ID 或 Secret Key 错误\n• 密钥可能已过期"
                    403 -> "• 没有访问权限\n• 检查账号是否开通语音服务"
                    404 -> "• API 端点不存在\n• Cluster ID 可能错误"
                    429 -> "• 请求过于频繁\n• 请稍后重试"
                    500, 502, 503 -> "• 服务器错误\n• 请稍后重试"
                    else -> "• 未知错误\n• 请检查所有配置"
                }
            }
            is com.example.recordingapp.data.model.TranscriptionError.NetworkError -> {
                "网络错误\n详情: ${error.message}\n\n可能的原因:\n• 网络连接不稳定\n• 防火墙阻止了请求"
            }
            is com.example.recordingapp.data.model.TranscriptionError.AuthenticationError -> {
                "认证错误\n详情: ${error.message}\n\n请检查:\n• App ID\n• Access Key ID\n• Secret Key\n• Cluster ID"
            }
            is com.example.recordingapp.data.model.TranscriptionError.FileError -> {
                "文件错误\n详情: ${error.message}"
            }
            else -> {
                "错误: ${error.message}"
            }
        }
    }

    private fun showDetailedErrorDialog(userMessage: String, detailedMessage: String) {
        AlertDialog.Builder(this)
            .setTitle("转写失败")
            .setMessage(detailedMessage)
            .setPositiveButton("重试") { _, _ ->
                viewModel.retryTranscription()
            }
            .setNegativeButton("返回", null)
            .setNeutralButton("复制错误") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("错误信息", detailedMessage)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "错误信息已复制", Toast.LENGTH_SHORT).show()
            }
            .show()
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
