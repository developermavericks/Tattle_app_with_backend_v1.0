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

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
