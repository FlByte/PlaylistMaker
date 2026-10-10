package com.practicum.playlistmaker

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
import java.text.SimpleDateFormat
import java.util.Locale

const val PLAYER_REFRESH_DELAY = 500L

class PlayerActivity : AppCompatActivity() {

    private lateinit var playButton: ImageButton
    private lateinit var addToPlaylistButton: ImageButton
    private lateinit var addToFavoriteButton: ImageButton
    private lateinit var timer: TextView

    private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    private val mediaPlayer = MediaPlayer()
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<Button>(R.id.player_back_button)
        backButton.setOnClickListener {
            finish()
        }

        val track = IntentCompat.getParcelableExtra(intent, "track", Track::class.java)

        val artwork = findViewById<ImageView>(R.id.player_album_img)
        val trackName = findViewById<TextView>(R.id.player_track_name)
        val artistName = findViewById<TextView>(R.id.player_artist_name)
        playButton = findViewById(R.id.player_play_buton)
        playButton.isEnabled = false
        addToPlaylistButton = findViewById(R.id.add_to_playlist_buton)
        addToFavoriteButton = findViewById(R.id.add_to_favorite_buton)
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
            .load(track?.artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg"))
            .placeholder(R.drawable.music_placeholder)
            .centerCrop()
            .transform(RoundedCorners(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2f,
                    resources.displayMetrics).toInt()
            ))
            .into(artwork)

        trackName.text = track?.trackName
        artistName.text = track?.artistName
        musicDuration.text = timeFormat.format(track?.trackTimeMillis)
        timer.text = timeFormat.format(0L)

        if (track?.previewUrl != null) {
            preparePlayer(track.previewUrl)
        }

        playButton.setOnClickListener { playbackControl() }

        if (track?.collectionName != null) {
            albumName.text = track.collectionName
            albumNameGroup.visibility = View.VISIBLE
        } else {
            albumNameGroup.visibility = View.GONE
        }

        if (track?.releaseDate != null) {
            musicYear.text = track.releaseDate.take(4)
            yearGroup.visibility = View.VISIBLE
        } else {
            yearGroup.visibility = View.GONE
        }

        if (track?.primaryGenreName != null) {
            musicGenre.text = track.primaryGenreName
            genreGroup.visibility = View.VISIBLE
        } else {
            genreGroup.visibility = View.GONE
        }

        if (track?.country != null) {
            musicCountry.text = track.country
            countryGroup.visibility = View.VISIBLE
        } else {
            countryGroup.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
        mediaPlayer.release()
        super.onDestroy()
    }
    companion object  {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }

    private var playerState = STATE_DEFAULT

    private fun preparePlayer(url: String) {
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                playButton.isEnabled = true
                playerState = STATE_PREPARED
                playButton.isEnabled = true
            }
            mediaPlayer.setOnCompletionListener {
                playButton.setBackgroundResource(R.drawable.play)
                playerState = STATE_PREPARED
                timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
                timer.text = timeFormat.format(0L)
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
        when(playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    private fun startTimer() {
        timerRunnable?.let { mainThreadHandler.removeCallbacks(it) }
        timer.text = timeFormat.format(mediaPlayer.currentPosition)

        timerRunnable = object : Runnable {
            override fun run() {
                timer.text = timeFormat.format(mediaPlayer.currentPosition)

                if ((playerState == STATE_PLAYING) && (mediaPlayer.currentPosition > 0)) {
                    mainThreadHandler.postDelayed(this, PLAYER_REFRESH_DELAY)
                }
            }
        }
        mainThreadHandler.postDelayed(timerRunnable!!, PLAYER_REFRESH_DELAY)
    }
}