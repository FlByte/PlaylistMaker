package com.practicum.playlistmaker

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private var searchString: String = SEARCH_STRING

    private var iTunesBaseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val ITunesService = retrofit.create(ITunesApi::class.java)

    private lateinit var backButton: Button
    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var musicRecyclerView: RecyclerView
    private lateinit var errorImage: ImageView
    private lateinit var errorText: TextView
    private lateinit var reloadButton: Button

    private val trackList = ArrayList<Track>()
    private val adapter = MusicAdapter(trackList)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.library)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton = findViewById(R.id.back_button)
        errorImage = findViewById(R.id.error_image)
        errorText = findViewById(R.id.error_text)
        reloadButton = findViewById(R.id.reload_button)

        backButton.setOnClickListener {
            finish()
        }

        inputEditText = findViewById(R.id.search_edit_frame)
        clearButton = findViewById(R.id.clear_text)

        clearButton.setOnClickListener { view ->
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
            inputEditText.setText("")
            trackList.clear()
            adapter.notifyDataSetChanged()
            errorText.visibility = View.GONE
            errorImage.visibility = View.GONE
            reloadButton.visibility = View.GONE
        }

        reloadButton.setOnClickListener {
            findTracks()
        }

        inputEditText.doOnTextChanged { text, _, _, _ ->
            clearButton.visibility = clearButtonVisibility(text)
            searchString = text.toString()
        }

        musicRecyclerView = findViewById(R.id.music_recycler_view)

        musicRecyclerView.adapter = adapter

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                findTracks()
                true
            }
            false
        }
    }

    private fun findTracks() {
        val query = inputEditText.text.toString().trim()
        if (query.isNotEmpty() && query.length <= 200) {
            errorText.visibility = View.GONE
            errorImage.visibility = View.GONE
            reloadButton.visibility = View.GONE
            ITunesService.search(inputEditText.text.toString()).enqueue(object: Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    if (response.code() == 200) {
                        trackList.clear()
                        if (response.body()?.results?.isNotEmpty() == true) {
                            trackList.addAll(response.body()?.results!!)
                            adapter.notifyDataSetChanged()
                        }
                        if (trackList.isEmpty()) {
                            showMessage(getString(R.string.nothing_found), false)
                        }
                    } else {
                        showMessage(getString(R.string.ethernet_error), true)
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    showMessage(getString(R.string.ethernet_error), true)
                }
            })
        } else if (query.length > 200) {
            showMessage(getString(R.string.to_long_query), false)
        }
    }

    private fun showMessage(text: String, ethernetError: Boolean) {
        if (text.isNotEmpty()) {
            errorText.visibility = View.VISIBLE
            errorImage.visibility = View.VISIBLE
            trackList.clear()
            adapter.notifyDataSetChanged()
            errorText.text = text
            if (ethernetError) {
                reloadButton.visibility = View.VISIBLE
                errorImage.setImageResource(R.drawable.ethernet_error)
            } else {
                reloadButton.visibility = View.GONE
                errorImage.setImageResource(R.drawable.nothing_found)
            }
        } else {
            errorText.visibility = View.GONE
            errorImage.visibility = View.GONE
            reloadButton.visibility = View.GONE
        }
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SEARCH_FRAME, searchString)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchString = savedInstanceState.getString(SEARCH_FRAME, SEARCH_STRING)
        findViewById<EditText>(R.id.search_edit_frame).setText(searchString)
    }

    companion object {
        const val SEARCH_FRAME = "SEARCH_STRING"
        const val SEARCH_STRING = ""
    }

    data class Track (
        val trackName: String,
        val artistName: String,
        val trackTimeMillis: Long,
        val artworkUrl100: String,
    )

    class TrackResponse (
        val resultCount: Int,
        val results: ArrayList<Track>
    )

    interface ITunesApi {
        @GET("/search")
        fun search(@Query("term") text: String) : Call<TrackResponse>
    }

    class MusicViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val trackName = itemView.findViewById<TextView>(R.id.track_name)
        private val artistName = itemView.findViewById<TextView>(R.id.artist_name)
        private val trackTime = itemView.findViewById<TextView>(R.id.track_time)
        private val albumImg = itemView.findViewById<ImageView>(R.id.album_img)

        fun bind(model: Track) {
            trackName.text = model.trackName
            artistName.text = model.artistName
            trackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(model.trackTimeMillis)
            Glide.with(itemView)
                .load(model.artworkUrl100)
                .placeholder(R.drawable.music_placeholder)
                .centerCrop()
                .transform(RoundedCorners(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        2f,
                        itemView.context.resources.displayMetrics).toInt()
                ))
                .into(albumImg)
        }
    }

    class MusicAdapter (private val track: List<Track>) : RecyclerView.Adapter<MusicViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.music_card, parent, false)
            return MusicViewHolder(view)
        }

        override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
            holder.bind(track[position])
        }

        override fun getItemCount(): Int {
            return track.size
        }
    }

}