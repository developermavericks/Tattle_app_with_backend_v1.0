package com.example.tattle.data

import com.example.tattle.PlatformConfig
import com.example.tattle.models.Article
import com.example.tattle.models.CuratedArticle
import com.example.tattle.models.SectorResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ArticleRepository(private val client: HttpClient) {
    private val baseUrl = PlatformConfig.CURATED_API_URL

    suspend fun getSectors(): List<String> {
        return try {
            val response: SectorResponse = client.get("$baseUrl/api/sectors").body()
            response.sectors
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getArticlesBySector(sector: String): List<Article> {
        return try {
            val encodedSector = sector.replace(" ", "%20")
            val url = "$baseUrl/api/mobile/articles/$encodedSector"
            
            val response: List<CuratedArticle> = client.get(url).body()
            
            response.map { curated ->
                mapToArticle(curated)
            }
        } catch (e: Exception) {
            println("TATTLE_DEBUG: Error fetching articles for $sector: ${e.message}")
            emptyList()
        }
    }

    suspend fun pollArticleUpdate(sector: String, articleId: String): Article? {
        return try {
            val articles = getArticlesBySector(sector)
            articles.find { it.id == articleId && !it.hook.contains("processed by AI") }
        } catch (e: Exception) {
            null
        }
    }

    private fun mapToArticle(curated: CuratedArticle): Article {
        return Article(
            id = curated.articleId,
            category = curated.sector,
            readTime = "30s read",
            headline = curated.headline,
            hook = curated.summary ?: "Summary currently being processed by AI...",
            brief = curated.summary ?: "Summary currently being processed by AI...",
            bullets = curated.keyTakeaways ?: listOf("Key insights are arriving soon..."),
            whyItMatters = curated.whyItMatters ?: "Our AI is currently analyzing the impact of this story.",
            fullText = curated.cleanedContent ?: "The full narrative is being prepared for your reading experience.",
            publisher = curated.source ?: "Tattle Reports",
            publishedAt = curated.publishedAt ?: "Just now",
            imageUrl = "",
            likes = (500..2000).random(),
            commentsCount = (10..500).random(),
            isBreaking = curated.rank <= 5,
            views = (1000..10000).random()
        )
    }
}
