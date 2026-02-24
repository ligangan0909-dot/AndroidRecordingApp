package com.example.recordingapp.audio

import android.media.MediaPlayer
import java.io.File

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlayingState = false
    
    var onPlaybackComplete: (() -> Unit)? = null
    var onPlaybackProgress: ((Int, Int) -> Unit)? = null

    fun play(file: File) {
        if (isPlayingState) {
            stop()
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            setOnCompletionListener {
                isPlayingState = false
                onPlaybackComplete?.invoke()
            }
            start()
        }
        isPlayingState = true
    }

    fun pause() {
        mediaPlayer?.pause()
        isPlayingState = false
    }

    fun resume() {
        mediaPlayer?.start()
        isPlayingState = true
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlayingState = false
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun isPlaying(): Boolean {
        return isPlayingState
    }

    fun release() {
        stop()
    }
}
