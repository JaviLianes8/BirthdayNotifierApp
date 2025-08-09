package com.jlianes.birthdaynotifier.presentation

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.content.res.Configuration
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatDelegate
import com.jlianes.birthdaynotifier.R
import com.jlianes.birthdaynotifier.databinding.ActivitySettingsBinding
import com.jlianes.birthdaynotifier.framework.cloud.BirthdayFirestoreStorage
import com.jlianes.birthdaynotifier.framework.receiver.AlarmScheduler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Activity that allows configuring app settings like notification time, language,
 * theme, data deletion and logout.
 *
 * Se añade un reinicio controlado de la app tras cambiar idioma o tema para
 * que los recursos se apliquen sin que varíe el tamaño de los botones.
 */
class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    /**
     * Inflates the view, sets toolbar, and wires click listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.buttonSetTime.setOnClickListener { showTimePicker() }
        binding.buttonLanguage.setOnClickListener { showLanguageDialog() }
        binding.buttonTheme.setOnClickListener { showThemeDialog() }
        binding.buttonDeleteData.setOnClickListener { confirmDeleteData() }
        binding.buttonLogout.setOnClickListener { performLogout() }
    }

    /**
     * Default pass-through; kept to respect current behavior.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    /**
     * Signs out from Firebase + Google and navigates back to LoginActivity.
     */
    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, gso)
        client.signOut().addOnCompleteListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Shows a time picker and persists the chosen hour/minute.
     * Reschedules the daily alarm accordingly.
     */
    private fun showTimePicker() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val hour = prefs.getInt("hour", 9)
        val minute = prefs.getInt("minute", 0)
        TimePickerDialog(this, { _, h, m ->
            prefs.edit().putInt("hour", h).putInt("minute", m).apply()
            AlarmScheduler.schedule(this)
        }, hour, minute, true).show()
    }

    /**
     * Language selector. Applies locale and reinicia la app para refrescar recursos.
     */
    private fun showLanguageDialog() {
        val languages = arrayOf(
            getString(R.string.system_default),
            getString(R.string.english),
            getString(R.string.spanish),
            getString(R.string.portuguese),
            getString(R.string.italian),
            getString(R.string.german),
            getString(R.string.french)
        )
        val codes = arrayOf("system", "en", "es", "pt", "it", "de", "fr")
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val current = prefs.getString("language", "system")
        val checked = codes.indexOf(current).let { if (it >= 0) it else 0 }
        AlertDialog.Builder(this)
            .setTitle(R.string.language)
            .setSingleChoiceItems(languages, checked) { dialog, which ->
                LocaleHelper.setLocale(this, codes[which])
                dialog.dismiss()
                restartApp()
            }
            .show()
    }

    /**
     * Theme selector. Applies AppCompatDelegate mode y reinicia la app
     * para que se apliquen los cambios visuales en todo el task.
     */
    private fun showThemeDialog() {
        val themes = arrayOf(
            getString(R.string.light),
            getString(R.string.dark),
            getString(R.string.system_default)
        )
        val codes = arrayOf("light", "dark", "system")
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val current = prefs.getString("theme", "system")
        val checked = codes.indexOf(current).let { if (it >= 0) it else 2 }
        AlertDialog.Builder(this)
            .setTitle(R.string.theme)
            .setSingleChoiceItems(themes, checked) { dialog, which ->
                prefs.edit().putString("theme", codes[which]).apply()
                val mode = when (codes[which]) {
                    "light" -> AppCompatDelegate.MODE_NIGHT_NO
                    "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(mode)
                dialog.dismiss()
                restartApp()
            }
            .show()
    }

    /**
     * Asks the user before starting the "type phrase to confirm" flow.
     */
    private fun confirmDeleteData() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_data)
            .setMessage(R.string.delete_data_confirm)
            .setPositiveButton(R.string.yes) { _, _ -> requestDeletePhrase() }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    /**
     * Requests the confirmation phrase; if correct, clears cloud JSON and local settings.
     */
    private fun requestDeletePhrase() {
        val phrase = getString(R.string.delete_data_phrase)
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_data)
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.delete_data_type_phrase, phrase),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
            .setView(input)
            .setPositiveButton(R.string.yes) { _, _ ->
                if (input.text.toString() == phrase) {
                    lifecycleScope.launch {
                        BirthdayFirestoreStorage.uploadJson("[]")
                        getSharedPreferences("settings", MODE_PRIVATE).edit().clear().apply()
                        Toast.makeText(this@SettingsActivity, R.string.delete_data_done, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SettingsActivity, R.string.delete_data_incorrect, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Restarts the whole task so new locale/theme take effect across the app.
     * Keeps logic minimal and avoids side effects.
     */
    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finishAffinity()
    }
}