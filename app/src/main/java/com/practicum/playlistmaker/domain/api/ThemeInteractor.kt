package com.practicum.playlistmaker.domain.api

interface ThemeInteractor {
    fun getTheme(): Boolean
    fun setTheme(enabled: Boolean)
}