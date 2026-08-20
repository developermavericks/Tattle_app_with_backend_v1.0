package com.example.tattle.auth

expect suspend fun verifyPhoneNumber(
    phoneNumber: String,
    onCodeSent: (String) -> Unit,
    onVerified: () -> Unit,
    onError: (String) -> Unit
)

expect suspend fun signInWithCode(
    verificationId: String,
    code: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
)
