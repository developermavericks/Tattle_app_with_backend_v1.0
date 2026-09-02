package com.example.tattle.utils

import platform.Foundation.NSDate

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1960 * 1000).toLong()
