package com.example.tattle.auth

actual suspend fun signInWithGoogle(
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit
) {
    onError("Web Google Auth not implemented")
}
