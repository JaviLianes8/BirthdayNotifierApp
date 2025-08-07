package com.jlianes.birthdaynotifier.presentation

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jlianes.birthdaynotifier.R
import com.jlianes.birthdaynotifier.data.repository.BirthdayRepositoryImpl
import com.jlianes.birthdaynotifier.domain.usecase.CheckTodaysBirthdaysUseCase
import com.jlianes.birthdaynotifier.framework.file.BirthdayFileHelper
import com.jlianes.birthdaynotifier.framework.notification.WhatsAppBirthdayNotifier
import com.jlianes.birthdaynotifier.framework.receiver.AlarmScheduler
import org.json.JSONObject
import java.util.Calendar

/**
 * Birthday list screen implemented with Jetpack Compose.
 */
class BirthdayListActivity : BaseActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val helper by lazy { BirthdayFileHelper(this) }
    private var statusText by mutableStateOf("" as CharSequence)
    private val clearStatus = Runnable { statusText = "" }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        AlarmScheduler.schedule(this)
        helper.load()

        setContent {
            MaterialTheme {
                BirthdayListScreen(
                    helper = helper,
                    status = statusText,
                    onManualCheck = { performManualCheck() },
                    onStatus = { show ->
                        statusText = show
                        handler.removeCallbacks(clearStatus)
                        handler.postDelayed(clearStatus, 10_000)
                    },
                    onOpenSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onOpenUrl = { openUrl(it) }
                )
            }
        }
    }

    private fun performManualCheck(): CharSequence {
        val repo = BirthdayRepositoryImpl()
        val today = "%02d-%02d".format(
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
            Calendar.getInstance().get(Calendar.MONTH) + 1
        )
        val names = repo.getAll(this)
            .filter { it.date.replace("/", "-").trim() == today }
            .map { it.name }

        val result = if (names.isEmpty()) {
            getString(R.string.no_birthdays)
        } else {
            val listItems = names.joinToString("\n") { "- $it" }
            val resId = if (names.size == 1) R.string.birthday_today else R.string.birthdays_today
            getString(resId, listItems)
        }

        CheckTodaysBirthdaysUseCase(
            repo,
            WhatsAppBirthdayNotifier()
        ).execute(this)

        return result
    }

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
private fun BirthdayListScreen(
    helper: BirthdayFileHelper,
    status: CharSequence,
    onManualCheck: () -> CharSequence,
    onStatus: (CharSequence) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val allBirthdays = remember { mutableStateListOf<JSONObject>().apply { addAll(helper.getAll()) } }
    var filterText by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(0) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    val displayed = remember(allBirthdays, filterText, sortOption) {
        allBirthdays.withIndex()
            .filter { it.value.getString("name").contains(filterText, ignoreCase = true) }
            .sortedWith(
                when (sortOption) {
                    0 -> compareBy { sortKey(it.value.getString("date")) }
                    1 -> compareByDescending { sortKey(it.value.getString("date")) }
                    2 -> compareBy { it.value.getString("name").lowercase() }
                    else -> compareByDescending { it.value.getString("name").lowercase() }
                }
            )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.open_json)) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Row(modifier = Modifier.padding(8.dp)) {
            SortDropdown(sortOption, onOptionSelected = { sortOption = it })
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                placeholder = { Text(stringResource(id = R.string.filter_name)) },
                modifier = Modifier.weight(1f)
            )
        }

        if (status.isNotBlank()) {
            Text(
                text = status.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(displayed) { (index, obj) ->
                BirthdayItem(obj) { editingIndex = index }
            }
        }

        FloatingButtonRow(
            onCheck = {
                onStatus(onManualCheck())
            },
            onAdd = { editingIndex = -1 }
        )

        BottomIconRow(onOpenUrl = onOpenUrl, onOpenSettings = onOpenSettings)
    }

    editingIndex?.let { idx ->
        val current = if (idx >= 0) allBirthdays[idx] else null
        EditBirthdayDialog(
            obj = current,
            onDismiss = { editingIndex = null },
            onSave = { json ->
                helper.save(idx, json)
                allBirthdays.clear(); allBirthdays.addAll(helper.getAll())
                editingIndex = null
            },
            onDelete = {
                if (idx >= 0) {
                    helper.delete(idx)
                    allBirthdays.clear(); allBirthdays.addAll(helper.getAll())
                }
                editingIndex = null
            }
        )
    }
}

@Composable
private fun SortDropdown(selected: Int, onOptionSelected: (Int) -> Unit) {
    val options = stringArrayResource(id = R.array.sort_options)
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize()) {
        OutlinedTextField(
            value = options[selected],
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .width(120.dp)
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onOptionSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BirthdayItem(obj: JSONObject, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = obj.getString("name"), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = stringResource(id = R.string.calendar_icon_desc),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = obj.getString("date"),
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            val msg = obj.optString("message")
            if (msg.isNotBlank()) {
                Text(text = msg, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun EditBirthdayDialog(
    obj: JSONObject?,
    onDismiss: () -> Unit,
    onSave: (JSONObject) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(obj?.getString("name") ?: "") }
    var date by remember { mutableStateOf(obj?.getString("date") ?: "") }
    var phone by remember { mutableStateOf(obj?.getString("phone") ?: "") }
    var message by remember { mutableStateOf(obj?.optString("message") ?: "") }
    val context = LocalContext.current

    val datePicker = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, _, month, dayOfMonth ->
                date = String.format("%02d-%02d", dayOfMonth, month + 1)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.findViewById<View>(
                context.resources.getIdentifier("year", "id", "android")
            )?.visibility = View.GONE
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(if (obj == null) R.string.add_birthday else R.string.edit_birthday))
        },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.hint_name)) })
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.hint_date)) },
                    readOnly = true,
                    modifier = Modifier.clickable { datePicker.show() }
                )
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(R.string.hint_phone)) })
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text(stringResource(R.string.hint_message)) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val json = JSONObject().apply {
                    put("name", name)
                    put("date", date)
                    put("phone", phone)
                    put("message", message)
                }
                onSave(json)
            }) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            Row {
                if (obj != null) {
                    TextButton(onClick = onDelete) {
                        Text(text = stringResource(id = R.string.delete))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        }
    )
}

@Composable
private fun BottomIconRow(onOpenUrl: (String) -> Unit, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
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

private fun sortKey(date: String): Int {
    val parts = date.replace("/", "-").split("-")
    val day = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val month = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return month * 100 + day
}

@Composable
private fun FloatingButtonRow(onCheck: () -> Unit, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ClassicButton(R.string.manual_check, onCheck)
        ClassicButton(R.string.add_contact, onAdd)
    }
}

@Composable
private fun ClassicButton(@androidx.annotation.StringRes textRes: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .widthIn(min = 160.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = stringResource(id = textRes))
    }
}
