package com.example.tattle.utils

/** A crash saved on the phone before the process died; uploaded at the next start. */
data class PendingCrash(val id: String, val json: String)

expect fun readPendingCrashes(): List<PendingCrash>

expect fun deletePendingCrash(id: String)

/** The last things the person did, and the screen they were on: attached to a crash report so it can be reproduced. */
object Breadcrumbs {
    var screen: String = "start"
    private var items: List<String> = emptyList()

    fun add(what: String) {
        items = (items + what.take(80)).takeLast(20)
    }

    fun snapshot(): List<String> = items
}
