package com.example.tattle.utils

/** Small taps for the phone's vibration motor, named like iOS feedback: selection (a tick), light/medium/heavy (impact), success and warning. */
expect object Haptics {
    fun selection()
    fun light()
    fun medium()
    fun heavy()
    fun success()
    fun warning()
}
