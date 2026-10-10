package com.practicum.playlistmaker

import android.os.Handler
import android.os.Looper

class ClickDebouncer(private val intervalMs: Long = 200L) {
    private var isClickAllowed = true
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            mainThreadHandler.postDelayed({isClickAllowed = true}, intervalMs)
        }
        return current
    }

    fun clear() {
        mainThreadHandler.removeCallbacksAndMessages(null)
        isClickAllowed = true
    }
}