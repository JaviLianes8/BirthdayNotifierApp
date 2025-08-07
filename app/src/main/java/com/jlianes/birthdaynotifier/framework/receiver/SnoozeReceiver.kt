package com.jlianes.birthdaynotifier.framework.receiver

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput

/**
 * Receiver that schedules a delayed notification when the user selects a snooze option.
 */
class SnoozeReceiver : BroadcastReceiver() {

    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent) ?: return
        val hours = results.getCharSequence(KEY_SNOOZE_HOURS)?.toString()?.toIntOrNull() ?: return
        val name = intent.getStringExtra(EXTRA_NAME) ?: return
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: return
        val phone = intent.getStringExtra(EXTRA_PHONE) ?: return

        val resendIntent = Intent(context, SnoozedNotificationReceiver::class.java).apply {
            putExtra(EXTRA_NAME, name)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_PHONE, phone)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            name.hashCode(),
            resendIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //val triggerAt = System.currentTimeMillis() + hours * 60 * 60 * 1000
        val triggerAt = System.currentTimeMillis() + 15 * 1000  // 15 segundos

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)

        val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
    }

    companion object {
        const val KEY_SNOOZE_HOURS = "EXTRA_SNOOZE_HOURS"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_PHONE = "extra_phone"
    }
}

