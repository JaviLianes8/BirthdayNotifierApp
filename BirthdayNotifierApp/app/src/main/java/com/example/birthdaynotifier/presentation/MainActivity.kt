package com.example.birthdaynotifier.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.os.*
import androidx.appcompat.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.birthdaynotifier.R
import com.example.birthdaynotifier.domain.usecase.CheckTodaysBirthdaysUseCase
import com.example.birthdaynotifier.data.repository.BirthdayRepositoryImpl
import com.example.birthdaynotifier.framework.notification.WhatsAppBirthdayNotifier
import com.example.birthdaynotifier.framework.receiver.BirthdayReceiver
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*
import com.example.birthdaynotifier.databinding.ActivityMainBinding

/**
 * Main screen of the application.
 *
 * Provides three buttons:
 * - One to manually trigger today's birthday notifications.
 * - One to open the birthday list editor.
 * - One to logout the current user.
 *
 * Schedules a daily alarm at 09:00 to check birthdays.
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

        binding.buttonLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(this, gso)
            client.signOut().addOnCompleteListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        // Request notification permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        scheduleDailyCheck()
    }

    /**
     * Schedules a daily alarm at 09:00 to trigger automatic birthday checks.
     */
    private fun scheduleDailyCheck() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BirthdayReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}