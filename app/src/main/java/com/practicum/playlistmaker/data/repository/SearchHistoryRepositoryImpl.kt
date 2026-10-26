package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.TrackDto
import com.practicum.playlistmaker.data.preferences.SearchHistoryStorage
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val storage: SearchHistoryStorage
) : SearchHistoryRepository {

    override fun getHistory(): List<TrackDto> = storage.get()

    override fun addTrack(track: TrackDto) = storage.add(track)

    override fun clearHistory() = storage.clear()
}