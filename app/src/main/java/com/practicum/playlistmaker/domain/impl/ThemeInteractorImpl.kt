package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.ThemeInteractor
import com.practicum.playlistmaker.domain.api.ThemeRepository

class ThemeInteractorImpl(
    private val repository: ThemeRepository
) : ThemeInteractor {

    override fun getTheme(): Boolean = repository.isDarkTheme()

    override fun setTheme(enabled: Boolean) = repository.setDarkTheme(enabled)
}