package com.example.birthdaynotifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.example.birthdaynotifier.BirthdayUtils

class BirthdayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        BirthdayUtils.checkBirthdays(context)
    }
}
