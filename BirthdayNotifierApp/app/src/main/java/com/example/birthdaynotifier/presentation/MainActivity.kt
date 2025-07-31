package com.example.birthdaynotifier.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.os.*
import androidx.appcompat.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.birthdaynotifier.R
import com.example.birthdaynotifier.domain.usecase.CheckTodaysBirthdaysUseCase
import com.example.birthdaynotifier.data.repository.BirthdayRepositoryImpl
import com.example.birthdaynotifier.framework.notification.WhatsAppBirthdayNotifier
import com.example.birthdaynotifier.framework.receiver.AlarmScheduler
import com.example.birthdaynotifier.databinding.ActivityMainBinding

/**
 * Main screen of the application.
 *
 * Provides three buttons:
 * - One to manually trigger today's birthday notifications.
 * - One to open the birthday list editor.
 * - One to logout the current user.
 *
 * Schedules a daily alarm at the configured time to check birthdays.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * Sets up the UI, handles click listeners, and performs initial sync.
     */
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar as Toolbar)

        // Button to manually trigger birthday notifications
        binding.buttonTest.setOnClickListener {
            CheckTodaysBirthdaysUseCase(
                BirthdayRepositoryImpl(),
                WhatsAppBirthdayNotifier()
            ).execute(this)
        }

        // Button to open birthday list editor
        binding.buttonOpen.setOnClickListener {
            startActivity(Intent(this, BirthdayListActivity::class.java))
        }

        // Open settings screen
        binding.buttonSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Request notification permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        AlarmScheduler.schedule(this)
    }

}