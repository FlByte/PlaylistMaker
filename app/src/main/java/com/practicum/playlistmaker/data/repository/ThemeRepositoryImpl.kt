package com.practicum.playlistmaker.data.repository

import android.content.res.Configuration
import com.practicum.playlistmaker.data.preferences.ThemeStorage
import com.practicum.playlistmaker.domain.api.ThemeRepository

class ThemeRepositoryImpl(
    private val storage: ThemeStorage,
    private val configurationProvider: () -> Configuration
) : ThemeRepository {

    override fun isDarkTheme(): Boolean =
        storage.isDarkTheme(storage.isSystemDark(configurationProvider()))

    override fun setDarkTheme(enabled: Boolean) = storage.setDarkTheme(enabled)
}