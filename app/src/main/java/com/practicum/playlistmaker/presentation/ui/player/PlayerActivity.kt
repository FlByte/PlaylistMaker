package com.practicum.playlistmaker.presentation.ui.player

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.Track
import java.text.SimpleDateFormat
import java.util.Locale

private const val PLAYER_REFRESH_DELAY = 500L
private const val TRACK_KEY = "track"

class PlayerActivity : AppCompatActivity() {

    private lateinit var playButton: ImageButton
    private lateinit var timer: TextView
    private val positionFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    private val mediaPlayer = MediaPlayer()
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var playerState = STATE_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.player_back_button).setOnClickListener { finish() }

        val track = IntentCompat.getParcelableExtra(intent, TRACK_KEY, Track::class.java)

        val artwork = findViewById<ImageView>(R.id.player_album_img)
        val trackName = findViewById<TextView>(R.id.player_track_name)
        val artistName = findViewById<TextView>(R.id.player_artist_name)
        playButton = findViewById(R.id.player_play_buton)
        playButton.isEnabled = false
        timer = findViewById(R.id.timer)
        val musicDuration = findViewById<TextView>(R.id.music_duration)
        val albumName = findViewById<TextView>(R.id.album_name)
        val musicYear = findViewById<TextView>(R.id.music_year)
        val musicGenre = findViewById<TextView>(R.id.music_genre)
        val musicCountry = findViewById<TextView>(R.id.music_country)
        val albumNameGroup = findViewById<Group>(R.id.album_name_group)
        val yearGroup = findViewById<Group>(R.id.year_group)
        val genreGroup = findViewById<Group>(R.id.genre_group)
        val countryGroup = findViewById<Group>(R.id.country_group)

        Glide.with(this)
            .load(track?.artworkUrl512)
            .placeholder(R.drawable.music_placeholder)
            .centerCrop()
            .transform(RoundedCorners(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2f,
                    resources.displayMetrics
                ).toInt()
            ))
            .into(artwork)

        trackName.text = track?.trackName
        artistName.text = track?.artistName
        musicDuration.text = track?.formattedTime ?: "00:00"
        timer.text = positionFormat.format(0L)

        track?.previewUrl?.let { preparePlayer(it) }
        playButton.setOnClickListener { playbackControl() }

        albumNameGroup.visibility = View.GONE
        track?.collectionName?.let {
            albumName.text = it
            albumNameGroup.visibility = View.VISIBLE
        }
        yearGroup.visibility = View.GONE
        track?.releaseYear?.let {
            musicYear.text = it
            yearGroup.visibility = View.VISIBLE
        }
        genreGroup.visibility = View.GONE
        track?.primaryGenreName?.let {
            musicGenre.text = it
            genreGroup.visibility = View.VISIBLE
        }
        countryGroup.visibility = View.GONE
        track?.country?.let {
            musicCountry.text = it
            countryGroup.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) pausePlayer()
    }

    override fun onDestroy() {
        timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
        mediaPlayer.release()
        super.onDestroy()
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }

    private fun preparePlayer(url: String) {
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                playButton.isEnabled = true
                playerState = STATE_PREPARED
            }
            mediaPlayer.setOnCompletionListener {
                playButton.setBackgroundResource(R.drawable.play)
                playerState = STATE_PREPARED
                timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
                timer.text = positionFormat.format(0L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playButton.isEnabled = false
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playButton.setBackgroundResource(R.drawable.stop)
        playerState = STATE_PLAYING
        startTimer()
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playButton.setBackgroundResource(R.drawable.play)
        playerState = STATE_PAUSED
        timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    private fun startTimer() {
        timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
        timer.text = positionFormat.format(mediaPlayer.currentPosition)
        timerRunnable = object : Runnable {
            override fun run() {
                timer.text = positionFormat.format(mediaPlayer.currentPosition)
                if (playerState == STATE_PLAYING && mediaPlayer.currentPosition > 0) {
                    mainThreadHandler.postDelayed(this, PLAYER_REFRESH_DELAY)
                }
            }
        }
        mainThreadHandler.postDelayed(timerRunnable!!, PLAYER_REFRESH_DELAY)
    }
}