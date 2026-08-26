package com.example.tattle.auth

actual suspend fun signInWithGoogle(
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit
) {
    onError("Desktop Google Auth not implemented")
}
