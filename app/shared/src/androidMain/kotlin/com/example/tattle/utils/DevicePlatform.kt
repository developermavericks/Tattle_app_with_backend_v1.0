package com.example.tattle.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.tattle.auth.currentActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/** Bridges Activity.requestPermissions to suspend calls: MainActivity forwards onRequestPermissionsResult here. */
object PermissionBridge {
    private val waiting = mutableMapOf<Int, (Boolean) -> Unit>()

    fun onResult(requestCode: Int, grantResults: IntArray) {
        waiting.remove(requestCode)?.invoke(grantResults.any { it == PackageManager.PERMISSION_GRANTED })
    }

    suspend fun request(permission: String, code: Int): Boolean = requestAny(arrayOf(permission), code)

    /** Asks for several permissions in one dialog; true when at least one was granted (location: "precise" or "approximate"). */
    suspend fun requestAny(permissions: Array<String>, code: Int): Boolean {
        val activity = currentActivity ?: return false
        return suspendCancellableCoroutine { cont ->
            waiting[code] = { granted -> if (cont.isActive) cont.resume(granted) }
            cont.invokeOnCancellation { waiting.remove(code) }
            activity.runOnUiThread { activity.requestPermissions(permissions, code) }
        }
    }
}

private const val LOCATION_CODE = 4711
private const val NOTIFICATION_CODE = 4712

actual fun deviceDetails(): DeviceDetails {
    val version = try {
        val a = currentActivity
        a?.packageManager?.getPackageInfo(a.packageName, 0)?.versionName ?: ""
    } catch (_: Exception) { "" }
    return DeviceDetails(model = Build.MODEL ?: "", manufacturer = Build.MANUFACTURER ?: "", os = "Android ${Build.VERSION.RELEASE}", appVersion = version)
}

actual fun notificationsAllowed(): Boolean {
    val a = currentActivity ?: return true
    return try {
        (a.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).areNotificationsEnabled()
    } catch (_: Exception) { true }
}

