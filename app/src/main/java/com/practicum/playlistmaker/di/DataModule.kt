package com.practicum.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.media.MediaPlayer
import com.google.gson.Gson
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.data.player.PlayerController
import com.practicum.playlistmaker.data.preferences.SearchHistoryStorage
import com.practicum.playlistmaker.data.preferences.ThemeStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://itunes.apple.com"
private const val HISTORY_PREFS = "search_history"
private const val SETTINGS_PREFS = "settings_pref"

val HistoryPrefs = named("history_prefs")
val SettingsPrefs = named("settings_prefs")

val dataModule = module {
    single<ITunesApi> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)
    }

    factory { Gson() }

    single<SharedPreferences>(HistoryPrefs) {
        androidContext().getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)
    }
    single<SharedPreferences>(SettingsPrefs) {
        androidContext().getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
    }

    single { SearchHistoryStorage(get(HistoryPrefs), get()) }
    single { ThemeStorage(get(SettingsPrefs)) }

    single<MediaPlayer> { MediaPlayer() }
    factory { PlayerController(get()) }

    single<() -> Configuration> {
        { androidContext().resources.configuration }
    }

}