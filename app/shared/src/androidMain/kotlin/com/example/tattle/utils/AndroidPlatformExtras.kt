package com.example.tattle.utils

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** The application context, available to code that runs without an Activity (background checks, crash capture). Set in TattleApplication. */
object AppContext {
    @Volatile
    var app: Application? = null

    /** The white "T" status-bar icon (R.drawable.ic_stat_tattle of the app module). Passed in as an id because release builds rename and strip resources by name. */
    @Volatile
    var notificationIcon: Int = 0
}

// ---------------------------------------------------------------------------------------------------------------------------------
// Crash capture: the report is written to disk BEFORE the process dies; the next start uploads it (UserSync.uploadCrashes).
// ---------------------------------------------------------------------------------------------------------------------------------
object CrashStore {
    private const val DIR = "crash_reports"
    private const val MAX_FILES = 10

    fun install(app: Application) {
        AppContext.app = app
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try { save(app, error, thread.name) } catch (_: Throwable) { }
            previous?.uncaughtException(thread, error)           // the normal crash behaviour continues
        }
    }

    private fun dir(ctx: Context): File = File(ctx.filesDir, DIR).apply { mkdirs() }

    private fun save(ctx: Context, error: Throwable, threadName: String) {
        val d = deviceDetails()
        val trace = error.stackTraceToString()
        val json = JSONObject().apply {
            put("exception", error.javaClass.name)
            put("message", (error.message ?: "").take(900) + " [thread $threadName]")
            put("stack", trace.take(11500))
            put("screen", Breadcrumbs.screen)
            put("breadcrumbs", JSONArray(Breadcrumbs.snapshot()))
            put("appVersion", d.appVersion)
            put("device", "${d.manufacturer} ${d.model}".trim())
            put("os", d.os)
            put("fatal", true)
            put("occurredAt", System.currentTimeMillis())
        }
        val folder = dir(ctx)
        File(folder, "${System.currentTimeMillis()}.json").writeText(json.toString())
        folder.listFiles()?.sortedBy { it.name }?.dropLast(MAX_FILES)?.forEach { it.delete() }       // never let reports pile up
    }
}

actual fun readPendingCrashes(): List<PendingCrash> {
    val app = AppContext.app ?: return emptyList()
    return try {
        File(app.filesDir, "crash_reports").listFiles()?.sortedBy { it.name }?.mapNotNull { f ->
            try { PendingCrash(f.name, f.readText()) } catch (_: Exception) { null }
        } ?: emptyList()
    } catch (_: Exception) { emptyList() }
}

actual fun deletePendingCrash(id: String) {
    val app = AppContext.app ?: return
    try { File(File(app.filesDir, "crash_reports"), id).delete() } catch (_: Exception) { }
}

// ---------------------------------------------------------------------------------------------------------------------------------
// Why there is no position, and local notifications
// ---------------------------------------------------------------------------------------------------------------------------------
actual fun locationFailureReason(): String {
    if (!locationPermissionGranted()) return "permission_denied"
    val ctx = AppContext.app ?: return "no_fix"
    return try {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val off = if (Build.VERSION.SDK_INT >= 28) !lm.isLocationEnabled else !(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        if (off) "location_off" else "no_fix"
    } catch (_: Exception) { "no_fix" }
}

private const val PUSH_CHANNEL = "tattle_push_channel"
const val EXTRA_NOTIFICATION_ID = "tattle_notification_id"
const val EXTRA_ARTICLE_ID = "tattle_article_id"

actual fun showLocalNotification(id: Int, title: String, body: String, articleId: String?, sentAtIso: String?) {
    val ctx = AppContext.app ?: return
    if (!notificationsAllowed()) return
    try {
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(PUSH_CHANNEL, "Tattle News Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Breaking news alerts and daily briefings"
            })
        }
        val launch = (ctx.packageManager.getLaunchIntentForPackage(ctx.packageName) ?: Intent()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_ID, id)                       // so the dashboard can count how many people opened it
            if (!articleId.isNullOrBlank()) putExtra(EXTRA_ARTICLE_ID, articleId)   // so tapping it opens that specific story
        }
        val pending = PendingIntent.getActivity(ctx, id, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        @Suppress("DEPRECATION")
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(ctx, PUSH_CHANNEL) else Notification.Builder(ctx)
        val icon = AppContext.notificationIcon.takeIf { it != 0 } ?: android.R.drawable.ic_popup_reminder
        // A plain Notification.Builder already shows "when" relative to post time by default; setWhen() makes it
        // reflect when the story was actually sent instead - without this, a notification delivered late (the 15-minute
        // poll, or a delayed push) would misleadingly show "now" no matter how old the story actually is.
        // kotlinx.datetime.Instant, not java.time.Instant: the latter needs API 26+ (or core library desugaring,
        // not configured here) and this app's minSdk is 24.
        val whenMs = sentAtIso?.let { runCatching { kotlinx.datetime.Instant.parse(it).toEpochMilliseconds() }.getOrNull() }
        val n = builder.setSmallIcon(icon).setColor(0xFFE14344.toInt()).setContentTitle(title).setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body)).setAutoCancel(true).setContentIntent(pending)
            .apply { if (whenMs != null) { setWhen(whenMs); setShowWhen(true) } }
            .build()
        manager.notify(10_000 + id, n)
    } catch (_: Exception) { }
}
