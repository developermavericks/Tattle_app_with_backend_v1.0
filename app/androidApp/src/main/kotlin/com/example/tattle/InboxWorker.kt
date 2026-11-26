package com.example.tattle

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tattle.data.UserSync
import org.koin.core.context.GlobalContext

/** Runs about every 15 minutes even when the app is closed: asks the server for notifications sent from the dashboard and shows them. */
class InboxWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            GlobalContext.get().get<UserSync>().checkInbox()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
