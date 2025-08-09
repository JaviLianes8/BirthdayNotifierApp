package com.jlianes.birthdaynotifier.framework.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver that reschedules the daily alarm after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    /**
     * Invoked when the system finishes booting to reschedule the daily alarm.
     */
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        // Reschedule alarms when the device has finished booting
        AlarmScheduler.schedule(context)
    }
}
