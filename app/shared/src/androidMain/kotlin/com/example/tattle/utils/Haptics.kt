package com.example.tattle.utils

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import com.example.tattle.auth.currentActivity

actual object Haptics {
    private val main = Handler(Looper.getMainLooper())

    // The system's own feedback constants: they follow the person's "touch feedback" setting and need no permission.
    private fun fire(constant: Int) {
        val view = currentActivity?.window?.decorView ?: return
        val run = { try { view.performHapticFeedback(constant) } catch (_: Exception) { } }
        if (Looper.myLooper() == Looper.getMainLooper()) run() else main.post { run() }
    }

    actual fun selection() = fire(HapticFeedbackConstants.CLOCK_TICK)
    actual fun light() = fire(if (Build.VERSION.SDK_INT >= 23) HapticFeedbackConstants.CONTEXT_CLICK else HapticFeedbackConstants.VIRTUAL_KEY)
    actual fun medium() = fire(HapticFeedbackConstants.KEYBOARD_TAP)
    actual fun heavy() = fire(HapticFeedbackConstants.LONG_PRESS)
    actual fun success() = fire(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.CONTEXT_CLICK)
    actual fun warning() = fire(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS)
}
