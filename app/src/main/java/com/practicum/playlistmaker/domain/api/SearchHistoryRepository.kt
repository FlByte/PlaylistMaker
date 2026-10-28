package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.model.Track

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}