package com.example.tattle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.tattle.auth.currentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.SupabaseClient
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val supabase: SupabaseClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        currentActivity = this
        
        // Handle initial deep link
        supabase.handleDeeplinks(intent)

        // Request SMS permission if not already granted
        // DISABLING GATEWAY SERVICE TO PREVENT SDK 36 CRASH
        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
        } else {
            // 2. Start the Gateway Service
            startGatewayService()
        }
        */

        setContent {
            App()
        }
    }

    private fun startGatewayService() {
        val intent = Intent(this, SmsGatewayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        supabase.handleDeeplinks(intent)
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
fun AppAndroidPreview() {
    App()
}
