package com.example.recordingapp.audio

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * WAV文件写入器
 * 负责将PCM音频数据写入WAV格式文件
 */
class WavFileWriter(
    private val file: File,
    private val sampleRate: Int,
    private val channels: Int,
    private val bitsPerSample: Int
) {
    private val outputStream = FileOutputStream(file)
    private var dataSize = 0

    init {
        writeWavHeader()
    }

    /**
     * 写入WAV文件头
     */
    private fun writeWavHeader() {
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        
        // RIFF chunk
        header.put("RIFF".toByteArray())
        header.putInt(0) // 文件大小，稍后更新
        header.put("WAVE".toByteArray())
        
        // fmt chunk
        header.put("fmt ".toByteArray())
        header.putInt(16) // fmt chunk size
        header.putShort(1) // audio format (PCM)
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(sampleRate * channels * bitsPerSample / 8) // byte rate
        header.putShort((channels * bitsPerSample / 8).toShort()) // block align
        header.putShort(bitsPerSample.toShort())
        
        // data chunk
        header.put("data".toByteArray())
        header.putInt(0) // data size，稍后更新
        
        outputStream.write(header.array())
    }

    /**
     * 写入音频数据
     */
    fun write(data: ByteArray, size: Int) {
        outputStream.write(data, 0, size)
        dataSize += size
    }

    /**
     * 关闭文件并更新文件头
     */
    fun close() {
        outputStream.close()
        updateWavHeader()
    }

    /**
     * 更新WAV文件头中的文件大小信息
     */
    private fun updateWavHeader() {
        val randomAccessFile = RandomAccessFile(file, "rw")
        randomAccessFile.seek(4)
        randomAccessFile.writeInt(Integer.reverseBytes(dataSize + 36))
        randomAccessFile.seek(40)
        randomAccessFile.writeInt(Integer.reverseBytes(dataSize))
        randomAccessFile.close()
    }
}