actual fun isDeviceOnline(): Boolean {
    val a = currentActivity ?: return true           // can't check right now: assume online rather than spuriously showing "offline"
    return try {
        val cm = a.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (_: Exception) { true }
}

actual suspend fun requestNotificationPermission(): Boolean {
    val a = currentActivity ?: return false
    if (Build.VERSION.SDK_INT < 33) return notificationsAllowed()        // before Android 13 notifications need no permission
    if (a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return true
    return PermissionBridge.request(Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_CODE)
}

actual fun locationPermissionGranted(): Boolean {
    val a = currentActivity ?: return false
    return a.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        a.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

private fun preciseLocationGranted(): Boolean =
    currentActivity?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

private suspend fun fusedLocation(activity: Activity): Location? = try {
    val client = LocationServices.getFusedLocationProviderClient(activity)
    val cts = CancellationTokenSource()
    val current = withTimeoutOrNull(20_000) {
        suspendCancellableCoroutine<Location?> { cont ->
            client.getCurrentLocation(if (preciseLocationGranted()) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
            cont.invokeOnCancellation { cts.cancel() }
        }
    }
    current ?: suspendCancellableCoroutine<Location?> { cont ->
        client.lastLocation.addOnSuccessListener { if (cont.isActive) cont.resume(it) }.addOnFailureListener { if (cont.isActive) cont.resume(null) }
    }
} catch (_: Exception) { null }

actual suspend fun fetchApproxLocation(): ApproxLocation? {
    val activity = currentActivity ?: return null
    if (!locationPermissionGranted() &&
        !PermissionBridge.requestAny(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_CODE)) return null
    return withContext(Dispatchers.IO) {
        try {
            val lm = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = buildList {
                if (Build.VERSION.SDK_INT >= 31) add(LocationManager.FUSED_PROVIDER)       // works with the approximate permission, on any phone with Google services
                add(LocationManager.NETWORK_PROVIDER); add(LocationManager.PASSIVE_PROVIDER)
            }
            var best: Location? = fusedLocation(activity)         // Google's fused provider first: works indoors and with the approximate permission
            for (p in providers) {
                try { lm.getLastKnownLocation(p)?.let { if (best == null || it.time > best!!.time) best = it } } catch (_: Exception) { }
            }
            if (best == null && Build.VERSION.SDK_INT >= 30) {
                best = withTimeoutOrNull(8_000) {
                    suspendCancellableCoroutine<Location?> { cont ->
                        try {
                            val provider = if (Build.VERSION.SDK_INT >= 31) LocationManager.FUSED_PROVIDER else LocationManager.NETWORK_PROVIDER
                            lm.getCurrentLocation(provider, null, Executors.newSingleThreadExecutor()) { loc -> if (cont.isActive) cont.resume(loc) }
                        } catch (_: Exception) { if (cont.isActive) cont.resume(null) }
                    }
                }
            }
            val loc = best ?: return@withContext null
            var city: String? = null; var region: String? = null; var country: String? = null
            try {
                if (Geocoder.isPresent()) {
                    @Suppress("DEPRECATION")
                    val a = Geocoder(activity, Locale.getDefault()).getFromLocation(loc.latitude, loc.longitude, 1)?.firstOrNull()
                    city = a?.locality ?: a?.subAdminArea; region = a?.adminArea; country = a?.countryName
                }
            } catch (_: Exception) { }
            ApproxLocation(loc.latitude, loc.longitude, city, region, country)
        } catch (_: Exception) { null }
    }
}

// ---------------------------------------------------------------------------------------------------------------------------------
// Reading the login code from an SMS with Google's SMS User Consent API: the app never gets the READ_SMS permission. When a message that
// contains a one-time code arrives, Android shows ONE dialog ("Allow Tattle to read this message?") and hands the app that single message only.
// ---------------------------------------------------------------------------------------------------------------------------------
private const val SMS_CONSENT_CODE = 4721
private var smsReceiver: BroadcastReceiver? = null
private var smsCallback: ((String) -> Unit)? = null
private val sixDigits = Regex("(?<!\\d)(\\d{6})(?!\\d)")

actual fun startOtpAutofill(onCode: (String) -> Unit) {
    val activity = currentActivity ?: return
    stopOtpAutofill()
    smsCallback = onCode
    try {
        SmsRetriever.getClient(activity).startSmsUserConsent(null)           // any sender; listens for the next 5 minutes
        val receiver = object : BroadcastReceiver() {
            @Suppress("DEPRECATION")
            override fun onReceive(context: Context, intent: Intent) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION != intent.action) return
                val extras = intent.extras ?: return
                val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status ?: return
                if (status.statusCode == CommonStatusCodes.SUCCESS) {
                    val consent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    try { if (consent != null) activity.startActivityForResult(consent, SMS_CONSENT_CODE) } catch (_: Exception) { }
                }
            }
        }
        val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (Build.VERSION.SDK_INT >= 33) activity.registerReceiver(receiver, filter, SmsRetriever.SEND_PERMISSION, null, Context.RECEIVER_EXPORTED)
        else activity.registerReceiver(receiver, filter, SmsRetriever.SEND_PERMISSION, null)
        smsReceiver = receiver
    } catch (_: Exception) { }
}

actual fun stopOtpAutofill() {
    val activity = currentActivity
    smsReceiver?.let { try { activity?.unregisterReceiver(it) } catch (_: Exception) { } }
    smsReceiver = null
    smsCallback = null
}

/** Called from MainActivity.onActivityResult with the answer to the consent dialog. */
fun handleSmsConsentResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode != SMS_CONSENT_CODE || resultCode != Activity.RESULT_OK) return
    val message = data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return
    sixDigits.find(message)?.groupValues?.get(1)?.let { smsCallback?.invoke(it) }
}
