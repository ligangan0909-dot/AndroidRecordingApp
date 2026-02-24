package com.example.recordingapp.ui.files

import java.io.File

data class RecordingFile(
    val file: File,
    val name: String,
    val duration: String,
    val size: String,
    val date: String,
    val hasTranscription: Boolean = false,
    var isSelected: Boolean = false
) {
    val displayName: String
        get() = name.removeSuffix(".wav")
    
    val transcriptionFile: File
        get() = File(file.parent, "${file.nameWithoutExtension}.txt")
}
