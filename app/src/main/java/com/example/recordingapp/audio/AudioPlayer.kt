package com.example.recordingapp.audio

import android.media.MediaPlayer
import java.io.File

/**
 * 音频播放器
 * 负责播放录音文件
 */
class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    
    var onPlaybackComplete: (() -> Unit)? = null
    var onPlaybackProgress: ((Int, Int) -> Unit)? = null

    /**
     * 播放音频文件
     */
    fun play(file: File) {
        if (isPlaying) {
            stop()
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            setOnCompletionListener {
                isPlaying = false
                onPlaybackComplete?.invoke()
            }
            start()
        }
        isPlaying = true
    }

    /**
     * 暂停播放
     */
    fun pause() {
        mediaPlayer?.pause()
        isPlaying = false
    }

    /**
     * 继续播放
     */
    fun resume() {
        mediaPlayer?.start()
        isPlaying = true
    }

    /**
     * 停止播放
     */
    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
    }

    /**
     * 获取当前播放位置（毫秒）
     */
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    /**
     * 获取音频总时长（毫秒）
     */
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    /**
     * 是否正在播放
     */
    fun isPlaying(): Boolean {
        return isPlaying
    }

    /**
     * 释放资源
     */
    fun release() {
        stop()
    }
}
