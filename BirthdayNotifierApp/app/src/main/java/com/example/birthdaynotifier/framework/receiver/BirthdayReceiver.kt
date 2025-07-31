package com.example.birthdaynotifier.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.birthdaynotifier.domain.usecase.CheckTodaysBirthdaysUseCase
import com.example.birthdaynotifier.data.repository.BirthdayRepositoryImpl
import com.example.birthdaynotifier.framework.notification.WhatsAppBirthdayNotifier

/**
 * Receiver that checks for birthdays when triggered by an alarm or system event.
 */
class BirthdayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        CheckTodaysBirthdaysUseCase(
            BirthdayRepositoryImpl(),
            WhatsAppBirthdayNotifier()
        ).execute(context)
    }
}