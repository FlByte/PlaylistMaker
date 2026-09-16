package com.practicum.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<Button>(R.id.back_button)

        backButton.setOnClickListener {
            finish()
        }

        val themeSwitcher = findViewById<SwitchCompat>(R.id.theme_switcher)
        val shareButton = findViewById<Button>(R.id.share_button)
        val supportButton = findViewById<Button>(R.id.support_button)
        val userAgreement = findViewById<Button>(R.id.user_agreement_button)

        val app = applicationContext as App

        themeSwitcher.isChecked = app.darkTheme

        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            app.switchTheme(checked)
        }

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.practicum_link)
                )
            }
            startActivity(shareIntent)
        }

        supportButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text))
            }
            startActivity(shareIntent)
        }

        userAgreement.setOnClickListener {
            val browseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.user_agreement_url)))
            startActivity(browseIntent)
        }
    }


}