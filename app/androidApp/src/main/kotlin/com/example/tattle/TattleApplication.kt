package com.example.tattle

import android.app.Application
import com.example.tattle.di.initKoin
import org.koin.android.ext.koin.androidContext
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class TattleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize App Check with Debug Provider for development
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        
        // Optional: Set the debug token if you want to reuse a specific one
        // This is often done via system property for the DebugAppCheckProvider
        System.setProperty("firebase.appcheck.debug.token", "AVweKojeG4ObomPY6nuucTu5HAU0xld5n1xcGgDS37jDR0Dmg-hu3ezl5j0lLJNQmArfKmIQIttYpIh7ZKp12Z-PzcGQuzjR5cbIaoIcVaqJaBLwS9xWOmQi_vumPxwX-z9T-DVxHweef4zgNVaf0mGWkA")

        initKoin {
            androidContext(this@TattleApplication)
        }
    }
}
