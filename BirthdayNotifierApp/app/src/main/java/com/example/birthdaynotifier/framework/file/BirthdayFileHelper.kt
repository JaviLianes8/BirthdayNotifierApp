package com.example.birthdaynotifier.framework.file

import android.content.Context
import com.example.birthdaynotifier.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Helper for loading and saving birthday data to internal storage.
 */
class BirthdayFileHelper(private val context: Context) {
    private val file = File(context.filesDir, "birthdays.json")
    private var data = JSONArray()

    fun load() {
        if (!file.exists()) {
            context.resources.openRawResource(R.raw.birthdays).use {
                file.writeBytes(it.readBytes())
            }
        }
        data = JSONArray(file.readText())
    }

    fun getAll(): List<JSONObject> = List(data.length()) { i -> data.getJSONObject(i) }

    fun get(index: Int): JSONObject = data.getJSONObject(index)

    fun save(index: Int, obj: JSONObject) {
        if (index >= 0) data.put(index, obj)
        else data.put(obj)
        persist()
    }

    fun delete(index: Int) {
        data = JSONArray().apply {
            for (i in 0 until data.length()) {
                if (i != index) put(data.getJSONObject(i))
            }
        }
        persist()
    }

    private fun persist() {
        file.writeText(data.toString())
    }
}