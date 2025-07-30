package com.example.birthdaynotifier

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BirthdayListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var birthdays = JSONArray()
    private lateinit var file: File

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        listView = ListView(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = adapter

        val buttonAdd = Button(this).apply { text = "Add Birthday" }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        layout.addView(listView, params)
        layout.addView(buttonAdd)

        setContentView(layout)

        file = File(filesDir, "birthdays.json")
        if (!file.exists()) {
            val input = resources.openRawResource(R.raw.birthdays)
            file.writeBytes(input.readBytes())
        }

        loadBirthdays()

        listView.setOnItemClickListener { _, _, position, _ ->
            val obj = birthdays.getJSONObject(position)
            showEditDialog(position, obj)
        }

        buttonAdd.setOnClickListener {
            showEditDialog(-1, null)
        }
    }

    private fun loadBirthdays() {
        birthdays = JSONArray(file.readText())
        adapter.clear()
        for (i in 0 until birthdays.length()) {
            val obj = birthdays.getJSONObject(i)
            adapter.add("${obj.getString("name")} - ${obj.getString("date")}")
        }
    }

    private fun saveBirthdays() {
        file.writeText(birthdays.toString())
        loadBirthdays()
    }

    private fun showEditDialog(index: Int, obj: JSONObject?) {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val nameInput = EditText(this).apply { hint = "Name" }
        val dateInput = EditText(this).apply { hint = "Date (dd-mm)" }
        val phoneInput = EditText(this).apply { hint = "Phone" }

        if (obj != null) {
            nameInput.setText(obj.getString("name"))
            dateInput.setText(obj.getString("date"))
            phoneInput.setText(obj.getString("phone"))
        }

        dialogLayout.addView(nameInput)
        dialogLayout.addView(dateInput)
        dialogLayout.addView(phoneInput)

        val alert = AlertDialog.Builder(this)
            .setTitle(if (obj == null) "Add Birthday" else "Edit Birthday")
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString()
                val date = dateInput.text.toString()
                val phone = phoneInput.text.toString()
                val newObj = JSONObject().apply {
                    put("name", name)
                    put("date", date)
                    put("phone", phone)
                }

                if (index >= 0) {
                    birthdays.put(index, newObj)
                } else {
                    birthdays.put(newObj)
                }

                saveBirthdays()
            }
            .setNegativeButton("Delete") { _, _ ->
                if (index >= 0) {
                    AlertDialog.Builder(this)
                        .setTitle("Confirm delete")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes") { _, _ ->
                            birthdays = JSONArray().apply {
                                for (i in 0 until birthdays.length()) {
                                    if (i != index) put(birthdays.getJSONObject(i))
                                }
                            }
                            saveBirthdays()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
            .setNeutralButton("Cancel", null)
            .create()

        alert.show()
    }
}