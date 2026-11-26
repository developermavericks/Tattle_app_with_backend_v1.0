package com.example.tattle.utils

actual fun deviceDetails(): DeviceDetails = DeviceDetails(model = "", manufacturer = "", os = "js", appVersion = "")

actual fun locationPermissionGranted(): Boolean = false

actual suspend fun fetchApproxLocation(): ApproxLocation? = null

actual fun notificationsAllowed(): Boolean = true

actual fun isDeviceOnline(): Boolean = true

actual suspend fun requestNotificationPermission(): Boolean = true

actual fun startOtpAutofill(onCode: (String) -> Unit) {}

actual fun stopOtpAutofill() {}

actual fun locationFailureReason(): String = "no_fix"

actual fun showLocalNotification(id: Int, title: String, body: String, articleId: String?, sentAtIso: String?) {}
