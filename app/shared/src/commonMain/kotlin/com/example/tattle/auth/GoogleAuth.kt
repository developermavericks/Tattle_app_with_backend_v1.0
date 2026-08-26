package com.example.tattle.auth

expect suspend fun signInWithGoogle(
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit
)
