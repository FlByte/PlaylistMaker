package com.practicum.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson

class SearchHistory (private val historyPrefs: SharedPreferences) {
    private val HISTORY_KEY = "history_tracks"
    private val MAX_HISTORY_SIZE = 10

    fun saveHistory(tracks: List<Track>) {
        val json = Gson().toJson(tracks)
        historyPrefs.edit().putString(HISTORY_KEY, json).apply()
    }

    fun getHistory(): Array<Track> {
        val json = historyPrefs.getString(HISTORY_KEY, null)

        if (json != null) {
            return Gson().fromJson(json, Array<Track>::class.java)
        } else {
            return emptyArray()
        }
    }

    fun addTrackToHistory(track: Track) {
        var history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE).toMutableList()
        }

        saveHistory(history)
    }

    fun clearHistory() {
        historyPrefs.edit().remove(HISTORY_KEY).apply()
    }
}