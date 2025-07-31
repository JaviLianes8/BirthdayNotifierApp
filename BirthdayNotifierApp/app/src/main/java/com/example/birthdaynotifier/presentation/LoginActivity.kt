package com.example.birthdaynotifier.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.birthdaynotifier.R
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Activity responsible for handling Google Sign-In and Firebase authentication.
 *
 * If the user is already authenticated, it redirects to the main activity.
 * Otherwise, it triggers the Google Sign-In flow.
 */
class LoginActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    /**
     * Handles the result from the Google Sign-In intent.
     */
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        @Suppress("DEPRECATION")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Initializes the sign-in flow or redirects to main if already authenticated.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        val account = auth.currentUser
        if (account != null) {
            goToMain()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        @Suppress("DEPRECATION")
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        signInLauncher.launch(googleSignInClient.signInIntent)
    }

    /**
     * Authenticates with Firebase using the Google ID token.
     *
     * @param idToken The ID token obtained from Google Sign-In.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goToMain()
                } else {
                    Toast.makeText(this, "Auth failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Navigates to the main activity and finishes the login screen.
     */
    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}