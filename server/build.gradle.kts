plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
}

group = "com.example.tattle"
version = "1.0.0"
application {
    mainClass = "com.example.tattle.ApplicationKt"
}

dependencies {
    api(projects.core)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.client.cio)
    
    implementation(libs.ktor.serverAuthJwt)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.sqlite.jdbc)
    implementation(libs.postgres.jdbc)
    implementation(libs.google.api.client)

    implementation("io.ktor:ktor-client-content-negotiation-jvm:3.5.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.5.0")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.5.0")
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
