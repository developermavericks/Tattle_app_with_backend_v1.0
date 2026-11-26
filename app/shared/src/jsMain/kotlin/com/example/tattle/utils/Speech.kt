package com.example.tattle.utils

actual object Speech {
    actual fun isAvailable(): Boolean = false
    actual fun speak(text: String, onDone: () -> Unit) { onDone() }
    actual fun stop() {}
}
