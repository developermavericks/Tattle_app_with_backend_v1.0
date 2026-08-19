# SMS Gateway Integration Plan

This plan outlines the steps to integrate a local SMS Gateway into the Tattle Android application, allowing the backend to send OTPs via the device's SIM card.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///F:/Tech-Folder/MyApplication/gradle/libs.versions.toml)
- Add Ktor server and serialization dependencies required for the gateway.

#### [MODIFY] [build.gradle.kts](file:///F:/Tech-Folder/MyApplication/app/androidApp/build.gradle.kts)
- Include the new Ktor dependencies in the `androidApp` module.

---

### Android Manifest & Permissions

#### [MODIFY] [AndroidManifest.xml](file:///F:/Tech-Folder/MyApplication/app/androidApp/src/main/AndroidManifest.xml)
- Add permissions: `SEND_SMS`, `INTERNET`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE`.
- Register `SmsGatewayService` with `foregroundServiceType="connectedDevice"`.

---

### Gateway Service Implementation

#### [NEW] [SmsGatewayService.kt](file:///F:/Tech-Folder/MyApplication/app/androidApp/src/main/kotlin/com/example/tattle/SmsGatewayService.kt)
- Implement the `SmsGatewayService` class which:
    - Starts an embedded Ktor server on port 8080.
    - Listens for `POST /send-sms` requests.
    - Validates an API key.
    - Sends SMS using `SmsManager`.
    - Runs as a foreground service with a persistent notification.

---

### App Integration

#### [MODIFY] [MainActivity.kt](file:///F:/Tech-Folder/MyApplication/app/androidApp/src/main/kotlin/com/example/tattle/MainActivity.kt)
- Request `SEND_SMS` permission at runtime.
- Start `SmsGatewayService` when permission is granted.

## Verification Plan

### Automated Tests
- I will attempt a `gradle build` to ensure the new dependencies are correctly resolved and the code compiles.

### Manual Verification
- The user should:
    1. Run the app on a physical Android device with a SIM card.
    2. Grant SMS permission.
    3. Check if the "SMS Gateway Active" notification appears.
    4. Test the endpoint from a backend or using `curl` as provided in the instructions.
