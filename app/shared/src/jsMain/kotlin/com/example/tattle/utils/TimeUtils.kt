package com.example.tattle.utils

import kotlin.js.Date

actual fun currentTimeMillis(): Long = Date.now().toLong()
