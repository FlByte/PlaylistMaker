package com.practicum.playlistmaker.presentation.ui.search

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.SearchTracksInteractor
import com.practicum.playlistmaker.domain.model.Track

sealed interface SearchUiState {
    data class History(val tracks: List<Track>): SearchUiState
    data class Tracks(val tracks: List<Track>): SearchUiState
    data object Loading: SearchUiState
    data object Empty: SearchUiState
    data object Error: SearchUiState
    data object MaxQueryLenght: SearchUiState
}

private const val SEARCH_DEBOUNCE_DELAY = 2000L
private const val MAX_QUERY_LENGTH = 200
private val mainThreadHandler = Handler(Looper.getMainLooper())

class SearchViewModel(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val historyInteractor: SearchHistoryInteractor
): ViewModel() {
    private val searchRunnable = Runnable { findTracks() }
    private val state = MutableLiveData<SearchUiState>()
    fun observeState(): LiveData<SearchUiState> = state

    private val query = MutableLiveData<String>()
    fun observeQuery(): LiveData<String> = query

    fun onQueryChanged(text: String) {
        query.postValue(text)
        cancelSearch()

        if (text.isEmpty()) {
            showHistory()
            return
        }
        if (text.length > MAX_QUERY_LENGTH) {
            state.postValue(SearchUiState.Empty)
            return
        }
        mainThreadHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    fun onReloadClicked() {
        if (query.value.orEmpty().isNotBlank()) findTracks()
    }

    fun onClearClicked() {
        cancelSearch()
        query.postValue("")
        showHistory()
    }

    fun onClearHistoryClicked() {
        historyInteractor.clearHistory()
        showHistory()
    }

    fun onTrackClicked(track: Track): Long {
        historyInteractor.addTrack(track)
        return track.trackId
    }

    fun onFocusChanged(hasFocus: Boolean) {
        if (hasFocus && query.value.isNullOrEmpty()) showHistory()
    }

    private fun showHistory() {
        val history = historyInteractor.getHistory()
        if (history.isEmpty()) {
            state.postValue(SearchUiState.Tracks(emptyList()))
        } else {
            state.postValue(SearchUiState.History(history))
        }
    }

    private fun findTracks() {
        if (query.value.isNullOrEmpty()) return
        state.postValue(SearchUiState.Loading)
        searchTracksInteractor.search(
            query.value!!,
            onSuccess = { tracks ->
                if (tracks.isEmpty()) {
                    state.postValue(SearchUiState.Empty)
                } else {
                    state.value = SearchUiState.Tracks(tracks)
                }
            },
            onError = {
                state.postValue(SearchUiState.Error)
            }
        )
    }

    private fun cancelSearch() {
        mainThreadHandler.removeCallbacks(searchRunnable)
    }

    override fun onCleared() {
        cancelSearch()
        super.onCleared()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SearchViewModel(
                    Creator.provideSearchTracksInteractor(),
                    Creator.provideSearchHistoryInteractor()
                )
            }
        }
    }
}