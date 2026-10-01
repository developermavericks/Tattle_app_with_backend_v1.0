package com.example.tattle.data

import com.example.tattle.PlatformConfig
import com.example.tattle.models.Article
import com.example.tattle.models.CuratedArticle
import com.example.tattle.models.SectorResponse
import com.example.tattle.utils.currentTimeMillis
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

@Serializable
data class ToggleBookmarkReq(val userIdentifier: String, val articleId: String)

@Serializable
data class ToggleBookmarkRes(val success: Boolean, val isBookmarked: Boolean, val bookmarks: List<String>)

@Serializable
data class ToggleLikeReq(val userIdentifier: String, val articleId: String)

@Serializable
data class ToggleLikeRes(val success: Boolean, val isLiked: Boolean, val likesCount: Int)

@Serializable
data class RecordShareReq(val userIdentifier: String, val articleId: String, val platform: String)

@Serializable
data class BatchArticlesReq(val ids: List<String>)

@Serializable
data class DirectMlFeedRes(
    val sector: String? = null,
    val count: Int = 0,
    val articles: List<DirectMlArticleDto> = emptyList()
)

@Serializable
data class DirectMlArticleDto(
    val id: Int? = null,
    val article_id: String? = null,
    val headline: String? = null,
    val brief: String? = null,
    val bullets_json: String? = null,
    val why_it_matters: String? = null,
    val full_text: String? = null,
    val image_url: String? = null,
    val age_bracket: String? = null,
    val sector: String? = null,
    val score: Float? = null,
    val published_at: String? = null
)

fun List<Article>.sortProcessedFirst(): List<Article> {
    return this.sortedWith(
        compareBy<Article> { article ->
            if (article.hook.contains("processed by AI", ignoreCase = true) ||
                article.brief.contains("processed by AI", ignoreCase = true)) 1 else 0
        }.thenByDescending { article ->
            article.isBreaking
        }.thenByDescending { article ->
            article.views
        }
    )
}

class ArticleRepository(private val client: HttpClient) {
    private val backendUrl = PlatformConfig.BASE_URL
    private val curatedUrl = PlatformConfig.CURATED_API_URL
    private val articleCache = mutableMapOf<String, Article>()
    private val cacheMutex = Mutex()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    suspend fun getSectors(): List<String> {
        return try {
            val response: SectorResponse = client.get("$backendUrl/api/sectors").body()
            if (response.sectors.isNotEmpty()) {
                _isOffline.value = false
                response.sectors
            } else {
                getDefaultSectors()
            }
        } catch (e: Exception) {
            try {
                val response: SectorResponse = client.get("$curatedUrl/api/sectors").body()
                if (response.sectors.isNotEmpty()) {
                    _isOffline.value = false
                    response.sectors
                } else {
                    getDefaultSectors()
                }
            } catch (_: Exception) {
                _isOffline.value = true
                getDefaultSectors()
            }
        }
    }

    private fun getDefaultSectors(): List<String> {
        return listOf(
            "ai", "tech", "world news", "money and business", "science and space",
            "climate and environment", "creator economy", "education", "gaming",
            "geopolitics", "healthcare", "lifestyle", "media and entertainment",
            "pop culture", "sports", "startups"
        )
    }

