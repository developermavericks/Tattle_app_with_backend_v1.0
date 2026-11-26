package com.example.tattle.utils

/** Reads text aloud with the phone's own text-to-speech (no audio comes from the server). `onDone` is called on the main thread when the text has been read. */
expect object Speech {
    fun isAvailable(): Boolean
    fun speak(text: String, onDone: () -> Unit)
    fun stop()
}
