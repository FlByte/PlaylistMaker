package com.practicum.playlistmaker

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Creator.init(this)
        Creator.provideThemeInteractor().setTheme(
            Creator.provideThemeInteractor().getTheme()
        )
    }
}