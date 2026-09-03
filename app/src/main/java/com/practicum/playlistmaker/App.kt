package com.practicum.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

const val SETTINGS_PREFERENCES = "settings_pref"
const val THEME = "dark_theme"

class App : Application() {
    lateinit var sharedPref: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        sharedPref = getSharedPreferences(SETTINGS_PREFERENCES, MODE_PRIVATE)

        val currentTheme = sharedPref.getBoolean(THEME, getCurrntTheme())
        switchTheme(currentTheme)

    }

    var darkTheme = false

    fun switchTheme(darkThemeEnabled: Boolean){
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        sharedPref.edit()
            .putBoolean(THEME, darkThemeEnabled)
            .apply()
    }

    fun getCurrntTheme() : Boolean {
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }
}