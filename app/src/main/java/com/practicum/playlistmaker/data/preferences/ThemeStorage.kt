package com.practicum.playlistmaker.data.preferences

import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

class ThemeStorage(private val prefs: SharedPreferences) {
    private val THEME_KEY = "dark_theme"

    fun isDarkTheme(systemDark: Boolean): Boolean = prefs.getBoolean(THEME_KEY, systemDark)

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(THEME_KEY, enabled).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun isSystemDark(configuration: Configuration): Boolean {
        val mode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}