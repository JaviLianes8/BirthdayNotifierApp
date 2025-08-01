package com.example.birthdaynotifier.presentation

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import com.example.birthdaynotifier.presentation.BaseActivity
import android.app.AlertDialog
import com.example.birthdaynotifier.R
import com.example.birthdaynotifier.databinding.ActivitySettingsBinding
import com.example.birthdaynotifier.framework.receiver.AlarmScheduler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.example.birthdaynotifier.presentation.LocaleHelper

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

        binding.buttonSetTime.setOnClickListener { showTimePicker() }
        binding.buttonLanguage.setOnClickListener { showLanguageDialog() }
        binding.buttonLogout.setOnClickListener { performLogout() }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.birthdaynotifier.R.string.default_web_client_id))
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
        val languages = arrayOf(getString(R.string.english), getString(R.string.spanish))
        val codes = arrayOf("en", "es")
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val current = prefs.getString("language", "en")
        val checked = if (current == "es") 1 else 0
        AlertDialog.Builder(this)
            .setTitle(R.string.language)
            .setSingleChoiceItems(languages, checked) { dialog, which ->
                LocaleHelper.setLocale(this, codes[which])
                dialog.dismiss()
                recreate()
            }
            .show()
    }
}
