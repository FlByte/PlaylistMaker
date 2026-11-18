package com.practicum.playlistmaker.presentation.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.api.ThemeInteractor

class SettingsViewModel(private val themeInteractor: ThemeInteractor): ViewModel() {

    private val themeLiveData = MutableLiveData(themeInteractor.getTheme())
    fun observeTheme(): LiveData<Boolean> = themeLiveData

    fun setTheme(enabled: Boolean) {
        if (themeLiveData.value == enabled) return
        themeInteractor.setTheme(enabled)
        themeLiveData.postValue(enabled)
    }
}