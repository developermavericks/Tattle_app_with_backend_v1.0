package com.example.tattle

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.tattle.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Tattle",
        ) {
            App()
        }
    }
}
