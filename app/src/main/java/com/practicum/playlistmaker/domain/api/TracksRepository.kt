package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.data.dto.TrackDto

interface TracksRepository {
    fun searchTracks(query: String, callback: SearchCallback)

    interface SearchCallback {
        fun onSuccess(tracks: List<TrackDto>)
        fun onError()
    }
}