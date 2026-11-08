package com.practicum.playlistmaker.presentation.ui.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
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

private const val PLAYER_REFRESH_DELAY = 500L
private const val TRACK_ID_KEY = "track_id"

class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    private val historyInteractor: SearchHistoryInteractor
): ViewModel() {

    companion object {
        const val STATE_DEFAULT = 0
        const val STATE_PREPARED = 1
        const val STATE_PLAYING = 2
        const val STATE_PAUSED = 3

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                PlayerViewModel(savedStateHandle, Creator.provideSearchHistoryInteractor())
            }
        }
    }

    private val trackId: Long = savedStateHandle.get<Long>(TRACK_ID_KEY) ?: -1L
    private val track: Track? = if (trackId >= 0) historyInteractor.getTrackById(trackId) else null

    private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    private val mediaPlayer = MediaPlayer()
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private var playerState = STATE_DEFAULT
    private var timerRunnable: Runnable? = null

    private val playerStateLiveData = MutableLiveData<PlayerUiState>()
    fun observePlayerState(): LiveData<PlayerUiState> = playerStateLiveData

    private val isPlayEnabled = track?.previewUrl != null

    init {
        if (track?.previewUrl != null) preparePlayer(track.previewUrl)
        publishState()
    }

    override fun onCleared() {
        timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
        mediaPlayer.release()
        super.onCleared()
    }

    private fun publishState(playerPosition: Int = mediaPlayer.currentPosition) {
        playerStateLiveData.postValue(PlayerUiState.Content(
            track = track,
            formattedTime = timeFormat.format(track?.trackTimeMillis ?: 0L),
            isPlaying = playerState == STATE_PLAYING,
            isPlayEnabled = isPlayEnabled,
            positionText = timeFormat.format(playerPosition)
        ))
    }

    fun onPlayButtonClicked() {
        when(playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    private fun preparePlayer(url: String) {
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                playerState = STATE_PREPARED
                publishState()
            }
            mediaPlayer.setOnCompletionListener {
                playerState= STATE_PREPARED
                timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
                publishState(0)
            }
        } catch (e: Exception) {
            playerState = STATE_DEFAULT
            publishState()
            e.printStackTrace()
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState = STATE_PLAYING
        publishState()
        startTimerUpdate()
    }

    fun pausePlayer() {
        if (playerState == STATE_PLAYING) {
            mediaPlayer.pause()
            timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
            playerState = STATE_PAUSED
            publishState()
        }
    }

    private fun startTimerUpdate() {
        timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
        timerRunnable = object : Runnable {
            override fun run() {
                if (playerState == STATE_PLAYING) {
                    publishState(mediaPlayer.currentPosition)
                    mainThreadHandler.postDelayed(this, PLAYER_REFRESH_DELAY)
                }
            }
        }
    }
}