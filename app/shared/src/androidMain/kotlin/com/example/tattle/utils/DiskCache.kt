package com.example.tattle.utils

import java.io.File
import java.security.MessageDigest

actual object DiskCache {
    private const val MAX_BYTES = 160L * 1024 * 1024            // the oldest pictures go first once this is exceeded
    private var writes = 0

    private fun dir(): File? = AppContext.app?.let { File(it.filesDir, "offline_images").apply { mkdirs() } }

    private fun name(key: String): String =
        MessageDigest.getInstance("SHA-256").digest(key.encodeToByteArray()).take(16).joinToString("") { "%02x".format(it) }

    actual fun read(key: String): ByteArray? = try {
        val f = File(dir() ?: return null, name(key))
        if (f.isFile) { f.setLastModified(System.currentTimeMillis()); f.readBytes() } else null      // reading refreshes it: used pictures stay
    } catch (_: Exception) { null }

    actual fun has(key: String): Boolean = try { File(dir() ?: return false, name(key)).isFile } catch (_: Exception) { false }

    actual fun write(key: String, bytes: ByteArray) {
        try {
            val d = dir() ?: return
            val tmp = File(d, name(key) + ".tmp")
            tmp.writeBytes(bytes)
            if (!tmp.renameTo(File(d, name(key)))) tmp.delete()
            if (++writes % 25 == 0) prune(d)
        } catch (_: Exception) { }
    }

    actual fun remove(key: String) {
        try { File(dir() ?: return, name(key)).delete() } catch (_: Exception) { }
    }

    private fun prune(d: File) {
        val files = d.listFiles()?.filter { it.isFile } ?: return
        var total = files.sumOf { it.length() }
        if (total <= MAX_BYTES) return
        for (f in files.sortedBy { it.lastModified() }) {
            if (total <= MAX_BYTES * 8 / 10) break
            total -= f.length()
            f.delete()
        }
    }
}
