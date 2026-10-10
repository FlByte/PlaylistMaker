package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val clickDebouncer = ClickDebouncer()
    private lateinit var searchButton: Button
    private lateinit var libraryButton: Button
    private lateinit var settingsButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        searchButton = findViewById(R.id.search_button)
        libraryButton = findViewById(R.id.library_button)
        settingsButton = findViewById(R.id.settings_button)

        searchButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) {
                val displayIntent = Intent(this, SearchActivity::class.java)
                startActivity(displayIntent)
            }
        }

        libraryButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) {
                val displayIntent = Intent(this, LibraryActivity::class.java)
                startActivity(displayIntent)
            }
        }

        settingsButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) {
                val displayIntent = Intent(this, SettingsActivity::class.java)
                startActivity(displayIntent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickDebouncer.clear()
    }
}