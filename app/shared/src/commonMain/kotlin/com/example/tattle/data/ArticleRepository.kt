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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
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

@Serializable
data class FeedReq(
    @SerialName("seen_ids") val seenIds: List<String> = emptyList(),
    val persona: String = "normal",
    val limit: Int = 40
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

class ArticleRepository(private val client: HttpClient, private val settings: com.russhwolf.settings.Settings) {
    private val backendUrl = PlatformConfig.BASE_URL
    private val feedJson = Json { ignoreUnknownKeys = true; isLenient = true }

    /** The last feed this phone received for a tab (at most 20 stories, at most 12 hours old), or empty. */
    fun cachedFeed(tab: String): List<Article> = try {
        val at = settings.getLong("feed_cache_at_$tab", 0L)
        val raw = settings.getStringOrNull("feed_cache_$tab")
        if (raw == null || currentTimeMillis() - at > 12 * 3600_000L) emptyList()
        else feedJson.decodeFromString<List<Article>>(raw)
    } catch (_: Exception) { emptyList() }

    private fun storeFeed(tab: String, items: List<Article>) {
        try {
            if (items.isEmpty()) return
            settings.putString("feed_cache_$tab", feedJson.encodeToString(items.take(20)))
            settings.putLong("feed_cache_at_$tab", currentTimeMillis())
        } catch (_: Exception) { }
    }

    // ---- the story library: the stories this phone received recently, kept for reading without a connection
    @Volatile private var libraryMemo: List<Article>? = null
    private val libraryLock = Mutex()

    private fun loadLibrary(): List<Article> {
        libraryMemo?.let { return it }
        val stored = try {
            settings.getStringOrNull("story_library")?.let { feedJson.decodeFromString<List<Article>>(it) } ?: emptyList()
        } catch (_: Exception) { emptyList() }
        libraryMemo = stored
        return stored
    }

    /** The ids the person has already swiped past (set by the app): stories read once are never kept for offline reading again. */
    @Volatile private var seenHint: Set<String> = emptySet()
    fun setSeen(ids: Collection<String>) { seenHint = ids.toSet() }

    /** Remembers a batch the server just sent: unread stories only, newest first, at most 120. */
    private suspend fun keep(items: List<Article>) {
        val usable = items.filterNot { it.hook.contains("processed by AI", ignoreCase = true) }.distinctBy { it.id }
        if (usable.isEmpty()) return
        withContext(Dispatchers.Default) {
            libraryLock.withLock {
                val ids = usable.map { it.id }.toSet()
                val seen = seenHint
                val merged = (usable + loadLibrary().filter { it.id !in ids }).filter { it.id !in seen }.take(120)
                libraryMemo = merged
                try { settings.putString("story_library", feedJson.encodeToString(merged)) } catch (_: Exception) { }
            }
        }
    }

    private fun tidy(s: String) = s.lowercase().filter { it.isLetterOrDigit() }

    /** Stories to read without a connection: only ones not read yet and, when a sector is asked for, only that sector's - never
     * padded with unrelated stories just to fill the page. */
    fun offlineStories(sector: String? = null, seen: Collection<String> = emptyList(), max: Int = 40): List<Article> {
        val seenSet = seen.toSet()
        val all = loadLibrary().filter { it.id !in seenSet }
        if (all.isEmpty()) return emptyList()
        if (sector.isNullOrBlank() || sector == "for_you" || sector == "all") return all.take(max)
        return all.filter { tidy(it.category) == tidy(sector) }.take(max)
    }

    /** While there is a connection, keeps about `target` unread stories on the phone so a long tunnel or train ride does not run out. */
    suspend fun topUpLibrary(interests: List<String>, seen: Collection<String>, target: Int = 80) {
        if (_isOffline.value) return
        val exclude = HashSet<String>(seen)
        val held = loadLibrary()
        var unread = held.count { it.id !in exclude }
        exclude += held.map { it.id }
        val sectors = interests.ifEmpty { listOf("ai", "tech", "world news", "money and business", "sports") }
        var rounds = 0
        while (unread < target && rounds < 3) {
            rounds++
            var added = 0
            for (sector in sectors) {
                if (unread + added >= target) break
                val batch = getArticlesBySector(sector, exclude.toList())
                if (_isOffline.value) return                                       // the connection dropped: stop quietly
                val fresh = batch.filter { it.id !in exclude }
                exclude += fresh.map { it.id }
                added += fresh.size
            }
            if (added == 0) break
            unread += added
        }
    }

    // ---- saved stories (bookmarks): kept in full on the phone, so the Saved tab works with no connection and never loses a story to the library's size limit
    @Volatile private var savedMemo: Map<String, Article>? = null

    private fun loadSaved(): Map<String, Article> {
        savedMemo?.let { return it }
        val stored = try {
            settings.getStringOrNull("saved_stories")?.let { feedJson.decodeFromString<List<Article>>(it) }?.associateBy { it.id } ?: emptyMap()
        } catch (_: Exception) { emptyMap() }
        savedMemo = stored
        return stored
    }

    /** Called whenever the bookmarks change (and at start): stores the full story of every bookmark, drops the ones removed. */
    suspend fun pinBookmarks(ids: Collection<String>) {
        withContext(Dispatchers.Default) {
            libraryLock.withLock {
                val current = loadSaved()
                val next = LinkedHashMap<String, Article>()
                for (id in ids) {
                    val story = articleCache[id] ?: current[id] ?: loadLibrary().find { it.id == id }
                    if (story != null) next[id] = story
                }
                if (next.keys != current.keys) {
                    savedMemo = next
                    try { settings.putString("saved_stories", feedJson.encodeToString(next.values.toList())) } catch (_: Exception) { }
                }
            }
        }
    }

    private val curatedUrl = PlatformConfig.CURATED_API_URL
    private val articleCache = mutableMapOf<String, Article>()
    private val cacheMutex = Mutex()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    /** A failed server call alone never means the phone is offline (the server could just be restarting, slow, etc) - only the OS's own
     * connectivity check does. Keeps the "offline" banner from showing when the phone actually has a connection. */
    private fun markOffline() { if (!com.example.tattle.utils.isDeviceOnline()) _isOffline.value = true }

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
                markOffline()
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

    suspend fun getArticlesBySector(
        sector: String,
        seenIds: List<String> = emptyList(),
        genZ: Boolean = false
    ): List<Article> {
        if (sector == "all" || sector == "for_you") return getForYouArticles(emptyList())
        val encodedSector = sector.replace(" ", "%20")
        // The backend returns the sector's unseen newspaper (OCR) stories first, then the curated Nexus stories (every reader gets the same stories, whatever their age).
        val remote = withTimeoutOrNull(12000) {
            try {
                val res: List<Article> = client.post("$backendUrl/api/mobile/feed/$encodedSector") {
                    contentType(ContentType.Application.Json)
                    setBody(FeedReq(seenIds.takeLast(2000), if (genZ) "genz" else "normal"))
                }.body()
                res
            } catch (_: Exception) {
                null
            }
        }
        if (remote != null) {
            _isOffline.value = false
            cacheMutex.withLock { remote.forEach { articleCache[it.id] = it } }
            storeFeed(sector, remote)
            keep(remote)
            return remote                                   // keep the server's order
        }
        markOffline()
        val saved = offlineStories(sector, seenIds)         // no connection: the stories saved from earlier visits
        if (saved.isNotEmpty()) return saved
        return if (PlatformConfig.USE_MOCK_FALLBACK) MockData.getArticlesBySector(sector).sortProcessedFirst() else emptyList()
    }

    /** Stories that name the reader's city (the server knows it from the location they shared). Offline there is nothing local to show. */
    suspend fun getLocalArticles(seenIds: List<String>, genZ: Boolean = false): List<Article> {
        val remote = withTimeoutOrNull(10000) {
            try {
                client.get("$backendUrl/api/mobile/local") {
                    parameter("persona", if (genZ) "genz" else "normal")
                    parameter("limit", 30)
                }.body<List<Article>>()
            } catch (_: Exception) { null }
        }
        if (remote == null) { markOffline(); return emptyList() }
        _isOffline.value = false
        cacheMutex.withLock { remote.forEach { articleCache[it.id] = it } }
        keep(remote)
        return remote.filter { it.id !in seenIds }
    }

    suspend fun getLocalInfo(): LocalInfo? = try { client.get("$backendUrl/api/mobile/local/info").body<LocalInfo>() } catch (_: Exception) { null }

    suspend fun getForYouArticles(userSectors: List<String>, seenIds: List<String> = emptyList()): List<Article> = coroutineScope {
        // 1. Try Ktor Backend with 4.0s timeout
        val remoteForYou = withTimeoutOrNull(4000) {
            try {
                val response: List<Article> = client.get("$backendUrl/api/articles/for-you") {
                    if (userSectors.isNotEmpty()) parameter("interests", userSectors.joinToString(","))      // what the reader picked at sign-up: a starting lean for new readers
                }.body()
                if (response.isNotEmpty()) {
                    _isOffline.value = false
                    val sorted = response.sortProcessedFirst()
                    cacheMutex.withLock {
                        sorted.forEach { articleCache[it.id] = it }
                    }
                    storeFeed("for_you", sorted)
                    keep(sorted)
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
            async { getArticlesBySector(sector, seenIds) }
        }
        val aggregated = deferreds.awaitAll().flatten()
        val distinct = aggregated.distinctBy { it.id }.sortProcessedFirst()
        if (distinct.isNotEmpty()) {
            return@coroutineScope distinct
        }

        markOffline()
        offlineStories(null, seenIds).ifEmpty { if (PlatformConfig.USE_MOCK_FALLBACK) MockData.articles.sortProcessedFirst() else emptyList() }
    }

    suspend fun getTrendingArticles(): List<Article> = coroutineScope {
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
                }.body()
                if (response.isNotEmpty()) {
                    _isOffline.value = false
                    val sorted = response.sortProcessedFirst()
                    cacheMutex.withLock {
                        sorted.forEach { articleCache[it.id] = it }
                    }
                    keep(sorted)
                    sorted
                } else null
            } catch (_: Exception) {
                null
            }
        }

        if (remoteArticles != null) {
            return@coroutineScope remoteArticles
        }

        markOffline()
        offlineStories(null).sortedByDescending { it.views }.ifEmpty { if (PlatformConfig.USE_MOCK_FALLBACK) MockData.getTrendingArticles().sortProcessedFirst() else emptyList() }
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
                keep(sorted)
                return sorted
            }
        } catch (_: Exception) { }

        return getArticlesByIds(ids)
    }

    fun getArticlesByIds(ids: List<String>): List<Article> {
        val result = mutableListOf<Article>()
        for (id in ids) {
            val cached = articleCache[id] ?: loadSaved()[id] ?: loadLibrary().find { it.id == id } ?: MockData.articles.find { it.id == id }
            if (cached != null) {
                articleCache[id] = cached
                result.add(cached)
            }
        }
        return result.sortProcessedFirst()
    }

    fun getArticleById(id: String): Article? {
        val cached = articleCache[id] ?: loadSaved()[id] ?: loadLibrary().find { it.id == id } ?: MockData.articles.find { it.id == id }
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
