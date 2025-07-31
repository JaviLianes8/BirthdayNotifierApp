package com.example.birthdaynotifier.data.repository

import android.content.Context
import com.example.birthdaynotifier.R
import com.example.birthdaynotifier.domain.model.Birthday
import com.example.birthdaynotifier.domain.repository.BirthdayRepository
import org.json.JSONArray
import java.io.File

class BirthdayRepositoryImpl : BirthdayRepository {
    override fun getAll(context: Context): List<Birthday> {
        val file = File(context.filesDir, "birthdays.json")
        if (!file.exists()) {
            val input = context.resources.openRawResource(R.raw.birthdays)
            file.writeBytes(input.readBytes())
        }
        val jsonArray = JSONArray(file.readText())
        return List(jsonArray.length()) { i ->
            val obj = jsonArray.getJSONObject(i)
            Birthday(
                name = obj.getString("name"),
                date = obj.getString("date"),
                phone = obj.getString("phone")
            )
        }
    }
}