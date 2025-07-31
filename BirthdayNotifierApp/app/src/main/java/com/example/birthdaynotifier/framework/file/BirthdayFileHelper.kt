package com.example.birthdaynotifier.framework.file

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.birthdaynotifier.framework.cloud.BirthdayFirestoreStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

/**
 * Helper class to manage reading and writing birthday data
 * stored in Firebase Firestore.
 *
 * @param context Application context, used for file access and resource loading.
 */
class BirthdayFileHelper(private val context: Context) {

    private var data = JSONArray()

    /**
     * Loads the birthday data from Firestore.
     */
    fun load() {
        val remote = runBlocking { BirthdayFirestoreStorage.downloadJson() }
        data = if (remote != null) JSONArray(remote) else JSONArray()
    }

    /**
     * Returns all birthday entries as a list of [JSONObject].
     */
    fun getAll(): List<JSONObject> = List(data.length()) { i -> data.getJSONObject(i) }

    /**
     * Returns the birthday entry at the given index.
     *
     * @param index Index of the item.
     * @return A [JSONObject] representing the birthday.
     */
    fun get(index: Int): JSONObject = data.getJSONObject(index)

    /**
     * Saves a birthday entry.
     * If [index] >= 0, it replaces the entry at that index.
     * If [index] < 0, it appends a new entry.
     * Automatically syncs to Firestore.
     *
     * @param index Position to insert/replace.
     * @param obj The birthday [JSONObject].
     */
    fun save(index: Int, obj: JSONObject) {
        if (index >= 0) data.put(index, obj)
        else data.put(obj)
        saveAllRemotely(context)
    }

    /**
     * Deletes the birthday entry at the given index.
     * Automatically syncs to Firestore.
     *
     * @param index Index of the entry to delete.
     */
    fun delete(index: Int) {
        data = JSONArray().apply {
            for (i in 0 until this@BirthdayFileHelper.data.length()) {
                if (i != index) put(this@BirthdayFileHelper.data.getJSONObject(i))
            }
        }
        saveAllRemotely(context)
    }

    /**
     * Uploads the current list of birthdays to Firestore for the current user.
     * This is done asynchronously in a coroutine.
     */
    private fun saveAllRemotely(context: Context) {
        val list = getAll().map {
            mapOf(
                "name" to it.getString("name"),
                "date" to it.getString("date"),
                "phone" to it.getString("phone")
            )
        }
        val json = JSONArray(list).toString()
        lifecycleScopeOrGlobal(context).launch {
            BirthdayFirestoreStorage.uploadJson(json)
        }
    }

    /**
     * Tries to get the [lifecycleScope] if [context] is an [AppCompatActivity].
     * Falls back to [GlobalScope] otherwise (discouraged but safe here for utility).
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun lifecycleScopeOrGlobal(context: Context): CoroutineScope {
        return (context as? AppCompatActivity)?.lifecycleScope ?: GlobalScope
    }
}