package com.example.recordingapp.ui.files

import java.io.File

data class RecordingFile(
    val file: File,
    val name: String,
    val duration: String,
    val size: String,
    val date: String,
    var isSelected: Boolean = false
) {
    val displayName: String
        get() = name.removeSuffix(".wav")
}
