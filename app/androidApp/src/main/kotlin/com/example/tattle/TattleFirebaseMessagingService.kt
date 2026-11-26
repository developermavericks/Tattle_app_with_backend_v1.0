package com.example.tattle

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TattleFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        try { org.koin.core.context.GlobalContext.get().get<com.example.tattle.data.UserSync>().registerPushToken(token) } catch (_: Exception) { }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "⚡ Tattle Breaking News"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Tap to read the 30-second Executive Brief."

        // a notification written in the admin console carries its id: it is shown the same way (and with the same id) as one fetched by polling, so a phone that gets both shows one
        val id = remoteMessage.data["id"]?.toIntOrNull()
        if (id != null) {
            com.example.tattle.utils.showLocalNotification(id, title, body, remoteMessage.data["article_id"], remoteMessage.data["sentAt"])
        } else {
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "tattle_push_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tattle News Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Breaking news alerts and daily briefings"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.tattle.app.R.drawable.ic_stat_tattle)
            .setColor(0xFFE14344.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
