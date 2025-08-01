package com.example.birthdaynotifier.data.repository

import android.content.Context
import com.example.birthdaynotifier.framework.cloud.BirthdayFirestoreStorage
import com.example.birthdaynotifier.domain.model.Birthday
import com.example.birthdaynotifier.domain.repository.BirthdayRepository
import org.json.JSONArray
import kotlinx.coroutines.runBlocking

/**
 * Implementation of [BirthdayRepository] that retrieves birthday data
 * directly from Firebase Firestore.
 */
class BirthdayRepositoryImpl : BirthdayRepository {

    /**
     * Returns all birthdays downloading them from Firestore.
     *
     * @param context Application context.
     * @return A list of [Birthday] objects.
     */
    override fun getAll(context: Context): List<Birthday> {
        val json = runBlocking { BirthdayFirestoreStorage.downloadJson() } ?: "[]"
        val jsonArray = JSONArray(json)
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