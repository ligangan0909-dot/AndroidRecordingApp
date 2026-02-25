package com.example.recordingapp.ui.playback

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recordingapp.R
import com.example.recordingapp.RecordingApp
import com.example.recordingapp.audio.AudioPlayer
import com.example.recordingapp.data.model.TranscriptionError
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.ui.views.WaveformView
import kotlinx.coroutines.launch
import java.io.File

class PlaybackFragment : Fragment() {
    private lateinit var toolbar: Toolbar
    private lateinit var fileName: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var currentTime: TextView
    private lateinit var totalTime: TextView
    private lateinit var transcribeButton: Button
    private lateinit var toggleViewButton: Button
    private lateinit var waveformView: WaveformView
    private lateinit var transcriptionScroll: ScrollView
    private lateinit var transcriptionText: TextView
    private lateinit var transcriptionProgress: ProgressBar
    private lateinit var transcriptionStatus: TextView
    
    private lateinit var audioPlayer: AudioPlayer
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var showingTranscription = false
    
    private val viewModel: PlaybackViewModel by lazy {
        val app = requireActivity().application as RecordingApp
        ViewModelProvider(this, PlaybackViewModelFactory(app.transcriptionService))[PlaybackViewModel::class.java]
    }
    
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                updateSeekBar()
                handler.postDelayed(this, 100)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        audioPlayer = AudioPlayer()
        setupToolbar()
        setupPlaybackControls()
        setupActionButtons()
        loadRecording()
        observeTranscriptionState()
        loadExistingTranscription()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        fileName = view.findViewById(R.id.file_name)
        playPauseButton = view.findViewById(R.id.play_pause_button)
        seekBar = view.findViewById(R.id.seek_bar)
        currentTime = view.findViewById(R.id.current_time)
        totalTime = view.findViewById(R.id.total_time)
        transcribeButton = view.findViewById(R.id.transcribe_button)
        toggleViewButton = view.findViewById(R.id.toggle_view_button)
        waveformView = view.findViewById(R.id.waveform_view)
        transcriptionScroll = view.findViewById(R.id.transcription_scroll)
        transcriptionText = view.findViewById(R.id.transcription_text)
        transcriptionProgress = view.findViewById(R.id.transcription_progress)
        transcriptionStatus = view.findViewById(R.id.transcription_status)
    }
    
    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupPlaybackControls() {
        playPauseButton.setOnClickListener {
            togglePlayPause()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioPlayer.mediaPlayer?.seekTo(progress)
                    updateTimeDisplay()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        audioPlayer.onPlaybackComplete = {
            isPlaying = false
            playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            handler.removeCallbacks(updateSeekBarRunnable)
        }
    }
    
    private fun setupActionButtons() {
        transcribeButton.setOnClickListener {
            val recordingId = arguments?.getLong("recordingId", 0L).toString()
            val filePath = arguments?.getString("filePath") ?: return@setOnClickListener
            val file = File(filePath)
            
            if (!file.exists()) {
                Toast.makeText(requireContext(), "文件不存在", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.startTranscription(recordingId, filePath, file)
        }
        
        toggleViewButton.setOnClickListener {
            toggleView()
        }
    }
    
    private fun loadRecording() {
        val filePath = arguments?.getString("filePath") ?: return
        val file = File(filePath)
        fileName.text = file.nameWithoutExtension
    }
    
    private fun togglePlayPause() {
        if (isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }
    
    private fun startPlayback() {
        val filePath = arguments?.getString("filePath") ?: return
        val file = File(filePath)
        
        if (!file.exists()) {
            Toast.makeText(requireContext(), "文件不存在", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (audioPlayer.mediaPlayer == null) {
            audioPlayer.play(file)
            seekBar.max = audioPlayer.getDuration()
            updateTimeDisplay()
        } else {
            audioPlayer.resume()
        }
        
        isPlaying = true
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        handler.post(updateSeekBarRunnable)
    }
    
    private fun pausePlayback() {
        audioPlayer.pause()
        isPlaying = false
        playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        handler.removeCallbacks(updateSeekBarRunnable)
    }
    
    private fun updateSeekBar() {
        val currentPosition = audioPlayer.getCurrentPosition()
        seekBar.progress = currentPosition
        updateTimeDisplay()
    }
    
    private fun updateTimeDisplay() {
        val current = audioPlayer.getCurrentPosition()
        val total = audioPlayer.getDuration()
        
        currentTime.text = formatTime(current)
        totalTime.text = formatTime(total)
    }
    
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    
    private fun toggleView() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasTranscription.collect { hasTranscription ->
                if (!hasTranscription) {
                    return@collect
                }
                
                showingTranscription = !showingTranscription
                
                if (showingTranscription) {
                    waveformView.visibility = View.GONE
                    transcriptionScroll.visibility = View.VISIBLE
                    toggleViewButton.text = "查看波形"
                } else {
                    waveformView.visibility = View.VISIBLE
                    transcriptionScroll.visibility = View.GONE
                    toggleViewButton.text = "查看转写"
                }
            }
        }
    }
    
    private fun observeTranscriptionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transcriptionState.collect { state ->
                when (state) {
                    is TranscriptionState.Idle -> {
                        transcriptionProgress.visibility = View.GONE
                        transcriptionStatus.visibility = View.GONE
                        toggleViewButton.visibility = View.GONE
                    }
                    
                    is TranscriptionState.Uploading -> {
                        transcriptionProgress.visibility = View.VISIBLE
                        transcriptionStatus.visibility = View.VISIBLE
                        transcriptionStatus.text = "正在上传..."
                        transcriptionScroll.visibility = View.VISIBLE
                        waveformView.visibility = View.GONE
                    }
                    
                    is TranscriptionState.Processing -> {
                        transcriptionProgress.visibility = View.VISIBLE
                        transcriptionStatus.visibility = View.VISIBLE
                        transcriptionStatus.text = "正在转写..."
                    }
                    
                    is TranscriptionState.Success -> {
                        transcriptionProgress.visibility = View.GONE
                        transcriptionStatus.visibility = View.GONE
                        transcriptionText.text = state.result.fullText
                        toggleViewButton.visibility = View.VISIBLE
                        
                        transcriptionScroll.visibility = View.VISIBLE
                        waveformView.visibility = View.GONE
                        showingTranscription = true
                        toggleViewButton.text = "查看波形"
                    }
                    
                    is TranscriptionState.Error -> {
                        transcriptionProgress.visibility = View.GONE
                        transcriptionStatus.visibility = View.GONE
                        
                        showDetailedErrorDialog(state.error)
                        
                        waveformView.visibility = View.VISIBLE
                        transcriptionScroll.visibility = View.GONE
                        showingTranscription = false
                    }
                    
                    is TranscriptionState.Cancelled -> {
                        transcriptionProgress.visibility = View.GONE
                        transcriptionStatus.visibility = View.GONE
                        Toast.makeText(requireContext(), "转写已取消", Toast.LENGTH_SHORT).show()
                        
                        waveformView.visibility = View.VISIBLE
                        transcriptionScroll.visibility = View.GONE
                        showingTranscription = false
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasTranscription.collect { hasTranscription ->
                if (hasTranscription) {
                    toggleViewButton.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun showDetailedErrorDialog(error: TranscriptionError) {
        val errorDetails = buildErrorDetails(error)
        val diagnostics = buildDiagnostics(error)
        
        val message = "${errorDetails}\n\n诊断建议：\n${diagnostics}"
        
        AlertDialog.Builder(requireContext())
            .setTitle("转写失败")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .setNeutralButton("复制错误") { _, _ ->
                copyErrorToClipboard(error, errorDetails, diagnostics)
            }
            .show()
    }
    
    private fun buildErrorDetails(error: TranscriptionError): String {
        return when (error) {
            is TranscriptionError.NetworkError -> "错误类型：网络错误\n错误消息：${error.message}\n详细信息：${error.errorCause?.message ?: "无"}"
            is TranscriptionError.ApiError -> "错误类型：API错误\n错误代码：${error.code}\n错误消息：${error.message}"
            is TranscriptionError.FileError -> "错误类型：文件错误\n错误消息：${error.message}"
            is TranscriptionError.AuthError, is TranscriptionError.AuthenticationError -> "错误类型：认证错误\n错误消息：${error.message}"
            is TranscriptionError.TimeoutError -> "错误类型：超时错误\n错误消息：${error.message}"
            is TranscriptionError.ValidationError -> "错误类型：验证错误\n错误消息：${error.message}"
            is TranscriptionError.UnknownError -> "错误类型：未知错误\n错误消息：${error.message}\n详细信息：${error.errorCause?.message ?: "无"}"
        }
    }
    
    private fun buildDiagnostics(error: TranscriptionError): String {
        return when (error) {
            is TranscriptionError.NetworkError -> "• 检查网络连接是否正常\n• 确认设备可以访问互联网\n• 尝试切换网络（WiFi/移动数据）"
            is TranscriptionError.ApiError -> {
                when (error.code) {
                    401, 403 -> "• 检查豆包语音的 4 个参数是否正确\n• App ID、Access Key ID、Secret Key、Cluster ID\n• 确认 API 密钥是否有效且未过期"
                    400 -> "• 音频文件格式可能不支持\n• 检查音频文件是否损坏\n• 确认音频文件大小是否超过限制"
                    429 -> "• API 调用频率超过限制\n• 请稍后再试\n• 检查账户配额是否用完"
                    500, 502, 503 -> "• 服务器暂时不可用\n• 请稍后再试"
                    else -> "• 检查豆包语音 API 设置是否正确\n• 确认所有必填参数都已填写\n• 查看错误消息中的详细信息"
                }
            }
            is TranscriptionError.FileError -> "• 检查音频文件是否存在\n• 确认应用有读取文件的权限\n• 尝试重新录制音频"
            is TranscriptionError.AuthError, is TranscriptionError.AuthenticationError -> "• 进入设置页面检查豆包语音配置\n• 确认 4 个参数都已正确填写\n• 点击验证凭证按钮测试配置"
            is TranscriptionError.TimeoutError -> "• 检查网络连接速度\n• 音频文件可能太大\n• 稍后再试"
            is TranscriptionError.ValidationError -> "• 检查豆包语音配置是否完整\n• 确认所有必填字段都已填写"
            is TranscriptionError.UnknownError -> "• 请将错误信息复制并发送给开发者\n• 尝试重启应用\n• 检查应用是否需要更新"
        }
    }
    
    private fun copyErrorToClipboard(error: TranscriptionError, details: String, diagnostics: String) {
        val fullErrorInfo = "===== 转写错误详情 =====\n\n${details}\n\n诊断建议：\n${diagnostics}\n\n===== 技术信息 =====\n错误类名：${error::class.java.simpleName}\n时间戳：${System.currentTimeMillis()}"
        
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("转写错误", fullErrorInfo)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(requireContext(), "错误信息已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }
    
    private fun loadExistingTranscription() {
        val recordingId = arguments?.getLong("recordingId", 0L).toString()
        val filePath = arguments?.getString("filePath") ?: return
        val file = File(filePath)
        
        if (file.exists()) {
            viewModel.loadExistingTranscription(recordingId, file)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSeekBarRunnable)
        audioPlayer.release()
    }
}
