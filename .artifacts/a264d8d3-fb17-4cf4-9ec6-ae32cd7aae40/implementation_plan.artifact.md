# Secure Supabase Credentials Implementation Plan

This plan outlines the steps to move Supabase credentials from hardcoded values in the source code to a secure `local.properties` file, using the `BuildKonfig` plugin for Kotlin Multiplatform.

## User Review Required

> [!IMPORTANT]
> This change will require a Gradle Sync and Rebuild of the project.
> You will need to ensure that `local.properties` is **NOT** committed to version control (it is already in your `.gitignore` by default in most Android projects).

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///F:/Tech-Folder/MyApplication/gradle/libs.versions.toml)
- Add `buildkonfig` version and plugin definition.

#### [MODIFY] [build.gradle.kts](file:///F:/Tech-Folder/MyApplication/app/shared/build.gradle.kts)
- Apply the `buildkonfig` plugin.
- Configure `buildkonfig` to read from `local.properties` and generate fields for `commonMain`.

#### [MODIFY] [local.properties](file:///F:/Tech-Folder/MyApplication/local.properties)
- Add `SUPABASE_URL` and `SUPABASE_KEY` with the values provided.

---

### Source Code

#### [MODIFY] [Koin.kt](file:///F:/Tech-Folder/MyApplication/app/shared/src/commonMain/kotlin/com/example/tattle/di/Koin.kt)
- Replace hardcoded strings with references to the generated `BuildKonfig` object.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:shared:assembleDebug` to verify that the build succeeds and `BuildKonfig` is generated correctly.

### Manual Verification
- Verify that the app still initializes the Supabase client correctly at runtime (this will require the user to run the app).
