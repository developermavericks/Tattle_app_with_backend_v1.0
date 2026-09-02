package com.example.tattle.utils

import android.content.Intent
import com.example.tattle.auth.currentActivity
import com.example.tattle.models.Article

actual fun shareArticleContent(article: Article) {
    val activity = currentActivity ?: return
    val shareText = """
        📰 Tattle Exclusive Digest
        ━━━━━━━━━━━━━━━━━━━━━
        *${article.headline}*

        ${article.hook.ifBlank { article.brief }}

        📲 Read full stories, AI key takeaways, and why it matters exclusively on Tattle!
        👉 Sign up or Log in on Tattle to view: https://tattle.app/story/${article.id}
    """.trimIndent()

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Story via Tattle")
    activity.startActivity(shareIntent)
}
