package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.data.dto.TrackDto

interface SearchHistoryRepository {
    fun getHistory(): List<TrackDto>
    fun addTrack(track: TrackDto)
    fun clearHistory()
}