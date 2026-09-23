package com.example.tattle.utils

import android.content.Intent
import com.example.tattle.auth.currentActivity
import com.example.tattle.models.Article

actual fun shareArticleContent(article: Article, platform: String) {
    val activity = currentActivity ?: return
    val cardText = """
┌──────────────────────────────┐
│  ⚡ TATTLE DIGEST
├──────────────────────────────┤
│  *${article.headline}*
│
│  ${article.hook.ifBlank { article.brief }}
│
│  📰 ${article.publisher} • ${article.publishedAt}
├──────────────────────────────┤
│  👉 View Card: https://tattle.app/story/${article.id}
└──────────────────────────────┘
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, cardText)
        type = "text/plain"

        when (platform.lowercase()) {
            "whatsapp" -> setPackage("com.whatsapp")
            "telegram" -> setPackage("org.telegram.messenger")
            "discord" -> setPackage("com.discord")
            "facebook" -> setPackage("com.facebook.katana")
            "twitter", "x" -> setPackage("com.twitter.android")
        }
    }

    try {
        activity.startActivity(sendIntent)
    } catch (_: Exception) {
        val fallbackIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, cardText)
            type = "text/plain"
        }
        activity.startActivity(Intent.createChooser(fallbackIntent, "Share Tattle Card"))
    }
}
