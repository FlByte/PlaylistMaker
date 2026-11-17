package com.practicum.playlistmaker.presentation.ui.search

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.presentation.ui.player.PlayerActivity
import com.practicum.playlistmaker.presentation.util.ClickDebouncer
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels { SearchViewModel.Factory }
    private val clickDebouncer = ClickDebouncer()
    private val searchAdapter = MusicAdapter(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener { finish() }

        binding.musicRecyclerView.adapter = searchAdapter

        binding.clearTextButton.setOnClickListener { view ->
            (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.hideSoftInputFromWindow(view.windowToken, 0)
            viewModel.onClearClicked()
        }

        binding.reloadButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) viewModel.onReloadClicked()
        }

        binding.clearHistoryButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) viewModel.onClearHistoryClicked()
        }

        binding.searchEditFrame.doOnTextChanged { text, _, _, _ ->
            binding.clearTextButton.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            viewModel.onQueryChanged(text?.toString().orEmpty())
        }

        binding.searchEditFrame.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onFocusChanged(hasFocus)
        }

        searchAdapter.setOnItemClickListener { track ->
            if (clickDebouncer.clickDebounce()) {
                val trackId = viewModel.onTrackClicked(track)
                startActivity(PlayerActivity.newIntent(this, trackId))
            }
        }

        viewModel.observeQuery().observe(this) { query ->
            if (binding.searchEditFrame.text.toString() != query) binding.searchEditFrame.setText(query)
        }

        viewModel.observeState().observe(this) { render(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickDebouncer.clear()
    }

    private fun render(state: SearchUiState) {
        hideMessage()
        binding.progressBar.visibility = View.GONE

        when (state) {
            is SearchUiState.History -> {
                searchAdapter.submit(state.tracks)
                binding.apply {
                    historyText.visibility = View.VISIBLE
                    clearHistoryButton.visibility = View.VISIBLE
                    musicRecyclerView.visibility = View.VISIBLE
                }
            }
            is SearchUiState.Tracks -> {
                searchAdapter.submit(state.tracks)
                binding.musicRecyclerView.visibility = View.VISIBLE
            }
            is SearchUiState.Loading -> binding.progressBar.visibility = View.VISIBLE
            is SearchUiState.Error -> showMessage(getString(R.string.ethernet_error), true)
            is SearchUiState.Empty -> showMessage(getString(R.string.nothing_found), false)
            is SearchUiState.MaxQueryLenght -> showMessage(getString(R.string.to_long_query), false)
        }
    }

    private fun showMessage(text: String, ethernetError: Boolean) {
        binding.apply {
            musicRecyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            errorText.visibility = View.VISIBLE
            errorImage.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.GONE
            historyText.visibility = View.GONE
            errorText.text = text
        }
        searchAdapter.submit(emptyList())

        if (ethernetError) {
            binding.reloadButton.visibility = View.VISIBLE
            binding.errorImage.setImageResource(R.drawable.ethernet_error)
        } else {
            binding.reloadButton.visibility = View.GONE
            binding.errorImage.setImageResource(R.drawable.nothing_found)
        }
    }

    private fun hideMessage() {
        binding.errorText.visibility = View.GONE
        binding.errorImage.visibility = View.GONE
        binding.reloadButton.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
        binding.historyText.visibility = View.GONE
        binding.musicRecyclerView.visibility = View.GONE
    }

    class MusicAdapter(
        initial: List<Track>
    ) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

        private val items = ArrayList<Track>(initial)
        private var onItemClickListener: ((Track) -> Unit)? = null
        fun setOnItemClickListener(listener: (Track) -> Unit) { onItemClickListener = listener }

        fun submit(newItems: List<Track>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.music_card, parent, false)
            return MusicViewHolder(view)
        }

        override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val trackName = itemView.findViewById<TextView>(R.id.track_name)
            private val artistName = itemView.findViewById<TextView>(R.id.artist_name)
            private val trackTime = itemView.findViewById<TextView>(R.id.track_time)
            private val albumImg = itemView.findViewById<ImageView>(R.id.album_img)

            private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

            init {
                itemView.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener?.invoke(items[position])
                    }
                }
            }

            fun bind(model: Track) {
                trackName.text = model.trackName
                artistName.text = model.artistName
                trackTime.text = timeFormat.format(model.trackTimeMillis)
                Glide.with(itemView)
                    .load(model.artworkUrl100)
                    .placeholder(R.drawable.music_placeholder)
                    .centerCrop()
                    .transform(RoundedCorners(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            2f,
                            itemView.context.resources.displayMetrics
                        ).toInt()
                    ))
                    .into(albumImg)
            }
        }
    }
}