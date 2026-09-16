package com.practicum.playlistmaker

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val track = intent.getSerializableExtra("track") as Track

        val artwork = findViewById<ImageView>(R.id.albumImg)
        val trackName = findViewById<TextView>(R.id.trackName)
        val artistName = findViewById<TextView>(R.id.artistName)
        val timer = findViewById<TextView>(R.id.timer)
        val musicDuration = findViewById<TextView>(R.id.musicDuration)
        val albumName = findViewById<TextView>(R.id.albumName)
        val musicYear = findViewById<TextView>(R.id.musicYear)
        val musicGenre = findViewById<TextView>(R.id.musicGenre)
        val musicCountry = findViewById<TextView>(R.id.musicCountry)
        val albumNameGroup = findViewById<Group>(R.id.albumNameGroup)
        val yearGroup = findViewById<Group>(R.id.yearGroup)
        val genreGroup = findViewById<Group>(R.id.genreGroup)
        val countryGroup = findViewById<Group>(R.id.countryGroup)

        Glide.with(this)
            .load(track.artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg"))
            .placeholder(R.drawable.music_placeholder)
            .centerCrop()
            .transform(RoundedCorners(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2f,
                    resources.displayMetrics).toInt()
            ))
            .into(artwork)

        trackName.text = track.trackName
        artistName.text = track.artistName
        musicDuration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        timer.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        if (track.collectionName != null) {
            albumName.text = track.collectionName
            albumNameGroup.visibility = View.VISIBLE
        } else {
            albumNameGroup.visibility = View.GONE
        }

        if (track.releaseDate != null) {
            musicYear.text = track.releaseDate.take(4)
            yearGroup.visibility = View.VISIBLE
        } else {
            yearGroup.visibility = View.GONE
        }

        if (track.primaryGenreName != null) {
            musicGenre.text = track.primaryGenreName
            genreGroup.visibility = View.VISIBLE
        } else {
            genreGroup.visibility = View.GONE
        }

        if (track.country != null) {
            musicCountry.text = track.country
            countryGroup.visibility = View.VISIBLE
        } else {
            countryGroup.visibility = View.GONE
        }
    }
}