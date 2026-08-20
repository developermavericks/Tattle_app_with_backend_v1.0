package com.example.tattle.auth

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import android.app.Activity
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.coroutines.tasks.await

import java.lang.ref.WeakReference

// This will be set by the MainActivity
private var _currentActivity: WeakReference<Activity>? = null
var currentActivity: Activity?
    get() = _currentActivity?.get()
    set(value) {
        _currentActivity = value?.let { WeakReference(it) }
    }

actual suspend fun verifyPhoneNumber(
    phoneNumber: String,
    onCodeSent: (String) -> Unit,
    onError: (String) -> Unit
) {
    val activity = currentActivity ?: run {
        onError("Activity not found")
        return
    }

    val auth = FirebaseAuth.getInstance()
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-verification or instant verification
        }

        override fun onVerificationFailed(e: FirebaseException) {
            onError(e.message ?: "Verification failed")
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            onCodeSent(verificationId)
        }
    }

    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(callbacks)
        .build()
    
    PhoneAuthProvider.verifyPhoneNumber(options)
}

actual suspend fun signInWithCode(
    verificationId: String,
    code: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val credential = PhoneAuthProvider.getCredential(verificationId, code)
    
    try {
        auth.signInWithCredential(credential).await()
        onSuccess()
    } catch (e: Exception) {
        onError(e.message ?: "Sign in failed")
    }
}
