package com.example.tattle.data

import com.example.tattle.PlatformConfig
import com.example.tattle.models.Article
import com.example.tattle.models.CuratedArticle
import com.example.tattle.models.SectorResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class ArticleRepository(private val client: HttpClient) {
    private val baseUrl = PlatformConfig.CURATED_API_URL
    private val articleCache = mutableMapOf<String, Article>()

    suspend fun getSectors(): List<String> {
        return try {
            val response: SectorResponse = client.get("$baseUrl/api/sectors").body()
            response.sectors
        } catch (e: Exception) {
            println("TATTLE_DEBUG: Error fetching sectors: ${e.message}")
            emptyList()
        }
    }

    suspend fun getArticlesBySector(sector: String): List<Article> {
        return try {
            val encodedSector = sector.replace(" ", "%20")
            val url = "$baseUrl/api/mobile/articles/$encodedSector"
            println("TATTLE_DEBUG: Fetching articles from $url")
            
            val response: List<CuratedArticle> = client.get(url).body()
            println("TATTLE_DEBUG: Received ${response.size} articles for $sector")
            
            val mapped = response.map { curated ->
                mapToArticle(curated, sector)
            }
            
            mapped.forEach { articleCache[it.id] = it }
            mapped
        } catch (e: Exception) {
            println("TATTLE_DEBUG: Error fetching articles for $sector: ${e.message}")
            emptyList()
        }
    }

    suspend fun getForYouArticles(userSectors: List<String>): List<Article> {
        if (userSectors.isEmpty()) {
            return getArticlesBySector("ai")
        }
        val aggregated = mutableListOf<Article>()
        for (sector in userSectors.take(3)) {
            val fetched = getArticlesBySector(sector)
            aggregated.addAll(fetched)
        }
        return aggregated.distinctBy { it.id }.sortedBy { it.isBreaking }.reversed()
    }

    suspend fun getTrendingArticles(): List<Article> {
        val trendingSectors = listOf("ai", "tech", "world news", "money and business")
        val aggregated = mutableListOf<Article>()
        for (sector in trendingSectors) {
            val fetched = getArticlesBySector(sector)
            aggregated.addAll(fetched)
        }
        return aggregated.distinctBy { it.id }.sortedByDescending { it.views }
    }

    fun getArticlesByIds(ids: List<String>): List<Article> {
        val result = mutableListOf<Article>()
        for (id in ids) {
            val article = articleCache[id]
            if (article != null) {
                result.add(article)
            }
        }
        return result
    }

    fun getArticleById(id: String): Article? {
        return articleCache[id]
    }

    suspend fun pollArticleUpdate(sector: String, articleId: String): Article? {
        return try {
            val articles = getArticlesBySector(sector)
            articles.find { it.id == articleId && !it.hook.contains("processed by AI") }
        } catch (e: Exception) {
            null
        }
    }

    private fun mapToArticle(curated: CuratedArticle, sector: String): Article {
        val article = Article(
            id = curated.articleId,
            category = curated.sector.ifBlank { sector },
            readTime = "30s read",
            headline = curated.headline,
            hook = curated.summary ?: "Summary currently being processed by AI...",
            brief = curated.summary ?: "Summary currently being processed by AI...",
            bullets = curated.keyTakeaways.ifEmpty { listOf("Key insights are arriving soon...") },
            whyItMatters = curated.whyItMatters ?: "Our AI is currently analyzing the impact of this story.",
            fullText = curated.cleanedContent.ifBlank { "The full narrative is being prepared for your reading experience." },
            publisher = curated.source ?: "Tattle Reports",
            publishedAt = curated.publishedAt ?: "Just now",
            imageUrl = curated.imageUrl,
            likes = (500..2000).random(),
            commentsCount = (10..500).random(),
            isBreaking = curated.rank <= 5,
            views = (1000..10000).random()
        )
        
        articleCache[article.id] = article
        return article
    }
}
