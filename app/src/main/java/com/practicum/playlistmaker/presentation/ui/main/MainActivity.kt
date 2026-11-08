package com.practicum.playlistmaker.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.practicum.playlistmaker.databinding.ActivityMainBinding
import com.practicum.playlistmaker.presentation.ui.library.LibraryActivity
import com.practicum.playlistmaker.presentation.ui.search.SearchActivity
import com.practicum.playlistmaker.presentation.ui.settings.SettingsActivity
import com.practicum.playlistmaker.presentation.util.ClickDebouncer

class MainActivity : AppCompatActivity() {

    private val clickDebouncer = ClickDebouncer()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.searchButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) {
                startActivity(Intent(this, SearchActivity::class.java))
            }
        }

        binding.libraryButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) {
                startActivity(Intent(this, LibraryActivity::class.java))
            }
        }

        binding.settingsButton.setOnClickListener {
            if (clickDebouncer.clickDebounce()) {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickDebouncer.clear()
    }
}