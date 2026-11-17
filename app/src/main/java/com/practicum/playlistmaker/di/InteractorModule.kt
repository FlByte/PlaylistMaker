package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.player.PlayerInteractorImpl
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.SearchTracksInteractor
import com.practicum.playlistmaker.domain.api.ThemeInteractor
import com.practicum.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.domain.impl.SearchTracksInteractorImpl
import com.practicum.playlistmaker.domain.impl.ThemeInteractorImpl
import com.practicum.playlistmaker.domain.api.PlayerInteractor
import org.koin.dsl.module

val interactorModule = module {
    single<SearchTracksInteractor> { SearchTracksInteractorImpl(get()) }
    single<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }
    single<ThemeInteractor> { ThemeInteractorImpl(get()) }
    factory<PlayerInteractor> { PlayerInteractorImpl(get()) }
}