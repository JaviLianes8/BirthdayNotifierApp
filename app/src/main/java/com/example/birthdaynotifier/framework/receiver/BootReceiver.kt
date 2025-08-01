package com.example.birthdaynotifier.framework.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver that reschedules the daily alarm after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        // Reschedule alarms when the device has finished booting
        AlarmScheduler.schedule(context)
    }
}
