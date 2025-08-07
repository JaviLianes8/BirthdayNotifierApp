package com.jlianes.birthdaynotifier.presentation

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import android.net.Uri
import android.provider.ContactsContract
import android.content.Context
import android.telephony.TelephonyManager
import android.text.InputType
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hbb20.CountryCodePicker
import com.jlianes.birthdaynotifier.R
import java.util.Calendar
import java.util.Locale
import com.jlianes.birthdaynotifier.presentation.BaseActivity
import com.jlianes.birthdaynotifier.framework.file.BirthdayFileHelper
import com.jlianes.birthdaynotifier.databinding.ActivityBirthdayListBinding
import com.jlianes.birthdaynotifier.presentation.LocaleHelper
import com.jlianes.birthdaynotifier.presentation.BirthdayAdapter
import org.json.JSONObject

/**
 * Activity that displays and manages a list of birthdays.
 *
 * Allows the user to add, edit, or delete birthday entries.
 * All changes are saved locally and synced with Firestore.
 */
class BirthdayListActivity : BaseActivity() {

    private lateinit var binding: ActivityBirthdayListBinding
    private lateinit var adapter: BirthdayAdapter
    private val helper by lazy { BirthdayFileHelper(this) }
    private var contactCallback: ((String, String) -> Unit)? = null
    private var displayedIndices: List<Int> = emptyList()
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
        supportActionBar?.setDisplayShowTitleEnabled(false)

        helper.load()
        adapter = BirthdayAdapter(this, helper.getAll().toMutableList())
        binding.listView.adapter = adapter

        ArrayAdapter.createFromResource(
            this,
            R.array.sort_options,
            R.layout.spinner_item
        ).also { spinAdapter ->
            spinAdapter.setDropDownViewResource(R.layout.spinner_item)
            binding.spinnerSort.adapter = spinAdapter
        }

        binding.spinnerSort.setSelection(0)
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.editFilter.addTextChangedListener {
            applyFilters()
        }

        binding.listView.setOnItemClickListener { _, _, pos, _ ->
            val originalIndex = displayedIndices.getOrNull(pos) ?: pos
            showEditDialog(originalIndex, helper.get(originalIndex))
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
        adapter.addAll(helper.getAll())
        adapter.notifyDataSetChanged()
        applyFilters()
    }

    private fun applyFilters() {
        val baseList = helper.getAll()
        val indexed = baseList.mapIndexed { idx, obj -> idx to obj }
        val sorted = when (binding.spinnerSort.selectedItemPosition) {
            0 -> indexed.sortedBy { sortKey(it.second.getString("date")) }
            1 -> indexed.sortedByDescending { sortKey(it.second.getString("date")) }
            2 -> indexed.sortedBy { it.second.getString("name").lowercase() }
            3 -> indexed.sortedByDescending { it.second.getString("name").lowercase() }
            else -> indexed
        }

        val filterText = binding.editFilter.text.toString().lowercase()
        val filtered = if (filterText.isNotEmpty()) {
            sorted.filter { it.second.getString("name").lowercase().contains(filterText) }
        } else {
            sorted
        }

        displayedIndices = filtered.map { it.first }
        adapter.clear()
        adapter.addAll(filtered.map { it.second })
        adapter.notifyDataSetChanged()
    }

    private fun sortKey(date: String): Int {
        val parts = date.replace("/", "-").split("-")
        val day = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return month * 100 + day
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
        val ccp = CountryCodePicker(this).apply {
            setCountryForNameCode(defaultCountryIso())
            val textColor = ContextCompat.getColor(
                this@BirthdayListActivity,
                R.color.md_theme_light_onBackground
            )
            setContentColor(textColor)
            setDialogTextColor(textColor)
        }
        val phoneInput = EditText(this).apply {
            hint = getString(R.string.hint_phone)
            inputType = InputType.TYPE_CLASS_PHONE
        }
        val phoneLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(ccp, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(phoneInput, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f))
        }
        val messageInput = EditText(this).apply { hint = getString(R.string.hint_message) }

        obj?.let {
            nameInput.setText(it.getString("name"))
            dateInput.setText(it.getString("date"))
            parseAndSetPhone(it.getString("phone"), ccp, phoneInput)
            messageInput.setText(it.optString("message"))
        }

        val importButton = Button(this).apply { text = getString(R.string.import_contact) }
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
            if (obj == null) addView(importButton)
            addView(nameInput)
            addView(dateInput)
            addView(phoneLayout)
            addView(messageInput)
        }

        if (obj == null) {
            importButton.setOnClickListener {
                contactCallback = { name, phone ->
                    nameInput.setText(name)
                    parseAndSetPhone(phone, ccp, phoneInput)
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
                    put("phone", ccp.selectedCountryCodeWithPlus + phoneInput.text.toString())
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

    private fun defaultCountryIso(): String {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return listOf(tm.networkCountryIso, tm.simCountryIso, Locale.getDefault().country)
            .firstOrNull { !it.isNullOrBlank() }?.uppercase(Locale.ROOT) ?: "US"
    }

    private fun parseAndSetPhone(phone: String, ccp: CountryCodePicker, phoneInput: EditText) {
        val util = PhoneNumberUtil.getInstance()
        try {
            val parsed = util.parse(phone, defaultCountryIso())
            ccp.setCountryForPhoneCode(parsed.countryCode)
            phoneInput.setText(parsed.nationalNumber.toString())
        } catch (e: Exception) {
            ccp.setCountryForNameCode(defaultCountryIso())
            phoneInput.setText(phone)
        }
    }
}