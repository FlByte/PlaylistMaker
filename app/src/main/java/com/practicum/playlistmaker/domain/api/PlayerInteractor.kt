package com.practicum.playlistmaker.domain.player.api

import com.practicum.playlistmaker.domain.model.Track

interface PlayerInteractor {

    fun prepare(track: Track)

    fun play()
    fun pause()
    fun release()

    fun setCallback(callback: Callback)

    interface Callback {
        fun onStateChanged(isPlaying: Boolean, isPlayEnabled: Boolean, positionMs: Int)
    }
}