    suspend fun getArticlesBySector(sector: String, userAge: Int = 18): List<Article> {
        val encodedSector = sector.replace(" ", "%20")
        
        // 1. Try Ktor Gateway Backend on Port 8081
        val backendArticles = withTimeoutOrNull(4000) {
            try {
                val response: List<Article> = client.get("$backendUrl/api/mobile/articles/$encodedSector") {
                    parameter("user_age", userAge)
                }.body()
                if (response.isNotEmpty()) response else null
            } catch (_: Exception) {
                null
            }
        }

        if (backendArticles != null) {
            _isOffline.value = false
            val sorted = backendArticles.sortProcessedFirst()
            cacheMutex.withLock {
                sorted.forEach { articleCache[it.id] = it }
            }
            return sorted
        }

        // 2. Direct ML Backend Fallback on Port 8000 if Gateway 8081 is offline
        val directMlArticles = withTimeoutOrNull(4000) {
            try {
                val querySector = if (encodedSector == "all" || encodedSector == "for_you") "tech" else encodedSector
                val mlPortUrl = if (backendUrl.contains(":8081")) backendUrl.replace(":8081", ":8000") else "http://127.0.0.1:8000"
                val mlRes: DirectMlFeedRes = client.get("$mlPortUrl/api/feed/$querySector") {
                    parameter("user_age", userAge)
                }.body()
                if (mlRes.articles.isNotEmpty()) {
                    mlRes.articles.map { ml ->
                        val bullets: List<String> = try {
                            if (!ml.bullets_json.isNullOrBlank()) Json.decodeFromString(ml.bullets_json) else listOf("Key insight point 1.", "Key insight point 2.")
                        } catch (_: Exception) { listOf("Key insight point 1.", "Key insight point 2.") }
                        
                        Article(
                            id = "ml_" + (ml.article_id ?: ml.id?.toString() ?: currentTimeMillis().toString()),
                            category = ml.sector ?: "tech",
                            readTime = "30s read",
                            headline = ml.headline ?: "Breaking Headline",
                            hook = ml.brief ?: "Brief summary.",
                            brief = ml.brief ?: "Brief summary.",
                            bullets = bullets,
                            whyItMatters = ml.why_it_matters ?: "Impact analysis.",
                            fullText = ml.full_text ?: "Full narrative text.",
                            publisher = "Tattle ML Wire",
                            publishedAt = ml.published_at?.take(10) ?: "Today",
                            imageUrl = ml.image_url ?: "https://images.unsplash.com/photo-1518770660439-4636190af475",
                            likes = 1200,
                            commentsCount = 88,
                            isBreaking = false,
                            views = 8500
                        )
                    }
                } else null
            } catch (_: Exception) {
                null
            }
        }

        if (directMlArticles != null) {
            _isOffline.value = false
            val sorted = directMlArticles.sortProcessedFirst()
            cacheMutex.withLock {
                sorted.forEach { articleCache[it.id] = it }
            }
            return sorted
        }

        // 3. Fallback to Mock Data when both backends are offline
        _isOffline.value = true
        val mockArticles = MockData.getArticlesBySector(sector).sortProcessedFirst()
        cacheMutex.withLock {
            mockArticles.forEach { articleCache[it.id] = it }
        }
        return mockArticles
    }

    suspend fun getForYouArticles(userSectors: List<String>, userAge: Int = 18): List<Article> = coroutineScope {
        // 1. Try Ktor Backend with 4.0s timeout
        val remoteForYou = withTimeoutOrNull(4000) {
            try {
                val response: List<Article> = client.get("$backendUrl/api/articles/for-you") {
                    parameter("user_age", userAge)
                }.body()
                if (response.isNotEmpty()) {
                    _isOffline.value = false
                    val sorted = response.sortProcessedFirst()
                    cacheMutex.withLock {
                        sorted.forEach { articleCache[it.id] = it }
                    }
                    sorted
                } else null
            } catch (_: Exception) {
                null
            }
        }

        if (remoteForYou != null) {
            return@coroutineScope remoteForYou
        }

        val sectorsToFetch = if (userSectors.isEmpty()) listOf("ai", "tech", "world news", "money and business") else userSectors
        val deferreds = sectorsToFetch.map { sector ->
            async { getArticlesBySector(sector, userAge) }
        }
        val aggregated = deferreds.awaitAll().flatten()
        val distinct = aggregated.distinctBy { it.id }.sortProcessedFirst()
        if (distinct.isNotEmpty()) {
            return@coroutineScope distinct
        }

        _isOffline.value = true
        val mockArticles = MockData.articles.sortProcessedFirst()
        cacheMutex.withLock {
            mockArticles.forEach { articleCache[it.id] = it }
        }
        mockArticles
    }

    suspend fun getTrendingArticles(userAge: Int = 18): List<Article> = coroutineScope {
        // 1. Instant RAM Cache Check (0ms latency)
        cacheMutex.withLock {
            val cachedTrending = articleCache.values.filter { it.views > 2000 || it.isBreaking }.sortProcessedFirst()
            if (cachedTrending.size >= 5) {
                return@coroutineScope cachedTrending
            }
        }

        // 2. Fast 4.0s Network Fetch from Backend
        val remoteArticles = withTimeoutOrNull(4000) {
            try {
                val response: List<Article> = client.get("$backendUrl/api/articles/trending") {
                    parameter("user_age", userAge)
                }.body()
                if (response.isNotEmpty()) {
                    _isOffline.value = false
                    val sorted = response.sortProcessedFirst()
                    cacheMutex.withLock {
                        sorted.forEach { articleCache[it.id] = it }
                    }
                    sorted
                } else null
            } catch (_: Exception) {
                null
            }
        }

        if (remoteArticles != null) {
            return@coroutineScope remoteArticles
        }

        // 3. Instant Fallback to Mock Trending Data (0ms latency)
        _isOffline.value = true
        val mockTrending = MockData.getTrendingArticles().sortProcessedFirst()
        cacheMutex.withLock {
            mockTrending.forEach { articleCache[it.id] = it }
        }
        mockTrending
    }

