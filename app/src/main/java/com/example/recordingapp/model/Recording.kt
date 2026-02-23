package com.example.recordingapp.model

import java.io.File

/**
 * 录音文件数据模型
 */
data class Recording(
    val file: File,
    val name: String,
    val duration: Long, // 时长（毫秒）
    val timestamp: Long // 创建时间戳
)
