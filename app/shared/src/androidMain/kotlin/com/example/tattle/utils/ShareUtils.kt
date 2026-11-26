package com.example.tattle.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.tattle.PlatformConfig
import com.example.tattle.auth.currentActivity
import com.example.tattle.data.ImageRepository
import com.example.tattle.data.UserSync
import com.example.tattle.models.Article
import com.example.tattle.ui.components.cardSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import java.io.File
import java.io.FileOutputStream

/** True for addresses that only work inside one network (home Wi-Fi, the emulator): a link like that is useless to the person receiving it. */
internal fun isPrivateAddress(url: String): Boolean {
    val host = url.substringAfter("://").substringBefore("/").substringBefore(":").lowercase()
    return host == "localhost" || host.startsWith("127.") || host.startsWith("10.") || host.startsWith("192.168.") ||
        Regex("""172\.(1[6-9]|2[0-9]|3[01])\..*""").matches(host) || !host.contains('.')
}

/** The link to put in a shared story: the server's public address, or null when the server is only reachable on this Wi-Fi. */
internal fun shareLink(article: Article): String? {
    if (article.shareUrl.isNotBlank()) return article.shareUrl
    val base = PlatformConfig.BASE_URL.trimEnd('/')
    return if (isPrivateAddress(base)) null else "$base/story/${article.id}"
}

/** What the person receiving the share reads: bold headline, the summary, the source and (when there is one) a link that opens. */
internal fun shareText(article: Article): String = buildString {
    append('*').append(article.headline.trim()).append("*\n\n")
    append(cardSummary(article.brief.ifBlank { article.hook }, 320)).append("\n\n")          // whole sentences, not the shortened card hook
    append("📰 ").append(article.publisher)
    if (article.publishedAt.isNotBlank()) append(" · ").append(article.publishedAt)
    shareLink(article)?.let { append("\n🔗 ").append(it) }
    append("\n\n⚡ Shared from Tattle")
}

/** The story's picture as a JPEG in the app's cache (WhatsApp treats a WebP as a sticker), shared through the file provider. Null when it is not on the phone. */
private fun prepareShareImage(context: Context, article: Article): Uri? = try {
    val images = GlobalContext.get().get<ImageRepository>()
    val address = images.sanitizeImageUrl(article.imageUrl.takeIf { it.isNotBlank() })
    val bytes = address?.let { DiskCache.read(it) }
    val bitmap = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    if (bitmap == null) null else {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        dir.listFiles()?.filter { System.currentTimeMillis() - it.lastModified() > 3600_000L }?.forEach { it.delete() }
        val file = File(dir, "tattle_${article.id.hashCode().toUInt()}.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        FileProvider.getUriForFile(context, "${context.packageName}.share", file)
    }
} catch (_: Exception) { null }

actual fun shareArticleContent(article: Article, platform: String) {
    val activity = currentActivity ?: return
    try { GlobalContext.get().get<UserSync>().recordShare(article.id, platform.lowercase()) } catch (_: Exception) { }
    val text = shareText(article)

    CoroutineScope(Dispatchers.Default).launch {
        val image = prepareShareImage(activity, article)
        withContext(Dispatchers.Main) {
            fun intent(packageName: String?) = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_SUBJECT, article.headline)
                if (image != null) {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, image)
                    clipData = ClipData.newRawUri("", image)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                }
                packageName?.let { setPackage(it) }
            }
            val target = when (platform.lowercase()) {
                "whatsapp" -> "com.whatsapp"
                "telegram" -> "org.telegram.messenger"
                "discord" -> "com.discord"
                "facebook" -> "com.facebook.katana"
                "twitter", "x" -> "com.twitter.android"
                else -> null
            }
            try {
                activity.startActivity(if (target != null) intent(target) else Intent.createChooser(intent(null), "Share Tattle story"))
            } catch (_: Exception) {
                try { activity.startActivity(Intent.createChooser(intent(null), "Share Tattle story")) } catch (_: Exception) { }      // the app is not installed: choose another
            }
        }
    }
}
