package com.jlianes.birthdaynotifier.presentation

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

/**
 * Utility object that handles reading and applying the app's preferred locale.
 */
object LocaleHelper {
    /**
     * Returns the currently saved language code from SharedPreferences.
     *
     * @param context Context used to access SharedPreferences.
     * @return String Language code (e.g., "en", "es"). Defaults to system language if not set.
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("language", "system") ?: "system"
    }

    /**
     * Applies the saved locale to the base context for proper resource localization.
     *
     * @param context Base context to wrap with the configured locale.
     * @return Context A new context with the locale applied.
     */
    fun applyBaseContext(context: Context): Context {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val code = prefs.getString("language", "system")
        val locale = if (code == "system") Locale.getDefault() else Locale(code!!)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        // Ensure both locale and locale list are set so that all resources
        // (including those expecting a single locale) resolve correctly.
        config.setLocale(locale)
        config.setLocales(LocaleList(locale))
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
        // Use commit to persist the preference synchronously before a potential
        // app restart, avoiding races where the new language might not be saved.
        prefs.edit().putString("language", code).commit()
    }
}
