package com.jlianes.birthdaynotifier.presentation

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.jlianes.birthdaynotifier.R
import com.jlianes.birthdaynotifier.databinding.ActivitySettingsBinding
import com.jlianes.birthdaynotifier.framework.cloud.BirthdayFirestoreStorage
import com.jlianes.birthdaynotifier.framework.receiver.AlarmScheduler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Activity that allows configuring app settings like the notification time
 * and logging out of the current user.
 */
class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.buttonSetTime.setOnClickListener { showTimePicker() }
        binding.buttonLanguage.setOnClickListener { showLanguageDialog() }
        binding.buttonDeleteData.setOnClickListener { confirmDeleteData() }
        binding.buttonLogout.setOnClickListener { performLogout() }
    }

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

    private fun showTimePicker() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val hour = prefs.getInt("hour", 9)
        val minute = prefs.getInt("minute", 0)
        TimePickerDialog(this, { _, h, m ->
            prefs.edit().putInt("hour", h).putInt("minute", m).apply()
            AlarmScheduler.schedule(this)
        }, hour, minute, true).show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(
            getString(R.string.english),
            getString(R.string.spanish),
            getString(R.string.portuguese),
            getString(R.string.italian),
            getString(R.string.german),
            getString(R.string.french)
        )
        val codes = arrayOf("en", "es", "pt", "it", "de", "fr")
        val current = LocaleHelper.getLanguage(this)
        val checked = codes.indexOf(current).let { if (it >= 0) it else 0 }
        AlertDialog.Builder(this)
            .setTitle(R.string.language)
            .setSingleChoiceItems(languages, checked) { dialog, which ->
                LocaleHelper.setLocale(this, codes[which])
                dialog.dismiss()
                recreate()
            }
            .show()
    }

    private fun confirmDeleteData() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_data)
            .setMessage(R.string.delete_data_confirm)
            .setPositiveButton(R.string.yes) { _, _ -> requestDeletePhrase() }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun requestDeletePhrase() {
        val phrase = getString(R.string.delete_data_phrase)
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_data)
            .setMessage(getString(R.string.delete_data_type_phrase, phrase))
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
}
