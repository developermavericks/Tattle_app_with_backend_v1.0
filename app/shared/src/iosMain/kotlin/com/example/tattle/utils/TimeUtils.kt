package com.example.tattle.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1960

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1960 * 1000).toLong()
