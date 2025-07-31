package com.example.birthdaynotifier.domain.service

import android.content.Context

interface BirthdayNotifier {
    fun notify(context: Context, name: String, message: String, phone: String)
}