    suspend fun getArticlesByIdsFromBackend(ids: List<String>): List<Article> {
        if (ids.isEmpty()) return emptyList()
        
        try {
            val response: List<Article> = client.post("$backendUrl/api/articles/batch") {
                contentType(ContentType.Application.Json)
                setBody(BatchArticlesReq(ids))
            }.body()

            if (response.isNotEmpty()) {
                _isOffline.value = false
                val sorted = response.sortProcessedFirst()
                cacheMutex.withLock {
                    sorted.forEach { articleCache[it.id] = it }
                }
                return sorted
            }
        } catch (_: Exception) { }

        return getArticlesByIds(ids)
    }

    fun getArticlesByIds(ids: List<String>): List<Article> {
        val result = mutableListOf<Article>()
        for (id in ids) {
            val cached = articleCache[id] ?: MockData.articles.find { it.id == id }
            if (cached != null) {
                articleCache[id] = cached
                result.add(cached)
            }
        }
        return result.sortProcessedFirst()
    }

    fun getArticleById(id: String): Article? {
        val cached = articleCache[id] ?: MockData.articles.find { it.id == id }
        if (cached != null) {
            articleCache[id] = cached
        }
        return cached
    }

    // --- Backend User Interaction Handlers (Save/Bookmark, Like, Share) ---
    suspend fun toggleBookmarkOnServer(userIdentifier: String, articleId: String): List<String>? {
        return try {
            val response: ToggleBookmarkRes = client.post("$backendUrl/api/user/bookmarks/toggle") {
                contentType(ContentType.Application.Json)
                setBody(ToggleBookmarkReq(userIdentifier, articleId))
            }.body()
            response.bookmarks
        } catch (e: Exception) {
            println("TATTLE_DEBUG: Error toggling bookmark: ${e.message}")
            null
        }
    }

    suspend fun fetchBookmarksFromServer(userIdentifier: String): List<String> {
        return try {
            val response: List<String> = client.get("$backendUrl/api/user/bookmarks") {
                parameter("userIdentifier", userIdentifier)
            }.body()
            response
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun toggleLikeOnServer(userIdentifier: String, articleId: String): Int? {
        return try {
            val response: ToggleLikeRes = client.post("$backendUrl/api/user/likes/toggle") {
                contentType(ContentType.Application.Json)
                setBody(ToggleLikeReq(userIdentifier, articleId))
            }.body()
            response.likesCount
        } catch (e: Exception) {
            println("TATTLE_DEBUG: Error toggling like: ${e.message}")
            null
        }
    }

    suspend fun recordShareOnServer(userIdentifier: String, articleId: String, platform: String = "generic") {
        try {
            client.post("$backendUrl/api/user/shares/record") {
                contentType(ContentType.Application.Json)
                setBody(RecordShareReq(userIdentifier, articleId, platform))
            }
        } catch (e: Exception) {
            println("TATTLE_DEBUG: Error recording share: ${e.message}")
        }
    }

    suspend fun pollArticleUpdate(sector: String, articleId: String): Article? {
        return try {
            val articles = getArticlesBySector(sector)
            articles.find { it.id == articleId && !it.hook.contains("processed by AI", ignoreCase = true) }
        } catch (e: Exception) {
            null
        }
    }

    private fun mapToArticle(curated: CuratedArticle, sector: String): Article {
        val seededRandom = Random(curated.articleId.hashCode())
        return Article(
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
            likes = seededRandom.nextInt(500, 2000),
            commentsCount = seededRandom.nextInt(10, 500),
            isBreaking = curated.rank <= 5,
            views = seededRandom.nextInt(1000, 10000)
        )
    }
}
