package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.presentation.ui.player.PlayerViewModel
import com.practicum.playlistmaker.presentation.ui.search.SearchViewModel
import com.practicum.playlistmaker.presentation.ui.settings.SettingsViewModel
import com.practicum.playlistmaker.presentation.util.ClickDebouncer
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SearchViewModel(get(), get()) }
    viewModel { PlayerViewModel(
        get(), get(), get())}
    viewModel { SettingsViewModel(get()) }
    factory { ClickDebouncer() }
}