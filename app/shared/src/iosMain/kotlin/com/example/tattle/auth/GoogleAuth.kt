package com.example.tattle.auth

actual suspend fun signInWithGoogle(
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit
) {
    onError("iOS Google Auth not implemented")
}
