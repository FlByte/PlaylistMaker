package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.mapper.toDomainOrNull
import com.practicum.playlistmaker.data.mapper.toDto
import com.practicum.playlistmaker.data.preferences.SearchHistoryStorage
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.domain.model.Track

class SearchHistoryRepositoryImpl(
    private val storage: SearchHistoryStorage
) : SearchHistoryRepository {

    override fun getHistory(): List<Track> =
        storage.get().mapNotNull { it.toDomainOrNull() }

    override fun addTrack(track: Track) = storage.add(track.toDto())

    override fun clearHistory() = storage.clear()
}