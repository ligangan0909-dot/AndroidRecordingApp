package com.example.recordingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recordingapp.databinding.ActivityMainBinding
import com.example.recordingapp.utils.PermissionHelper

/**
 * 主界面Activity
 * 提供开始录音和查看录音列表的入口
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnStartRecording.setOnClickListener {
            if (PermissionHelper.hasRecordAudioPermission(this)) {
                startRecordingActivity()
            } else {
                PermissionHelper.requestRecordAudioPermission(this)
            }
        }

        binding.btnViewRecordings.setOnClickListener {
            startActivity(Intent(this, RecordingListActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PermissionHelper.RECORD_AUDIO_PERMISSION_CODE) {
            if (PermissionHelper.hasRecordAudioPermission(this)) {
                startRecordingActivity()
            } else {
                Toast.makeText(
                    this,
                    R.string.permission_denied,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startRecordingActivity() {
        startActivity(Intent(this, RecordingActivity::class.java))
    }
}
