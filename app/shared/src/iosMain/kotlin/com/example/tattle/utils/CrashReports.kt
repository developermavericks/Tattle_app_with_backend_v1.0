package com.example.tattle.utils

actual fun readPendingCrashes(): List<PendingCrash> = emptyList()

actual fun deletePendingCrash(id: String) {}
