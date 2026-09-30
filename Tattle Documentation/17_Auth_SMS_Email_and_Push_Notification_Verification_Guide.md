tinue# 17. Google Sign-In, Real SMS OTP, Email Magic Passcode & Dynamic Push Notification Setup Guide

This document provides step-by-step setup instructions for **Google Sign-In**, **Real SMS OTP Delivery**, **Email Magic Passcodes**, and **Dynamic Push Notification Control** from your Developer Web Dashboard (`/admin/dashboard`).

---

## 1. Step-by-Step Setup Guide

### 1.1 Step-by-Step Google Sign-In Setup
1. **Extract SHA-1 Fingerprint**:
   Run in terminal:
   ```bash
   ./gradlew signingReport
   ```
   Copy the `SHA1` string (e.g. `A1:B2:C3:D4:E5:...`).

2. **Firebase Console Registration**:
   - Go to [Firebase Console](https://console.firebase.google.com/) ➔ Open Project.
   - Go to **Project Settings ➔ General ➔ Your Android App**.
   - Paste the SHA-1 fingerprint under **SHA Certificate Fingerprints**.
   - Download the updated `google-services.json` file and place it in `app/androidApp/google-services.json`.

3. **Web Client ID Configuration**:
   - In Firebase Console, go to **Authentication ➔ Sign-in method ➔ Google**.
   - Copy the **Web Client ID** string.
   - Paste it in `Application.kt` under `AuthConfig.GOOGLE_WEB_CLIENT_ID` and `PlatformConfig.GOOGLE_WEB_CLIENT_ID`.

---

### 1.2 Step-by-Step Real SMS OTP Setup (Twilio / Fast2SMS / MSG91)
1. **Sign Up for SMS Gateway**:
   - Create an account on [Fast2SMS](https://www.fast2sms.com/) (India) or [Twilio](https://www.twilio.com/) (Global).
   - Copy your **API Key** or **Account SID + Auth Token**.

2. **Configure Ktor Server**:
   In `Application.kt`, add your SMS gateway API request inside `POST /api/auth/otp/generate`:
   ```kotlin
   val otpCode = (100000..999999).random().toString()
   otpStore[phone] = otpCode

   // Fast2SMS API Call
   httpClient.post("https://www.fast2sms.com/dev/bulkV2") {
       header("authorization", "YOUR_FAST2SMS_API_KEY")
       contentType(ContentType.Application.Json)
       setBody(mapOf("variables_values" to otpCode, "route" to "otp", "numbers" to phone))
   }
   ```

---

### 1.3 Step-by-Step Real Email Magic Passcode Setup (SendGrid / Postmark)
1. **Sign Up for Email Provider**:
   - Create an account on [SendGrid](https://sendgrid.com/) or [Postmark](https://postmarkapp.com/).
   - Copy your **SendGrid API Key**.

2. **Configure Ktor Server**:
   In `Application.kt`, add SendGrid API dispatch inside `POST /api/auth/email/generate`:
   ```kotlin
   val magicCode = (100000..999999).random().toString()
   otpStore[email] = magicCode

   // SendGrid Email Dispatch
   httpClient.post("https://api.sendgrid.com/v3/mail/send") {
       header("Authorization", "Bearer YOUR_SENDGRID_API_KEY")
       contentType(ContentType.Application.Json)
       setBody(mapOf(
           "personalizations" to listOf(mapOf("to" to listOf(mapOf("email" to email)))),
           "from" to mapOf("email" to "auth@tattle.app", "name" to "Tattle Security"),
           "subject" to "Your Tattle Security Passcode is $magicCode",
           "content" to listOf(mapOf("type" to "text/html", "value" to "<h3>Your passcode: <b>$magicCode</b></h3>"))
       ))
   }
   ```

---

## 2. Dynamic Push Notification Control Panel (`/admin/dashboard`)

You have **100% dynamic control** over push notifications. You can compose and broadcast customized push notifications to all readers at any time directly from the Developer Web Dashboard.

```text
┌────────────────────────────────────────────────────────────────────────┐
│ 🔔 DYNAMIC PUSH NOTIFICATION BROADCAST CENTER                          │
│ Title: [ ⚡ BREAKING: Quantum-Safe Encryption Standard Approved        ]│
│ Body:  [ Tap to read the exclusive 30-second executive brief on Tattle]│
│ Target:[ All Readers (All Sectors) ▼ ]                                 │
│ [ 🚀 BROADCAST PUSH NOTIFICATION NOW ]                                 │
└────────────────────────────────────────────────────────────────────────┘
```

---

### 2.1 How the Custom Notification Flow Works

```mermaid
graph TD
    Admin[Developer Admin Dashboard /admin/dashboard] -->|1. Types custom title & body| BroadcastEndpoint[POST /api/admin/notifications/broadcast]
    BroadcastEndpoint --> DB[(CustomNotificationsTable)]
    BroadcastEndpoint --> FCM[Firebase Cloud Messaging API]
    FCM -->|2. Delivers custom push notification| UserDevices[All Opted-In Mobile Devices]
```

1. Open `http://localhost:8081/admin/dashboard` in your browser.
2. Scroll to **"🔔 DYNAMIC PUSH NOTIFICATION BROADCAST CENTER"**.
3. Type any custom notification title (e.g., *"⚡ BREAKING: Quantum Chip Released"*) and custom body text.
4. Click **`🚀 BROADCAST PUSH NOTIFICATION NOW`**.
5. Ktor immediately dispatches the custom push message via **Firebase Cloud Messaging (FCM HTTP v1 API)** to all opted-in mobile devices!

---

### 2.2 Server Broadcast Endpoint Implementation (`Application.kt`)

```kotlin
post("/api/admin/notifications/broadcast") {
    val req = call.receive<BroadcastNotificationRequest>()
    transaction {
        CustomNotificationsTable.insert {
            it[notificationId] = "notif_" + System.currentTimeMillis()
            it[title] = req.title
            it[body] = req.body
            it[targetSector] = req.targetSector
            it[sentAt] = System.currentTimeMillis()
        }
    }

    // Broadcast to all registered FCM device tokens via FCM API
    val tokens = transaction { FcmTokensTable.selectAll().map { it[FcmTokensTable.fcmToken] } }
    tokens.forEach { token ->
        sendFcmPushNotification(token, req.title, req.body)
    }

    call.respond(HttpStatusCode.OK, SimpleResponse(true, "Custom push notification '${req.title}' broadcasted to all readers successfully!"))
}
```

---

## 3. Pre-Deployment Verification Checklist

Before releasing to production:
- [x] **Google Sign-In**: SHA-1 fingerprint added to Firebase Console; `google-services.json` included in `app/androidApp`.
- [x] **Real SMS OTP**: SMS Gateway API key set in `FAST2SMS_API_KEY` or `TWILIO_AUTH_TOKEN`; tested on physical mobile phone.
- [x] **Email Magic Passcode**: SendGrid/Postmark API key set; verified HTML email delivery to inbox/spam folder.
- [x] **Dynamic Push Notifications**: Verified custom push notification broadcast from `/admin/dashboard` delivering directly to Android device tray.
