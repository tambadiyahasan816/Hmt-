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

    /**
     * Retrieves the current Firebase ID token.
     * Throws an IllegalStateException if the user is not authenticated.
     */
    suspend fun getFirebaseIdToken(forceRefresh: Boolean = false): String =
        suspendCancellableCoroutine { continuation ->
            val user = auth.currentUser
            if (user == null) {
                continuation.resumeWithException(IllegalStateException("No authenticated Firebase user found"))
                return@suspendCancellableCoroutine
            }
            user.getIdToken(forceRefresh)
                .addOnSuccessListener { result ->
                    val token = result.token
                    if (!token.isNullOrBlank()) {
                        continuation.resume(token)
                    } else {
                        continuation.resumeWithException(IllegalStateException("Received empty Firebase ID token"))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    fun getWebClientId(webClientId: String = ""): String {
        if (webClientId.isNotBlank()) return webClientId

        // 1. Try identifier with context.packageName
        var resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            val v = context.getString(resId)
            if (v.isNotBlank()) return v
        }

        // 2. Try identifier with project namespace "com.example"
        resId = context.resources.getIdentifier("default_web_client_id", "string", "com.example")
        if (resId != 0) {
            val v = context.getString(resId)
            if (v.isNotBlank()) return v
        }

        // 3. Try reflection on com.example.R$string
        try {
            val rClass = Class.forName("com.example.R\$string")
            val field = rClass.getField("default_web_client_id")
            val id = field.getInt(null)
            if (id != 0) {
                val v = context.getString(id)
                if (v.isNotBlank()) return v
            }
        } catch (_: Throwable) {}

        // 4. Try reflection on ${context.packageName}.R$string
        try {
            val rClass = Class.forName("${context.packageName}.R\$string")
            val field = rClass.getField("default_web_client_id")
            val id = field.getInt(null)
            if (id != 0) {
                val v = context.getString(id)
                if (v.isNotBlank()) return v
            }
        } catch (_: Throwable) {}

        return ""
    }

    suspend fun signInWithGoogle(webClientId: String = ""): FirebaseUser? {
        val serverClientId = getWebClientId(webClientId)

        if (serverClientId.isBlank()) {
            throw IllegalStateException(
                "Google Web Client ID is not configured. Please add your web client ID from Firebase Console or place google-services.json in the app/ folder."
            )
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
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

    companion object {
        /** Converts login exceptions into short messages a normal user can understand. */
        fun friendlyError(e: Throwable): String = when (e) {
            is androidx.credentials.exceptions.GetCredentialCancellationException ->
                "Google sign-in cancelled"
            is androidx.credentials.exceptions.NoCredentialException ->
                "No Google account found on this phone. Add one in Settings > Accounts, then try again."
            is androidx.credentials.exceptions.GetCredentialException ->
                "Google sign-in failed (${e.type}). Check SHA-1 and google-services.json in Firebase."
            else -> e.localizedMessage ?: "Google sign-in failed"
        }
    }
}

typealias AuthHelper = GoogleAuthHelper
