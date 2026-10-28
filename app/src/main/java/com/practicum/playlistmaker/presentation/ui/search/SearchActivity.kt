package com.practicum.playlistmaker.presentation.ui.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.SearchTracksInteractor
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.presentation.ui.player.PlayerActivity
import com.practicum.playlistmaker.presentation.util.ClickDebouncer
import java.text.SimpleDateFormat
import java.util.Locale

private const val SEARCH_DEBOUNCE_DELAY = 2000L
private const val TRACK_KEY = "track"

class SearchActivity : AppCompatActivity() {

    private val searchTracksInteractor: SearchTracksInteractor =
        Creator.provideSearchTracksInteractor()
    private val historyInteractor: SearchHistoryInteractor =
        Creator.provideSearchHistoryInteractor()

    private val clickDebouncer = ClickDebouncer()
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { findTracks() }

    private var searchString: String = ""

    private lateinit var inputEditText: EditText
    private lateinit var clearTextButton: ImageView
    private lateinit var musicRecyclerView: RecyclerView
    private lateinit var errorImage: ImageView
    private lateinit var errorText: TextView
    private lateinit var reloadButton: Button
    private lateinit var clearHistoryButton: Button
    private lateinit var historyText: TextView
    private lateinit var progressBar: ProgressBar

    private val trackList = ArrayList<Track>()
    private val searchAdapter = MusicAdapter(trackList)
    private val historyList = ArrayList<Track>()
    private val historyAdapter = MusicAdapter(historyList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.library)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.back_button).setOnClickListener { finish() }

        errorImage = findViewById(R.id.error_image)
        errorText = findViewById(R.id.error_text)
        reloadButton = findViewById(R.id.reload_button)
        clearHistoryButton = findViewById(R.id.clear_history)
        historyText = findViewById(R.id.history_text)
        progressBar = findViewById(R.id.progress_bar)
        inputEditText = findViewById(R.id.search_edit_frame)
        clearTextButton = findViewById(R.id.clear_text)
        musicRecyclerView = findViewById(R.id.music_recycler_view)

        musicRecyclerView.adapter = searchAdapter

        clearTextButton.setOnClickListener { view ->
            (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.hideSoftInputFromWindow(view.windowToken, 0)
            inputEditText.setText("")
            trackList.clear()
            searchAdapter.notifyDataSetChanged()
            hideMessage()
        }

        reloadButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) findTracks()
        }

        clearHistoryButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) {
                historyInteractor.clearHistory()
                historyList.clear()
                historyAdapter.notifyDataSetChanged()
                clearHistoryButton.visibility = View.GONE
                historyText.visibility = View.GONE
            }
        }

        inputEditText.doOnTextChanged { text, _, _, _ ->
            clearTextButton.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            searchString = text?.toString().orEmpty()

            if (inputEditText.hasFocus() && text.isNullOrEmpty()) {
                showHistory()
                trackList.clear()
                searchAdapter.notifyDataSetChanged()
            } else {
                hideHistory()
            }
            searchDebounce()
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) showHistory() else hideHistory()
        }

        searchAdapter.setOnItemClickListener { track ->
            if (clickDebouncer.clickDebounce()) {
                historyInteractor.addTrack(track)
                startActivity(Intent(this, PlayerActivity::class.java).apply {
                    putExtra(TRACK_KEY, track)
                })
            }
        }
        historyAdapter.setOnItemClickListener { track ->
            if (clickDebouncer.clickDebounce()) {
                historyInteractor.addTrack(track)
                showHistory()
                startActivity(Intent(this, PlayerActivity::class.java).apply {
                    putExtra(TRACK_KEY, track)
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainThreadHandler.removeCallbacks(searchRunnable)
        clickDebouncer.clear()
    }

    private fun searchDebounce() {
        mainThreadHandler.removeCallbacks(searchRunnable)
        mainThreadHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun showHistory() {
        val history = historyInteractor.getHistory()
        historyList.clear()
        historyList.addAll(history)
        historyAdapter.notifyDataSetChanged()

        if (history.isNotEmpty()) {
            historyText.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
            hideMessage()
            musicRecyclerView.adapter = historyAdapter
        } else {
            hideHistory()
        }
    }

    private fun hideHistory() {
        historyText.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        hideMessage()
        musicRecyclerView.adapter = searchAdapter
    }

    private fun findTracks() {
        val query = inputEditText.text.toString().trim()
        if (query.isEmpty()) return
        if (query.length > 200) {
            showMessage(getString(R.string.to_long_query), false)
            return
        }

        musicRecyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        hideMessage()
        historyText.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE

        searchTracksInteractor.search(
            query,
            onSuccess = { tracks ->
                trackList.clear()
                trackList.addAll(tracks)
                progressBar.visibility = View.GONE
                if (tracks.isEmpty()) {
                    showMessage(getString(R.string.nothing_found), false)
                } else {
                    musicRecyclerView.visibility = View.VISIBLE
                    searchAdapter.notifyDataSetChanged()
                }
            },
            onError = {
                showMessage(getString(R.string.ethernet_error), true)
            }
        )
    }

    private fun showMessage(text: String, ethernetError: Boolean) {
        musicRecyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorImage.visibility = View.VISIBLE
        clearHistoryButton.visibility = View.GONE
        historyText.visibility = View.GONE
        errorText.text = text

        trackList.clear()
        historyList.clear()
        searchAdapter.notifyDataSetChanged()
        historyAdapter.notifyDataSetChanged()

        if (ethernetError) {
            reloadButton.visibility = View.VISIBLE
            errorImage.setImageResource(R.drawable.ethernet_error)
        } else {
            reloadButton.visibility = View.GONE
            errorImage.setImageResource(R.drawable.nothing_found)
        }
    }

    private fun hideMessage() {
        errorText.visibility = View.GONE
        errorImage.visibility = View.GONE
        reloadButton.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SEARCH_FRAME, searchString)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchString = savedInstanceState.getString(SEARCH_FRAME, "")
        inputEditText.setText(searchString)
    }

    companion object {
        const val SEARCH_FRAME = "SEARCH_STRING"
    }

    class MusicAdapter(
        private val items: List<Track>
    ) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

        private var onItemClickListener: ((Track) -> Unit)? = null
        fun setOnItemClickListener(listener: (Track) -> Unit) { onItemClickListener = listener }

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