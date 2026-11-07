package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.model.Track

interface TracksRepository {
    fun searchTracks(query: String, callback: SearchCallback)

    interface SearchCallback {
        fun onSuccess(tracks: List<Track>)
        fun onError()
    }
}