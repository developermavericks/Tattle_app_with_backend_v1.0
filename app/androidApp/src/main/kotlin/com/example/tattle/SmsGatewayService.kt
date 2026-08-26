package com.example.tattle

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.pm.ServiceInfo
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO as ClientCIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

data class SmsRequest(val phone: String, val message: String)
data class GatewayRegisterRequest(val ip: String, val port: Int)

class SmsGatewayService : Service() {

    private var server: EmbeddedServer<*, *>? = null
    private val apiKey = "TATTLE_CLEO_CM_MAVS"
    private val client = HttpClient(ClientCIO) {
        install(ClientContentNegotiation) {
            gson()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()
        startWebServer()
        sendHeartbeat()
        return START_STICKY
    }

    private fun sendHeartbeat() {
        val ip = getLocalIpAddress() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SmsGateway", "Sending heartbeat to ${PlatformConfig.BASE_URL}")
                client.post("${PlatformConfig.BASE_URL}/api/gateway/register") {
                    contentType(ContentType.Application.Json)
                    setBody(GatewayRegisterRequest(ip, 8080))
                }
            } catch (e: Exception) {
                Log.e("SmsGateway", "Heartbeat failed", e)
            }
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun startWebServer() {
        if (server != null) return

        server = embeddedServer(CIO, port = 8080) {
            install(ServerContentNegotiation) { gson() }
            
            routing {
                post("/send-sms") {
                    val authHeader = call.request.headers["Authorization"]
                    if (authHeader != "Bearer $apiKey") {
                        Log.w("SmsGateway", "Unauthorized request attempt")
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Token"))
                        return@post
                    }

                    val payload = call.receive<SmsRequest>()
                    Log.i("SmsGateway", "SMS Request received for: ${payload.phone}")
                    
                    try {
                        sendSms(payload.phone, payload.message)
                        Log.i("SmsGateway", "SMS Sent successfully to ${payload.phone}")
                        call.respond(mapOf("status" to "success", "message" to "SMS Sent"))
                    } catch (e: Exception) {
                        Log.e("SmsGateway", "Failed to send SMS", e)
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
                    }
                }
            }
        }.start(wait = false)
    }

    private fun sendSms(phoneNumber: String, message: String) {
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            this.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

    private fun startForegroundServiceNotification() {
        val channelId = "sms_gateway_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SMS Gateway Running", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SMS Gateway Active")
            .setContentText("Listening on port 8080 for OTP requests...")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        server?.stop(1000, 2000)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
