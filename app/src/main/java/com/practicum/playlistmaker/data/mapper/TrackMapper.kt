package com.practicum.playlistmaker.data.mapper

import com.practicum.playlistmaker.data.dto.TrackDto
import com.practicum.playlistmaker.domain.model.Track
import java.text.SimpleDateFormat
import java.util.Locale

private const val ARTWORK_512_SUFFIX = "512x512bb.jpg"

private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

fun TrackDto.toDomainOrNull(): Track? {
    val name = trackName ?: return null
    val artist = artistName ?: return null
    val durationMs = trackTimeMillis ?: 0L
    return Track(
        trackId = trackId ?: return null,
        trackName = name,
        artistName = artist,
        trackTimeMillis = durationMs,
        formattedTime = timeFormat.format(durationMs),
        artworkUrl100 = artworkUrl100,
        artworkUrl512 = artworkUrl100?.replaceAfterLast('/', ARTWORK_512_SUFFIX),
        collectionName = collectionName,
        releaseDate = releaseDate,
        releaseYear = releaseDate?.take(4)?.takeIf { it.length == 4 },
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )
}

fun Track.toDto(): TrackDto = TrackDto(
    trackId = trackId,
    trackName = trackName,
    artistName = artistName,
    trackTimeMillis = trackTimeMillis,
    artworkUrl100 = artworkUrl100,
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    previewUrl = previewUrl
)