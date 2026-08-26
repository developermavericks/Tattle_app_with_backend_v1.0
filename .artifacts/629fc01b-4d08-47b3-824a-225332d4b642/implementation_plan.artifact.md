# Use Firebase Phone Verification Test Token

The user provided a test token for Firebase Phone Number Verification. This token is used to bypass the Play Integrity / SafetyNet checks when testing phone authentication on emulators or in development environments.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///F:/Tech-Folder/MyApplication/gradle/libs.versions.toml)
- Add `firebase-appcheck-playintegrity` and `firebase-appcheck-debug` dependencies.

#### [MODIFY] [app/androidApp/build.gradle.kts](file:///F:/Tech-Folder/MyApplication/app/androidApp/build.gradle.kts)
- Include the new App Check dependencies.

### Android Implementation

#### [MODIFY] [TattleApplication.kt](file:///F:/Tech-Folder/MyApplication/app/androidApp/src/main/kotlin/com/example/tattle/TattleApplication.kt)
- Initialize Firebase App Check with the Debug Provider.
- Set the provided debug token so the emulator can pass the integrity check.

## Verification Plan

### Automated Tests
- Build the project to ensure dependencies are resolved correctly.

### Manual Verification
- The user should run the app on an emulator.
- Attempt phone number verification with the test number `+91 00 0000 0000`.
- The verification should proceed without the "App not authorized" error.
