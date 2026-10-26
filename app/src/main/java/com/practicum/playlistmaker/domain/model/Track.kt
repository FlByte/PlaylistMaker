package com.practicum.playlistmaker.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val formattedTime: String,
    val artworkUrl100: String? = null,
    val artworkUrl512: String? = null,
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val releaseYear: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null,
    val previewUrl: String? = null
) : Parcelable