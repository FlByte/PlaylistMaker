package com.practicum.playlistmaker.data.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class PlayerController {

    interface Listener {
        fun onPrepared()
        fun onCompleted()
        fun onPlaybackTick(positionMs: Int)
        fun onError()
    }

    private val mediaPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper())

    private var listener: Listener? = null
    private var isPrepared = false
    private var isPlaying = false
    private var tickRunnable: Runnable? = null

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun prepare(url: String) {
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.setOnPreparedListener {
                isPrepared = true
                listener?.onPrepared()
            }
            mediaPlayer.setOnCompletionListener {
                isPlaying = false
                stopTicker()
                listener?.onCompleted()
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                isPlaying = false
                stopTicker()
                listener?.onError()
                true
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            isPrepared = false
            listener?.onError()
        }
    }

    fun play() {
        if (!isPrepared) return
        mediaPlayer.start()
        isPlaying = true
        startTicker()
    }

    fun pause() {
        if (!isPrepared) return
        mediaPlayer.pause()
        isPlaying = false
        stopTicker()
    }

    fun release() {
        stopTicker()
        listener = null
        mediaPlayer.release()
        isPrepared = false
        isPlaying = false
    }

    fun currentPosition(): Int = mediaPlayer.currentPosition

    private fun startTicker() {
        stopTicker()
        tickRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    listener?.onPlaybackTick(mediaPlayer.currentPosition)
                    handler.postDelayed(this, TICK_INTERVAL_MS)
                }
            }
        }
        handler.postDelayed(tickRunnable!!, TICK_INTERVAL_MS)
    }

    private fun stopTicker() {
        tickRunnable?.let { handler.removeCallbacks(it) }
        tickRunnable = null
    }

    companion object {
        private const val TICK_INTERVAL_MS = 500L
    }
}