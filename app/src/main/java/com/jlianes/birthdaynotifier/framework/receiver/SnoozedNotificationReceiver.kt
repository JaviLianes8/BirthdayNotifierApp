package com.jlianes.birthdaynotifier.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jlianes.birthdaynotifier.framework.notification.WhatsAppBirthdayNotifier

/**
 * Receiver invoked after a snooze delay to show the original birthday notification again.
 */
class SnoozedNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(SnoozeReceiver.EXTRA_NAME) ?: return
        val message = intent.getStringExtra(SnoozeReceiver.EXTRA_MESSAGE) ?: return
        val phone = intent.getStringExtra(SnoozeReceiver.EXTRA_PHONE) ?: return

        WhatsAppBirthdayNotifier().notify(context, name, message, phone)
    }
}

