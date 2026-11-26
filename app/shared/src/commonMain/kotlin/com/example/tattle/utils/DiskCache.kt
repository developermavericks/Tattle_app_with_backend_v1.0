package com.example.tattle.utils

/** Picture files kept on the phone (so stories can be read, pictures included, without a connection). Android stores them; the other targets keep nothing. */
expect object DiskCache {
    fun read(key: String): ByteArray?
    fun has(key: String): Boolean
    fun write(key: String, bytes: ByteArray)
    fun remove(key: String)
}
