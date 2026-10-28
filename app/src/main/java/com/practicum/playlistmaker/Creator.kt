package com.practicum.playlistmaker

import android.content.Context
import com.practicum.playlistmaker.data.network.NetworkClient
import com.practicum.playlistmaker.data.preferences.SearchHistoryStorage
import com.practicum.playlistmaker.data.preferences.ThemeStorage
import com.practicum.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.ThemeRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.domain.api.SearchTracksInteractor
import com.practicum.playlistmaker.domain.api.ThemeInteractor
import com.practicum.playlistmaker.domain.api.ThemeRepository
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.domain.impl.SearchTracksInteractorImpl
import com.practicum.playlistmaker.domain.impl.ThemeInteractorImpl

object Creator {

    private const val HISTORY_PREFS = "search_history"
    private const val SETTINGS_PREFS = "settings_pref"

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val tracksRepository: TracksRepository by lazy {
        TracksRepositoryImpl(NetworkClient.iTunesApi)
    }

    private val searchHistoryRepository: SearchHistoryRepository by lazy {
        val prefs = appContext.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)
        SearchHistoryRepositoryImpl(SearchHistoryStorage(prefs))
    }

    private val themeRepository: ThemeRepository by lazy {
        val prefs = appContext.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        ThemeRepositoryImpl(
            ThemeStorage(prefs),
            configurationProvider = { appContext.resources.configuration }
        )
    }

    fun provideSearchTracksInteractor(): SearchTracksInteractor =
        SearchTracksInteractorImpl(tracksRepository)

    fun provideSearchHistoryInteractor(): SearchHistoryInteractor =
        SearchHistoryInteractorImpl(searchHistoryRepository)

    fun provideThemeInteractor(): ThemeInteractor =
        ThemeInteractorImpl(themeRepository)
}