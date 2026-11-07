package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.model.Track

interface SearchTracksInteractor {
    fun search(query: String, onSuccess: (List<Track>) -> Unit, onError: () -> Unit)
}