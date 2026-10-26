package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.data.mapper.toDomainOrNull
import com.practicum.playlistmaker.data.mapper.toDto
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.domain.model.Track

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    override fun getHistory(): List<Track> =
        repository.getHistory().mapNotNull { it.toDomainOrNull() }

    override fun addTrack(track: Track) =
        repository.addTrack(track.toDto())

    override fun clearHistory() = repository.clearHistory()
}