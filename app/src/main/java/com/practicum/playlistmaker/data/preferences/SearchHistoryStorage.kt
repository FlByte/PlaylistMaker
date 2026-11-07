package com.practicum.playlistmaker.data.preferences

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.dto.TrackDto

class SearchHistoryStorage(private val prefs: SharedPreferences) {
    private val HISTORY_KEY = "history_tracks"
    private val MAX_HISTORY_SIZE = 10
    private val gson = Gson()

    fun save(tracks: List<TrackDto>) {
        prefs.edit().putString(HISTORY_KEY, gson.toJson(tracks)).apply()
    }

    fun get(): List<TrackDto> {
        val json = prefs.getString(HISTORY_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<TrackDto>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun add(track: TrackDto) {
        val history = get().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        val trimmed = if (history.size > MAX_HISTORY_SIZE)
            history.subList(0, MAX_HISTORY_SIZE).toList() else history
        save(trimmed)
    }

    fun clear() {
        prefs.edit().remove(HISTORY_KEY).apply()
    }
}