package com.example.birthdaynotifier.domain.repository

import android.content.Context
import com.example.birthdaynotifier.domain.model.Birthday

interface BirthdayRepository {
    fun getAll(context: Context): List<Birthday>
}