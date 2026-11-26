package com.example.tattle

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.tattle.auth.currentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.tattle.ui.screens.SplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        currentActivity = this
        reportNotificationOpen(intent)

        setContent {
            App()
        }
    }

    private fun sync() = try { org.koin.core.context.GlobalContext.get().get<com.example.tattle.data.UserSync>() } catch (_: Exception) { null }

    override fun onStart() {
        super.onStart()
        sync()?.appForeground()                  // time in the app is counted only while the app is on screen
    }

    override fun onStop() {
        sync()?.appBackground()
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        com.example.tattle.utils.PermissionBridge.onResult(requestCode, grantResults)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        com.example.tattle.utils.handleSmsConsentResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        reportNotificationOpen(intent)
    }

    /** Opened from a dashboard notification: tell the server (it counts how many people opened it), and if it was a
     * breaking-news push with a story behind it, tell App() to open that story directly. */
    private fun reportNotificationOpen(intent: android.content.Intent?) {
        val id = intent?.getIntExtra(com.example.tattle.utils.EXTRA_NOTIFICATION_ID, -1) ?: -1
        if (id >= 0) {
            sync()?.notificationOpened(id)
            intent?.removeExtra(com.example.tattle.utils.EXTRA_NOTIFICATION_ID)
        }
        val articleId = intent?.getStringExtra(com.example.tattle.utils.EXTRA_ARTICLE_ID)
        if (!articleId.isNullOrBlank()) {
            com.example.tattle.utils.PendingDeepLink.articleId = articleId
            intent.removeExtra(com.example.tattle.utils.EXTRA_ARTICLE_ID)
        }
    }

    override fun onDestroy() {
        if (currentActivity == this) {
            currentActivity = null
        }
        super.onDestroy()
    }
}

@Preview
@Composable
fun SplashPreview() {
    SplashScreen(onGetStarted = {})
}
