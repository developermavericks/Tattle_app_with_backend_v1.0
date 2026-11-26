package com.example.tattle

import android.app.Application
import com.tattle.app.R
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tattle.di.initKoin
import com.example.tattle.utils.AppContext
import com.example.tattle.utils.CrashStore
import org.koin.android.ext.koin.androidContext
import java.util.concurrent.TimeUnit

class TattleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CrashStore.install(this)             // crashes are saved to disk and uploaded at the next start
        AppContext.notificationIcon = R.drawable.ic_stat_tattle

        try {
            initKoin {
                androidContext(this@TattleApplication)
            }
        } catch (e: Exception) {
            println("TattleApplication: Koin init notice: ${e.message}")
        }

        try {                                // the phone's Firebase token (when Google services are there) goes to the server, so notifications can be pushed
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                try { org.koin.core.context.GlobalContext.get().get<com.example.tattle.data.UserSync>().registerPushToken(token) } catch (_: Exception) { }
            }
        } catch (e: Exception) {
            println("TattleApplication: Firebase messaging notice: ${e.message}")
        }

        try {
            // notifications from the admin dashboard also arrive while the app is closed (checked about every 15 minutes)
            val request = PeriodicWorkRequestBuilder<InboxWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork("tattle-inbox", ExistingPeriodicWorkPolicy.KEEP, request)
        } catch (e: Exception) {
            println("TattleApplication: WorkManager notice: ${e.message}")
        }
    }
}
