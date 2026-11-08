package com.practicum.playlistmaker.data.player

import android.os.Handler
import android.os.Looper
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.player.api.PlayerInteractor

class PlayerInteractorImpl : PlayerInteractor {

    private val controller = PlayerController()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var callback: PlayerInteractor.Callback? = null
    private var isPrepared = false
    private var isPlaying = false

    init {
        controller.setListener(object : PlayerController.Listener {
            override fun onPrepared() {
                isPrepared = true
                notifyState()
            }
            override fun onCompleted() {
                isPlaying = false
                isPrepared = true
                notifyState(positionMs = 0)
            }
            override fun onPlaybackTick(positionMs: Int) = notifyState(positionMs)
            override fun onError() {
                isPrepared = false
                isPlaying = false
                notifyState()
            }
        })
    }

    override fun prepare(track: Track) {
        val url = track.previewUrl ?: run {
            isPrepared = false
            notifyState()
            return
        }
        controller.prepare(url)
    }

    override fun play() {
        if (!isPrepared) return
        isPlaying = true
        controller.play()
        notifyState()
    }

    override fun pause() {
        if (!isPrepared) return
        isPlaying = false
        controller.pause()
        notifyState()
    }

    override fun release() {
        callback = null
        controller.release()
    }

    override fun setCallback(callback: PlayerInteractor.Callback) {
        this.callback = callback
        notifyState()
    }

    private fun notifyState(positionMs: Int = controller.currentPosition()) {
        mainHandler.post {
            callback?.onStateChanged(
                isPlaying = isPlaying,
                isPlayEnabled = isPrepared,
                positionMs = positionMs
            )
        }
    }
}