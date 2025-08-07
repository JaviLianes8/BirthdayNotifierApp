package com.jlianes.birthdaynotifier.presentation

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.jlianes.birthdaynotifier.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                SettingsScreen(
                    onSetTime = { showTimePicker() },
                    onLanguage = { showLanguageDialog() },
                    onTheme = { showThemeDialog() },
                    onDeleteData = { confirmDeleteData() },
                    onLogout = { performLogout() }
                )
            }
        }
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

    @Composable
    private fun SettingsScreen(
        onSetTime: () -> Unit,
        onLanguage: () -> Unit,
        onTheme: () -> Unit,
        onDeleteData: () -> Unit,
        onLogout: () -> Unit
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cake),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = onSetTime, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.set_time))
                }
                Button(onClick = onLanguage, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.language))
                }
                Button(onClick = onTheme, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.theme))
                }
                Button(onClick = onDeleteData, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.delete_data))
                }
                Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.logout))
                }
            }
        }
    }
}

