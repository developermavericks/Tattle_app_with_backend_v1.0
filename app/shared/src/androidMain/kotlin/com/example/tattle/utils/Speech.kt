package com.example.tattle.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

actual object Speech {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var startWhenReady: (() -> Unit)? = null
    private val main = Handler(Looper.getMainLooper())
    private val whenDone = ConcurrentHashMap<String, () -> Unit>()

    private fun finish(id: String?) {
        id?.let { whenDone.remove(it) }?.let { callback -> main.post(callback) }
    }

    private fun ensure(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            val engine = tts
            if (ready && engine != null) {
                val indian = Locale("en", "IN")
                engine.language = if (engine.isLanguageAvailable(indian) >= TextToSpeech.LANG_AVAILABLE) indian else Locale.ENGLISH
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) = finish(utteranceId)
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) = finish(utteranceId)       // a failed story must not freeze the brief: move on
                    override fun onError(utteranceId: String?, errorCode: Int) = finish(utteranceId)
                })
                startWhenReady?.invoke()
            } else {
                startWhenReady = null
            }
        }
    }

    actual fun isAvailable(): Boolean = AppContext.app != null

    actual fun speak(text: String, onDone: () -> Unit) {
        val context = AppContext.app
        if (context == null || text.isBlank()) { onDone(); return }
        ensure(context)
        val id = "tattle-" + System.nanoTime()
        val start: () -> Unit = {
            whenDone[id] = onDone
            tts?.speak(text.take(3900), TextToSpeech.QUEUE_FLUSH, null, id)                 // one call may carry at most about 4000 characters
        }
        if (ready) start() else startWhenReady = start
    }

    actual fun stop() {
        startWhenReady = null
        whenDone.clear()
        tts?.stop()
    }
}
