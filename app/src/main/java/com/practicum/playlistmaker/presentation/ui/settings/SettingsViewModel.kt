package com.practicum.playlistmaker.presentation.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.domain.api.ThemeInteractor

class SettingsViewModel(private val themeInteractor: ThemeInteractor): ViewModel() {

    private val themeLiveData = MutableLiveData(themeInteractor.getTheme())
    fun observeTheme(): LiveData<Boolean> = themeLiveData

    fun setTheme(enabled: Boolean) {
        if (themeLiveData.value == enabled) return
        themeInteractor.setTheme(enabled)
        themeLiveData.postValue(enabled)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(Creator.provideThemeInteractor())
            }
        }
    }
}