package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.data.dto.TrackDto
import com.practicum.playlistmaker.data.mapper.toDomainOrNull
import com.practicum.playlistmaker.domain.api.SearchTracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.model.Track

class SearchTracksInteractorImpl(
    private val repository: TracksRepository
) : SearchTracksInteractor {

    override fun search(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onError: () -> Unit
    ) {
        if (query.isBlank()) {
            onError()
            return
        }
        repository.searchTracks(query, object : TracksRepository.SearchCallback {
            override fun onSuccess(tracks: List<TrackDto>) {
                onSuccess(tracks.mapNotNull { it.toDomainOrNull() })
            }
            override fun onError() = onError()
        })
    }
}