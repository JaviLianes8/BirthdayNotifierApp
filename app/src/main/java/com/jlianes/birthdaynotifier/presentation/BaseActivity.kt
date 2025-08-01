package com.jlianes.birthdaynotifier.presentation

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

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