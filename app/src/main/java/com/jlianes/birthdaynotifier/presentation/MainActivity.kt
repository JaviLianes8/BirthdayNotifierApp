package com.jlianes.birthdaynotifier.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jlianes.birthdaynotifier.R
import com.jlianes.birthdaynotifier.data.repository.BirthdayRepositoryImpl
import com.jlianes.birthdaynotifier.domain.usecase.CheckTodaysBirthdaysUseCase
import com.jlianes.birthdaynotifier.framework.notification.WhatsAppBirthdayNotifier
import com.jlianes.birthdaynotifier.framework.receiver.AlarmScheduler
import java.util.Calendar

/**
 * Main screen of the application built entirely with Jetpack Compose.
 */
class MainActivity : BaseActivity() {

    private val handler = Handler(Looper.getMainLooper())
    @SuppressLint("MutableCollectionMutableState")
    private var statusText by mutableStateOf<CharSequence>("")
    private val clearStatus = Runnable { statusText = "" }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        AlarmScheduler.schedule(this)

        setContent {
            MaterialTheme {
                MainScreen(
                    status = statusText,
                    onManualCheck = { performManualCheck() },
                    onOpenList = {
                        startActivity(Intent(this, BirthdayListActivity::class.java))
                    },
                    onOpenSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onOpenUrl = { openUrl(it) }
                )
            }
        }
    }

    /** Executes the birthday check and updates the status text. */
    private fun performManualCheck() {
        val repo = BirthdayRepositoryImpl()
        val today = "%02d-%02d".format(
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
            Calendar.getInstance().get(Calendar.MONTH) + 1
        )
        val names = repo.getAll(this)
            .filter { it.date.replace("/", "-").trim() == today }
            .map { it.name }

        statusText = if (names.isEmpty()) {
            getString(R.string.no_birthdays)
        } else {
            val listItems = names.joinToString("\n") { "- $it" }
            val resId = if (names.size == 1) R.string.birthday_today else R.string.birthdays_today
            getString(resId, listItems)
        }

        handler.removeCallbacks(clearStatus)
        handler.postDelayed(clearStatus, 60_000)

        CheckTodaysBirthdaysUseCase(
            repo,
            WhatsAppBirthdayNotifier()
        ).execute(this)
    }

    /** Opens a web URL in the browser. */
    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(clearStatus)
        statusText = ""
    }
}

@Composable
private fun MainScreen(
    status: CharSequence,
    onManualCheck: () -> Unit,
    onOpenList: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = {
                Image(
                    painter = painterResource(id = R.drawable.ic_cake),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.logo_size))
                )
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onManualCheck,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            Text(text = stringResource(id = R.string.test_app))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenList,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            Text(text = stringResource(id = R.string.open_json))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (status.isNotBlank()) {
                Text(
                    text = status.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { onOpenUrl("https://www.linkedin.com/in/jlianes/") }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_linkedin),
                    contentDescription = stringResource(id = R.string.linkedin)
                )
            }
            IconButton(onClick = { onOpenUrl("https://buymeacoffee.com/jlianesglrs") }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_buymeacoffee),
                    contentDescription = stringResource(id = R.string.buy_me_coffee)
                )
            }
            IconButton(onClick = { onOpenUrl("https://github.com/JaviLianes8/BirthdayNotifierApp") }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_github),
                    contentDescription = stringResource(id = R.string.repo)
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
        }
    }
}
