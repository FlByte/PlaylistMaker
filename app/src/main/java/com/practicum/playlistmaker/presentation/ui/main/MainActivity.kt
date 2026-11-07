package com.practicum.playlistmaker.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.ui.library.LibraryActivity
import com.practicum.playlistmaker.presentation.ui.search.SearchActivity
import com.practicum.playlistmaker.presentation.ui.settings.SettingsActivity
import com.practicum.playlistmaker.presentation.util.ClickDebouncer

class MainActivity : AppCompatActivity() {

    private val clickDebouncer = ClickDebouncer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.search_button).setOnClickListener {
            if (clickDebouncer.clickDebounce()) {
                startActivity(Intent(this, SearchActivity::class.java))
            }
        }

        findViewById<Button>(R.id.library_button).setOnClickListener {
            if (clickDebouncer.clickDebounce()) {
                startActivity(Intent(this, LibraryActivity::class.java))
            }
        }

        findViewById<Button>(R.id.settings_button).setOnClickListener {
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