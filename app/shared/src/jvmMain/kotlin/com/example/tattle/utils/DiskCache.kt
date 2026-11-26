package com.example.tattle.utils

actual object DiskCache {
    actual fun read(key: String): ByteArray? = null
    actual fun has(key: String): Boolean = false
    actual fun write(key: String, bytes: ByteArray) {}
    actual fun remove(key: String) {}
}
