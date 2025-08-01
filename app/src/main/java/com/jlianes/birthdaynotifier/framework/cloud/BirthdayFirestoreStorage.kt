package com.jlianes.birthdaynotifier.framework.cloud

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles upload and download of birthday data to Firebase Firestore.
 *
 * Stores a single document per user (based on UID) under the "birthdays" collection,
 * where the full JSON is saved as a string in the "data" field.
 */
object BirthdayFirestoreStorage {

    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Current Firebase user UID.
     * Throws [IllegalStateException] if the user is not logged in.
     */
    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

    /**
     * Uploads the given JSON string to Firestore under the current user's document.
     *
     * @param json The JSON string to upload.
     */
    suspend fun uploadJson(json: String) {
        firestore.collection("birthdays").document(uid)
            .set(mapOf("data" to json))
            .await()
    }

    /**
     * Downloads the JSON string from Firestore for the current user.
     *
     * @return The JSON string if found, or null otherwise.
     */
    suspend fun downloadJson(): String? {
        val snapshot = firestore.collection("birthdays").document(uid).get().await()
        return snapshot.getString("data")
    }
}