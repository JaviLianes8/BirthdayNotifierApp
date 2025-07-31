package com.example.birthdaynotifier.presentation

import android.app.AlertDialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.birthdaynotifier.framework.file.BirthdayFileHelper
import org.json.JSONObject

/**
 * Activity to show and manage the list of birthdays.
 */
class BirthdayListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val helper by lazy { BirthdayFileHelper(this) }

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

        layout.addView(listView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        layout.addView(buttonAdd)

        setContentView(layout)

        helper.load()
        refreshList()

        listView.setOnItemClickListener { _, _, pos, _ ->
            showEditDialog(pos, helper.get(pos))
        }

        buttonAdd.setOnClickListener {
            showEditDialog(-1, null)
        }
    }

    private fun refreshList() {
        adapter.clear()
        helper.getAll().forEach {
            adapter.add("${it.getString("name")} - ${it.getString("date")}")
        }
    }

    private fun showEditDialog(index: Int, obj: JSONObject?) {
        val nameInput = EditText(this).apply { hint = "Name" }
        val dateInput = EditText(this).apply { hint = "Date (dd-mm)" }
        val phoneInput = EditText(this).apply { hint = "Phone" }

        obj?.let {
            nameInput.setText(it.getString("name"))
            dateInput.setText(it.getString("date"))
            phoneInput.setText(it.getString("phone"))
        }

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
            addView(nameInput)
            addView(dateInput)
            addView(phoneInput)
        }

        AlertDialog.Builder(this)
            .setTitle(if (obj == null) "Add Birthday" else "Edit Birthday")
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                val newObj = JSONObject().apply {
                    put("name", nameInput.text.toString())
                    put("date", dateInput.text.toString())
                    put("phone", phoneInput.text.toString())
                }
                helper.save(index, newObj)
                refreshList()
            }
            .setNegativeButton("Delete") { _, _ ->
                if (index >= 0) {
                    AlertDialog.Builder(this)
                        .setTitle("Confirm delete")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes") { _, _ ->
                            helper.delete(index)
                            refreshList()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
}