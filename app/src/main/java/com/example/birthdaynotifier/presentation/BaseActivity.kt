package com.example.birthdaynotifier.presentation

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * Base activity that applies the persisted locale to the context and recreates
 * itself whenever the language is changed from the settings screen.
 */
open class BaseActivity : AppCompatActivity() {

    private var currentLanguage: String? = null

    override fun attachBaseContext(newBase: Context) {
        currentLanguage = LocaleHelper.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.applyBaseContext(newBase))
    }

    override fun onResume() {
        super.onResume()
        val lang = LocaleHelper.getLanguage(this)
        if (lang != currentLanguage) {
            currentLanguage = lang
            recreate()
        }
    }
}
