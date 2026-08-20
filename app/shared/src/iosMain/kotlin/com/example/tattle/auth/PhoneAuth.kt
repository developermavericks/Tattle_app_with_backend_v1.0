package com.example.tattle.auth

actual suspend fun verifyPhoneNumber(
    phoneNumber: String,
    onCodeSent: (String) -> Unit,
    onVerified: () -> Unit,
    onError: (String) -> Unit
) {
    onError("iOS Phone Auth not implemented yet")
}

actual suspend fun signInWithCode(
    verificationId: String,
    code: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    onError("iOS Phone Auth not implemented yet")
}
