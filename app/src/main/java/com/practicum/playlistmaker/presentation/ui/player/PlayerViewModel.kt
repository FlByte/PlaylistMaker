package com.practicum.playlistmaker.presentation.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.player.api.PlayerInteractor
import java.text.SimpleDateFormat
import java.util.Locale

sealed interface PlayerUiState {
    data class Content(
        val track: Track?,
        val formattedTime: String,
        val isPlaying: Boolean,
        val isPlayEnabled: Boolean,
        val positionText: String
    ) : PlayerUiState
}

private const val TRACK_ID_KEY = "track_id"

class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    private val historyInteractor: SearchHistoryInteractor,
    private val playerInteractor: PlayerInteractor
): ViewModel() {

    companion object {
        const val STATE_DEFAULT = 0
        const val STATE_PREPARED = 1
        const val STATE_PLAYING = 2
        const val STATE_PAUSED = 3

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                PlayerViewModel(
                    savedStateHandle,
                    Creator.provideSearchHistoryInteractor(),
                            Creator.providePlayerInteractor()
                )
            }
        }
    }

    private val trackId: Long = savedStateHandle.get<Long>(TRACK_ID_KEY) ?: -1L
    private val track: Track? = if (trackId >= 0) historyInteractor.getTrackById(trackId) else null

    private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    private val playerStateLiveData = MutableLiveData<PlayerUiState>()
    fun observePlayerState(): LiveData<PlayerUiState> = playerStateLiveData

    private var isPlaying = false
    private var isPlayEnabled = false
    private var positionMs = 0

    init {
        if (track != null) {
            playerStateLiveData.postValue(PlayerUiState.Content(
                track = track,
                formattedTime = timeFormat.format(track?.trackTimeMillis ?: 0L),
                isPlaying = isPlaying,
                isPlayEnabled = isPlayEnabled,
                positionText = timeFormat.format(positionMs)
            ))

            playerInteractor.setCallback(object : PlayerInteractor.Callback {
                override fun onStateChanged(
                    isPlaying: Boolean,
                    isPlayEnabled: Boolean,
                    positionMs: Int
                ) {
                    this@PlayerViewModel.isPlaying = isPlaying
                    this@PlayerViewModel.isPlayEnabled = isPlayEnabled
                    this@PlayerViewModel.positionMs = positionMs
                    publishState()
                }
            })
            playerInteractor.prepare(track)
        }
    }

    override fun onCleared() {
        playerInteractor.release()
        super.onCleared()
    }

    private fun publishState() {
        playerStateLiveData.postValue(PlayerUiState.Content(
            track = track,
            formattedTime = timeFormat.format(track?.trackTimeMillis ?: 0L),
            isPlaying = isPlaying,
            isPlayEnabled = isPlayEnabled,
            positionText = timeFormat.format(positionMs)
        ))
    }

    fun onPlayButtonClicked() {
        if (isPlaying) playerInteractor.pause() else playerInteractor.play()
    }

    fun pausePlayer() {
        if (isPlaying) playerInteractor.pause()
    }
}