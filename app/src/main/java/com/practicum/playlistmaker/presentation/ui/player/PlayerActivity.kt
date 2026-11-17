package com.practicum.playlistmaker.presentation.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModels { PlayerViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.playerBackButton.setOnClickListener { finish() }

        binding.playerPlayButton.isEnabled = false

        binding.playerPlayButton.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        viewModel.observePlayerState().observe(this) {
            render(it)
        }
    }

    private fun render(state: PlayerUiState) {
        if (state !is PlayerUiState.Content) return
        val track = state.track

        binding.apply {
            playerTrackName.text = track?.trackName
            playerArtistName.text = track?.artistName
            musicDuration.text = state.formattedTime
            timer.text = state.positionText
            playerPlayButton.isEnabled = state.isPlayEnabled
            playerPlayButton.setBackgroundResource(
                if (state.isPlaying) R.drawable.stop else R.drawable.play
            )
        }

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
            .into(binding.playerAlbumImg)

        binding.albumNameGroup.visibility = View.GONE
        track?.collectionName?.let {
            binding.albumName.text = it
            binding.albumNameGroup.visibility = View.VISIBLE
        }
        binding.yearGroup.visibility = View.GONE
        track?.releaseYear?.let {
            binding.musicYear.text = it
            binding.yearGroup.visibility = View.VISIBLE
        }
        binding.genreGroup.visibility = View.GONE
        track?.primaryGenreName?.let {
            binding.musicGenre.text = it
            binding.genreGroup.visibility = View.VISIBLE
        }
        binding.countryGroup.visibility = View.GONE
        track?.country?.let {
            binding.musicCountry.text = it
            binding.countryGroup.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    companion object {
        private const val TRACK_KEY = "track_id"

        fun newIntent(context: Context, trackId: Long): Intent =
            Intent(context, PlayerActivity::class.java).apply {
                putExtra(TRACK_KEY, trackId)
            }
    }
}