package com.practicum.playlistmaker.domain.impl

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
            override fun onSuccess(tracks: List<Track>) = onSuccess(tracks)
            override fun onError() = onError()
        })
    }
}