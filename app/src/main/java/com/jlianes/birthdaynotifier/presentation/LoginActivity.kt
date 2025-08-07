package com.jlianes.birthdaynotifier.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jlianes.birthdaynotifier.R

/**
 * Activity responsible for handling Google Sign-In and Firebase authentication.
 *
 * If the user is already authenticated, it redirects to the main activity.
 * Otherwise, it triggers the Google Sign-In flow.
 */
class LoginActivity : BaseActivity() {

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
            Toast.makeText(this, getString(R.string.login_failed, e.message), Toast.LENGTH_SHORT).show()
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

        setContent {
            MaterialTheme {
                LoginScreen {
                    signInLauncher.launch(googleSignInClient.signInIntent)
                }
            }
        }
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
                    Toast.makeText(this, getString(R.string.auth_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Navigates to the main activity and finishes the login screen.
     */
    private fun goToMain() {
        startActivity(Intent(this, BirthdayListActivity::class.java))
        finish()
    }

    @Composable
    private fun LoginScreen(onSignIn: () -> Unit) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cake),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = onSignIn,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.login))
                    }
                }
            }
        }
    }
}