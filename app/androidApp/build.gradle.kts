import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.app.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.work.runtime)
    
    // Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // SMS Gateway dependencies
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverCIO)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.clientContentNegotiation)
    implementation(libs.ktor.serializationGson)
    implementation(libs.ktor.client.cio)

    implementation(platform(libs.firebase.android.bom))
    implementation(libs.firebase.android.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.core.ktx)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.tattle.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.tattle.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.1"
        // Plain HTTP is allowed only while the server address is a LAN / emulator one. With an https:// BASE_URL (the Cloudflare tunnel) it is switched off:
        // the phone then refuses to talk to the server unencrypted.
        val serverUrl = rootProject.file("local.properties").takeIf { it.exists() }
            ?.inputStream()?.use { Properties().apply { load(it) } }?.getProperty("BASE_URL").orEmpty()
        manifestPlaceholders["usesCleartext"] = if (serverUrl.startsWith("https://")) "false" else "true"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Release signing: the key lives OUTSIDE the repository. Point TATTLE_KEYSTORE_PROPS at a keystore.properties file
    // (storeFile, storePassword, keyAlias, keyPassword); without it the release build is simply unsigned.
    val signingProps = Properties().also { props ->
        System.getenv("TATTLE_KEYSTORE_PROPS")?.let { path -> file(path).takeIf { it.exists() }?.inputStream()?.use(props::load) }
    }
    signingConfigs {
        if (signingProps.containsKey("storeFile")) {
            create("release") {
                storeFile = file(signingProps.getProperty("storeFile"))
                storePassword = signingProps.getProperty("storePassword")
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true                  // R8: removes unused code and optimises the rest (faster start, smaller APK)
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}