package com.jlianes.birthdaynotifier.presentation

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * Base activity that applies the persisted locale to the context and
 * automatically recreates itself when the language setting changes.
 *
 * Activities extending this class will:
 * - Use the saved language preference for all resources.
 * - Automatically reload if the language is changed while navigating.
 */
open class BaseActivity : AppCompatActivity() {

    /** Holds the last known language code for comparison. */
    private var currentLanguage: String? = null

    /**
     * Attaches the base context with the persisted locale applied.
     *
     * @param newBase Context Original base context.
     */
    override fun attachBaseContext(newBase: Context) {
        currentLanguage = LocaleHelper.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.applyBaseContext(newBase))
    }

    /**
     * Applies the selected theme before creating the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val mode = when (prefs.getString("theme", "system")) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
        super.onCreate(savedInstanceState)
    }

    /**
     * On resume, checks if the language has changed and recreates the activity if needed.
     */
    override fun onResume() {
        super.onResume()
        val lang = LocaleHelper.getLanguage(this)
        if (lang != currentLanguage) {
            currentLanguage = lang
            recreate()
        }
    }
}
