package com.example.birthdaynotifier.presentation

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContracts
import com.example.birthdaynotifier.R
import java.util.Calendar
import com.example.birthdaynotifier.presentation.BaseActivity
import com.example.birthdaynotifier.framework.file.BirthdayFileHelper
import com.example.birthdaynotifier.databinding.ActivityBirthdayListBinding
import com.example.birthdaynotifier.presentation.LocaleHelper
import org.json.JSONObject

/**
 * Activity that displays and manages a list of birthdays.
 *
 * Allows the user to add, edit, or delete birthday entries.
 * All changes are saved locally and synced with Firestore.
 */
class BirthdayListActivity : BaseActivity() {

    private lateinit var binding: ActivityBirthdayListBinding
    private lateinit var adapter: ArrayAdapter<String>
    private val helper by lazy { BirthdayFileHelper(this) }
    private var contactCallback: ((String, String) -> Unit)? = null
    private val contactPicker = registerForActivityResult(ActivityResultContracts.PickContact()) { uri: Uri? ->
        uri ?: return@registerForActivityResult

        // The URI returned by the picker points to Contacts.CONTENT_URI which
        // does not include phone columns. We must first resolve the contact ID
        // then query the Phone table for the number.

        val idProjection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
        var contactId = ""
        var name = ""
        contentResolver.query(uri, idProjection, null, null, null)?.use { c ->
            if (!c.moveToFirst()) return@registerForActivityResult
            contactId = c.getString(0)
            name = c.getString(1)
        } ?: return@registerForActivityResult

        val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phoneProjection,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )
        phoneCursor?.use { pc ->
            if (pc.moveToFirst()) {
                val phone = pc.getString(0)
                contactCallback?.invoke(name, phone)
            }
        }
    }

    /**
     * Initializes the UI and loads the birthday list.
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBirthdayListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = ArrayAdapter(this, R.layout.item_birthday)
        binding.listView.adapter = adapter

        helper.load()
        refreshList()

        binding.listView.setOnItemClickListener { _, _, pos, _ ->
            showEditDialog(pos, helper.get(pos))
        }

        binding.fab.setOnClickListener {
            showEditDialog(-1, null)
        }
    }

    /**
     * Refreshes the birthday list view by reloading all items.
     */
    private fun refreshList() {
        adapter.clear()
        helper.getAll().forEach {
            adapter.add(getString(R.string.birthday_item, it.getString("name"), it.getString("date")))
        }
    }

    /**
     * Displays a dialog to add or edit a birthday entry.
     *
     * @param index The position of the item in the list, or -1 for new entries.
     * @param obj The existing birthday JSON object, or null if creating a new one.
     */
    private fun showEditDialog(index: Int, obj: JSONObject?) {
        val nameInput = EditText(this).apply { hint = getString(R.string.hint_name) }
        val dateInput = EditText(this).apply {
            hint = getString(R.string.hint_date)
            isFocusable = false
            isClickable = true
        }
        dateInput.setOnClickListener { showDatePicker(dateInput) }
        val phoneInput = EditText(this).apply { hint = getString(R.string.hint_phone) }
        val messageInput = EditText(this).apply { hint = getString(R.string.hint_message) }

        obj?.let {
            nameInput.setText(it.getString("name"))
            dateInput.setText(it.getString("date"))
            phoneInput.setText(it.getString("phone"))
            messageInput.setText(it.optString("message"))
        }

        val importButton = Button(this).apply { text = getString(R.string.import_contact) }
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
            if (obj == null) addView(importButton)
            addView(nameInput)
            addView(dateInput)
            addView(phoneInput)
            addView(messageInput)
        }

        if (obj == null) {
            importButton.setOnClickListener {
                contactCallback = { name, phone ->
                    nameInput.setText(name)
                    phoneInput.setText(phone)
                }
                importContactWithPermission()
            }
        }

        AlertDialog.Builder(this)
            .setTitle(if (obj == null) getString(R.string.add_birthday) else getString(R.string.edit_birthday))
            .setView(dialogLayout)
            .setPositiveButton(R.string.save) { _, _ ->
                val newObj = JSONObject().apply {
                    put("name", nameInput.text.toString())
                    put("date", dateInput.text.toString())
                    put("phone", phoneInput.text.toString())
                    put("message", messageInput.text.toString())
                }
                helper.save(index, newObj)
                refreshList()
            }
            .setNegativeButton(R.string.delete) { _, _ ->
                if (index >= 0) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.are_you_sure)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            helper.delete(index)
                            refreshList()
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
                }
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    /**
     * Opens a date picker dialog and writes the selected day and month
     * to the provided EditText in "dd-MM" format.
     */
    private fun showDatePicker(target: EditText) {
        val cal = Calendar.getInstance()
        val parts = target.text.toString().split("-", "/")
        if (parts.size >= 2) {
            parts[0].toIntOrNull()?.let { cal.set(Calendar.DAY_OF_MONTH, it) }
            parts[1].toIntOrNull()?.let { cal.set(Calendar.MONTH, it - 1) }
        }

        val dialog = DatePickerDialog(this, { _, _, month, dayOfMonth ->
            target.setText(String.format("%02d-%02d", dayOfMonth, month + 1))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

        dialog.datePicker.findViewById<View>(resources.getIdentifier("year", "id", "android"))?.visibility = View.GONE
        dialog.show()
    }

    /**
     * Handles the result of a permission request to read contacts.
     * If granted, launches the contact picker. Otherwise, shows a warning toast.
     */
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                contactPicker.launch(null)
            } else {
                Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Requests the READ_CONTACTS permission if not already granted.
     * If permission is granted, launches the contact picker.
     * If rationale should be shown, shows it before requesting.
     */
    private fun importContactWithPermission() {
        when {
            checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                contactPicker.launch(null)
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS) -> {
                permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
            }
            else -> {
                permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
            }
        }
    }
}