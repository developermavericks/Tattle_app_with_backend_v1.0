package com.example.tattle.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class DeviceDetails(val model: String, val manufacturer: String, val os: String, val appVersion: String)

/** Set by the platform entry point (MainActivity.onCreate/onNewIntent) when the app was opened from a breaking-news
 * notification; App() observes it and opens that story, then clears it so re-entering the screen does not repeat it. */
object PendingDeepLink {
    var articleId: String? by mutableStateOf(null)
}

/** City-level position: coordinates are rounded again by the server (about 1 km). */
data class ApproxLocation(val lat: Double, val lon: Double, val city: String?, val region: String?, val country: String?)

/** Phone model, OS version and app version (sent to the server so bugs can be fixed on the phones people really use). */
expect fun deviceDetails(): DeviceDetails

/** True when the OS already lets the app read the approximate location. */
expect fun locationPermissionGranted(): Boolean

/** Asks the OS for the (coarse) location permission when needed, then returns the current approximate position, or null when it was refused or is unavailable. */
expect suspend fun fetchApproxLocation(): ApproxLocation?

/** Does the phone's system setting currently let the app show notifications? */
expect fun notificationsAllowed(): Boolean

/** True when the OS reports an active network connection right now. The "offline" banner should only ever show when this is false -
 * a failed server call on its own does not mean the phone itself is offline (the server could just be restarting, slow, etc). */
expect fun isDeviceOnline(): Boolean

/** Asks the OS for the notification permission where one exists (Android 13+); returns whether notifications are allowed afterwards. */
expect suspend fun requestNotificationPermission(): Boolean

/**
 * Login code from an SMS. The OS shows one dialog asking whether the app may read THAT message; the app never gets access to the inbox.
 * onCode receives the 6-digit code. Call stopOtpAutofill when the screen goes away.
 */
expect fun startOtpAutofill(onCode: (String) -> Unit)

expect fun stopOtpAutofill()

/** Why no position could be had: permission_denied, location_off or no_fix (sent to the server so the dashboard can say why). */
expect fun locationFailureReason(): String

/** Shows a real system notification (the admin dashboard's notifications arrive this way). Tapping it opens the app -
 * and, when `articleId` is set (a breaking-news push), opens that story directly. `sentAtIso` (ISO-8601 UTC, "...Z")
 * drives the OS's own live-updating "2h ago" display; omitted or unparseable, the notification just shows "now". */
expect fun showLocalNotification(id: Int, title: String, body: String, articleId: String? = null, sentAtIso: String? = null)
