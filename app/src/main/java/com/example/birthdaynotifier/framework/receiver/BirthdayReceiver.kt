package com.example.birthdaynotifier.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.birthdaynotifier.domain.usecase.CheckTodaysBirthdaysUseCase
import com.example.birthdaynotifier.data.repository.BirthdayRepositoryImpl
import com.example.birthdaynotifier.framework.notification.WhatsAppBirthdayNotifier

/**
 * BroadcastReceiver that listens for scheduled alarms and triggers birthday checks.
 *
 * This receiver is typically invoked by the system's AlarmManager at a fixed time (e.g., daily).
 * It executes the [CheckTodaysBirthdaysUseCase] to find today's birthdays
 * and sends notifications via WhatsApp if any are found.
 */
class BirthdayReceiver : BroadcastReceiver() {

    /**
     * Called when the receiver is triggered.
     *
     * @param context Context in which the receiver is running.
     * @param intent The received broadcast Intent (not used).
     */
    override fun onReceive(context: Context, intent: Intent?) {
        CheckTodaysBirthdaysUseCase(
            BirthdayRepositoryImpl(),
            WhatsAppBirthdayNotifier()
        ).execute(context)
    }
}