package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.TrackSearchResponse
import com.practicum.playlistmaker.data.mapper.toDomainOrNull
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.domain.api.TracksRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl(private val api: ITunesApi) : TracksRepository {

    override fun searchTracks(query: String, callback: TracksRepository.SearchCallback) {
        api.search(query).enqueue(object : Callback<TrackSearchResponse> {
            override fun onResponse(
                call: Call<TrackSearchResponse>,
                response: Response<TrackSearchResponse>
            ) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    val tracks = body.results.mapNotNull { it.toDomainOrNull() }
                    callback.onSuccess(tracks)
                } else {
                    callback.onError()
                }
            }

            override fun onFailure(call: Call<TrackSearchResponse>, t: Throwable) {
                callback.onError()
            }
        })
    }
}