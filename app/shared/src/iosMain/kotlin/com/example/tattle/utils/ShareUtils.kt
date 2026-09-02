package com.example.tattle.utils

import com.example.tattle.models.Article

actual fun shareArticleContent(article: Article) {
    println("Share on iOS: ${article.headline}")
}
