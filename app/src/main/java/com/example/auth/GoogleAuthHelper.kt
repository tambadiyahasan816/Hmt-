package com.example.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.MessageDigest
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * =====================================================================================
 * STEP THAT ONLY *YOU* CAN DO (cannot be done from code):
 * 1. Go to https://console.firebase.google.com/
 * 2. Select your project and navigate to Authentication > Sign-in method.
 * 3. Enable the Google sign-in provider.
 * 4. In Google provider settings, copy your "Web SDK configuration" Web Client ID.
 * 5. Add google-services.json to the app/ directory or provide the Web Client ID.
 * =====================================================================================
 */
class GoogleAuthHelper(private val context: Context) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

    val currentUser: FirebaseUser?
        get() = try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun signInWithGoogle(webClientId: String = ""): FirebaseUser? {
        val serverClientId = if (webClientId.isNotBlank()) {
            webClientId
        } else {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else ""
        }

        if (serverClientId.isBlank()) {
            throw IllegalStateException(
                "Google Web Client ID is not configured. Please add your web client ID from Firebase Console or place google-services.json in the app/ folder."
            )
        }

        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = hashNonce(rawNonce)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            return authCredential.signIn()
        }
        return null
    }

    private suspend fun AuthCredential.signIn(): FirebaseUser =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithCredential(this)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        continuation.resume(user)
                    } else {
                        continuation.resumeWithException(IllegalStateException("Firebase user is null"))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    private fun hashNonce(rawNonce: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(rawNonce.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

typealias AuthHelper = GoogleAuthHelper
