package com.example.birthdaynotifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BirthdayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
    }
}
