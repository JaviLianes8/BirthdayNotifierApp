package com.example.birthdaynotifier.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.os.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.birthdaynotifier.R
import com.example.birthdaynotifier.domain.usecase.CheckTodaysBirthdaysUseCase
import com.example.birthdaynotifier.data.repository.BirthdayRepositoryImpl
import com.example.birthdaynotifier.framework.notification.WhatsAppBirthdayNotifier
import com.example.birthdaynotifier.framework.receiver.BirthdayReceiver
import java.util.*

/**
 * Main entry point of the application.
 * Displays two buttons: one to trigger birthday notifications manually,
 * and another to open the birthday list editor.
 */
class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val buttonTest = Button(this).apply {
            text = getString(R.string.test_app)
            textSize = 24f
            setPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val buttonOpen = Button(this).apply {
            text = getString(R.string.open_json)
            textSize = 24f
            setPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        layout.addView(buttonTest)
        layout.addView(buttonOpen)

        setContentView(layout)

        buttonTest.setOnClickListener {
            CheckTodaysBirthdaysUseCase(
                BirthdayRepositoryImpl(),
                WhatsAppBirthdayNotifier()
            ).execute(this)
        }

        buttonOpen.setOnClickListener {
            startActivity(Intent(this, BirthdayListActivity::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        scheduleDailyCheck()
    }

    /**
     * Schedules a daily alarm at 09:00 to trigger birthday notifications.
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