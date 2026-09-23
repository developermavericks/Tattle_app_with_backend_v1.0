package com.example.tattle.utils

import kotlin.time.Clock

actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
