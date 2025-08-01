package com.example.birthdaynotifier.presentation

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    /**
     * Returns the currently saved language code from SharedPreferences.
     *
     * @param context Context used to access SharedPreferences.
     * @return String Language code (e.g., "en", "es"). Defaults to "en" if not set.
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }

    /**
     * Applies the saved locale to the base context for proper resource localization.
     *
     * @param context Base context to wrap with the configured locale.
     * @return Context A new context with the locale applied.
     */
    fun applyBaseContext(context: Context): Context {
        val code = getLanguage(context)
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * Persists a new language code in SharedPreferences.
     *
     * @param context Context used to access SharedPreferences.
     * @param code String Language code to save (e.g., "en", "es").
     */
    fun setLocale(context: Context, code: String) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("language", code).apply()
    }
}