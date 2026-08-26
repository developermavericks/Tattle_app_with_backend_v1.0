package com.example.tattle.auth

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.tattle.PlatformConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun signInWithGoogle(
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit
) {
    val activity = currentActivity ?: run {
        onError("Activity not found")
        return
    }

    val credentialManager = CredentialManager.create(activity)

    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(PlatformConfig.GOOGLE_WEB_CLIENT_ID)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = withContext(Dispatchers.Main) {
            credentialManager.getCredential(
                context = activity,
                request = request
            )
        }
        
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            onSuccess(credential.idToken)
        } else {
            onError("Unexpected credential type")
        }
    } catch (e: GetCredentialException) {
        onError(e.message ?: "Google sign in failed")
    } catch (e: Exception) {
        onError(e.message ?: "An unknown error occurred")
    }
}
