package com.jlianes.birthdaynotifier.framework.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/**
 * Receiver that schedules a delayed notification when the user selects a snooze option.
 */
class SnoozeReceiver : BroadcastReceiver() {
    /**
     * Handles the snooze action by scheduling a new notification in one hour
     * and cancelling the current one.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(EXTRA_NAME) ?: return
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: return
        val phone = intent.getStringExtra(EXTRA_PHONE) ?: return

        val hours = 1

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

        val triggerAt = System.currentTimeMillis() + hours * 60 * 60 * 1000

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)

        NotificationManagerCompat.from(context).cancel(name.hashCode())
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_PHONE = "extra_phone"
    }
}

