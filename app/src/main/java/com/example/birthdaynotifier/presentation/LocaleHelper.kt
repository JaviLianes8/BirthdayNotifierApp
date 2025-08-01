package com.example.birthdaynotifier.presentation

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    /** Returns the currently saved language code. */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }

    fun applyBaseContext(context: Context): Context {
        val code = getLanguage(context)
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun setLocale(context: Context, code: String) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("language", code).apply()
    }
}
