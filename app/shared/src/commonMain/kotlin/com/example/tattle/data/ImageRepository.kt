package com.example.tattle.data

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap
import com.example.tattle.PlatformConfig
import com.example.tattle.models.Article
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class ImageRepository(private val client: HttpClient) {
    var envatoApiToken: String = "OP6NdHQ7vl9vb4TNaOqloZdaf0c9A2UF"
    private val backendUrl = PlatformConfig.BASE_URL
    private val pixabayApiKey = "57288715-79ef2e54282e42425a088bb2e"
    private val pixabayBaseUrl = "https://pixabay.com/api/"
    private val envatoBaseUrl = "https://api.envato.com/v1/discovery/search/search/item"

    // Single-Query Repository Cache: Keyed by article ID or Query
    private val imageCache = mutableMapOf<String, String>()
    private val bitmapCache = mutableMapOf<String, ImageBitmap>()
    private val cacheMutex = Mutex()

    // Reliable Unsplash Stock Photos mapped by Category
    private val categoryStockPhotos = mapOf(
        "AI & Robotics" to "https://images.unsplash.com/photo-1677442136019-21780efad99a?w=800&q=80",
        "Tech" to "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&q=80",
        "Pop Culture" to "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&q=80",
        "World News" to "https://images.unsplash.com/photo-1529107386315-e1a2ed48a620?w=800&q=80",
        "Science & Space" to "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80",
        "Business & Money" to "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=800&q=80",
        "Climate & Environment" to "https://images.unsplash.com/photo-1509391365360-2e959784a276?w=800&q=80",
        "Gaming" to "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=800&q=80",
        "Entertainment" to "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&q=80",
        "Startups" to "https://images.unsplash.com/photo-1559136555-9303baea8ebd?w=800&q=80",
        "Healthcare" to "https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=800&q=80",
        "Education" to "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800&q=80",
        "Lifestyle" to "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=800&q=80",
        "Sports" to "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=800&q=80",
        "Creator Economy" to "https://images.unsplash.com/photo-1533750516457-a7f992034fec?w=800&q=80",
        "Geopolitics" to "https://images.unsplash.com/photo-1541872703-74c5e44368f9?w=800&q=80"
    )

    fun sanitizeImageUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        var clean = url.trim()
        if (clean.startsWith("//")) {
            clean = "https:$clean"
        } else if (clean.startsWith("http://")) {
            clean = "https://" + clean.removePrefix("http://")
        }
        if (clean.contains("images.unsplash.com") && !clean.contains("?")) {
            clean = "$clean?w=800&auto=format&fit=crop&q=80"
        }
        return clean
    }

    /**
     * Fetches and decodes ImageBitmap directly using Compose Multiplatform native decoders.
     */
    suspend fun getOrFetchBitmapForArticle(article: Article): ImageBitmap? = withContext(NonCancellable) {
        // 1. Check bitmap cache
        cacheMutex.withLock {
            val cached = bitmapCache[article.id]
            if (cached != null) return@withContext cached
        }

        // 2. Resolve image URL
        val imageUrl = getOrFetchImageForArticle(article)
        if (imageUrl.isBlank()) return@withContext null

        // 3. Fetch raw image bytes and decode directly into Compose ImageBitmap
        var bitmap = fetchImageBitmap(imageUrl)
        if (bitmap == null) {
            val fallbackUrl = getCategoryFallbackPhoto(article.category)
            bitmap = fetchImageBitmap(fallbackUrl)
        }

        if (bitmap != null) {
            cacheMutex.withLock {
                bitmapCache[article.id] = bitmap
            }
        }

        return@withContext bitmap
    }

    private suspend fun fetchImageBitmap(url: String): ImageBitmap? {
        return try {
            val bytes: ByteArray = client.get(url).body()
            bytes.decodeToImageBitmap()
        } catch (e: Exception) {
            println("TATTLE_DEBUG: Direct bitmap fetch failed for '$url': ${e.message}")
            null
        }
    }

    /**
     * Resolves and caches the relevant image for an article EXACTLY ONCE.
     * Uses NonCancellable so composition scope changes will NEVER interrupt image fetching.
     */
    suspend fun getOrFetchImageForArticle(article: Article): String = withContext(NonCancellable) {
        // 1. Check in-memory image cache for this article ID
        cacheMutex.withLock {
            val cachedUrl = imageCache[article.id]
            if (!cachedUrl.isNullOrBlank()) {
                return@withContext cachedUrl
            }
        }

        // 2. Check if article already carries a valid non-placeholder URL
        if (article.imageUrl.isNotBlank() && !article.imageUrl.contains("picsum")) {
            val sanitized = sanitizeImageUrl(article.imageUrl)!!
            cacheMutex.withLock {
                imageCache[article.id] = sanitized
            }
            return@withContext sanitized
        }

        // 3. Build smart queries (1-2 core keywords -> Category fallback)
        val queries = buildSearchQueries(article)
        var resolvedUrl: String? = null

        for (query in queries) {
            resolvedUrl = searchImage(query)
            if (resolvedUrl != null) break
        }

        val finalUrl = sanitizeImageUrl(resolvedUrl)
            ?: getCategoryFallbackPhoto(article.category)

        // 4. Cache resolved image URL permanently for this article ID
        cacheMutex.withLock {
            imageCache[article.id] = finalUrl
        }

        return@withContext finalUrl
    }

    fun getCachedImage(articleId: String): String? {
        return imageCache[articleId]
    }

    fun getCategoryFallbackPhoto(category: String): String {
        return categoryStockPhotos[category]
            ?: categoryStockPhotos.values.find { category.contains("ai", ignoreCase = true) || category.contains("tech", ignoreCase = true) }
            ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80"
    }

    private fun buildSearchQueries(article: Article): List<String> {
        val title = article.headline.lowercase()

        // Smart topic keyword extraction based on article headline subject matter
        val topicQuery = when {
            title.contains("semiconductor") || title.contains("chip") || title.contains("wafer") -> "semiconductor microchip"
            title.contains("quantum") || title.contains("cryptograph") || title.contains("encryption") -> "cybersecurity quantum code"
            title.contains("central bank") || title.contains("cross-border") || title.contains("settlement") || title.contains("remittance") -> "financial banking trading"
            title.contains("lunar") || title.contains("moon") || title.contains("regolith") -> "moon space habitat"
            title.contains("water") || title.contains("river") || title.contains("aquifer") -> "clean water river ocean"
            title.contains("perovskite") || title.contains("solar") || title.contains("photovoltaic") -> "solar panel clean energy"
            title.contains("pacific") || title.contains("maritime") || title.contains("shipping") || title.contains("vessel") -> "cargo shipping port ocean"
            title.contains("audio") || title.contains("acoustic") || title.contains("headset") || title.contains("sound") -> "gaming headphones spatial audio"
            title.contains("flu") || title.contains("vaccine") || title.contains("mrna") || title.contains("virus") -> "medical vaccine laboratory"
            title.contains("zero-knowledge") || title.contains("passport") || title.contains("identity") -> "cybersecurity digital identity"
            title.contains("circadian") || title.contains("lighting") || title.contains("urban") || title.contains("led") -> "city lighting street night"
            title.contains("biometric") || title.contains("football") || title.contains("athlete") || title.contains("sport") -> "stadium soccer athlete"
            title.contains("npu") || title.contains("smartphones") || title.contains("offline") -> "smartphone artificial intelligence"
            title.contains("sodium") || title.contains("battery") || title.contains("ev") || title.contains("electric vehicle") -> "electric vehicle battery"
            title.contains("truck") || title.contains("freight") || title.contains("driverless") || title.contains("highway") -> "semi truck interstate highway"
            title.contains("james webb") || title.contains("telescope") || title.contains("exoplanet") -> "deep space telescope planet"
            title.contains("gpu") || title.contains("exaflop") || title.contains("supercomput") -> "data center server GPU"
            title.contains("tutor") || title.contains("student") || title.contains("school") || title.contains("education") -> "student education classroom"
            title.contains("holographic") || title.contains("orchestra") || title.contains("arena") || title.contains("concert") -> "concert stage performance"
            title.contains("micropayment") || title.contains("creator") || title.contains("monetiz") -> "digital content creator"
            title.contains("microplastic") || title.contains("enzyme") || title.contains("ocean") -> "ocean plastic ecology"
            title.contains("code") || title.contains("developer") || title.contains("software") || title.contains("bug") -> "software developer coding"
            title.contains("photonics") || title.contains("laser") || title.contains("copper") -> "fiber optics technology"
            title.contains("maglev") || title.contains("rail") || title.contains("train") -> "high speed train railway"
            title.contains("carbon") || title.contains("offset") || title.contains("biomass") -> "forest nature climate"
            title.contains("asteroid") || title.contains("mining") || title.contains("spacecraft") -> "space asteroid mining"
            title.contains("air capture") || title.contains("co2") || title.contains("geothermal") -> "clean energy atmosphere"
            title.contains("crispr") || title.contains("gene") || title.contains("blindness") || title.contains("retinal") -> "genetics medical research"
            title.contains("blackout") || title.contains("grid") || title.contains("power") -> "city blackout electricity"
            title.contains("work week") || title.contains("burnout") || title.contains("workplace") -> "modern office working"
            else -> null
        }

        if (topicQuery != null) {
            return listOf(topicQuery, article.category)
        }

        val stopWords = setOf(
            "global", "hits", "faces", "massive", "silent", "rise", "underground",
            "scene", "surprising", "critical", "next", "enter", "trial", "trials", "faces"
        )

        val headlineWords = article.headline
            .split(" ", "-", ":", "—")
            .map { it.lowercase().trim() }
            .filter { it.length > 3 && it !in stopWords }

        val primaryKey = headlineWords.take(2).joinToString(" ")
        val secondaryKey = headlineWords.firstOrNull() ?: article.category

        return listOfNotNull(
            primaryKey.ifBlank { null },
            secondaryKey.ifBlank { null },
            article.category
        ).distinct()
    }

    suspend fun searchImage(query: String): String? = withContext(NonCancellable) {
        if (query.isBlank()) return@withContext null
        val cleanQuery = query.take(100)

        // Check query cache
        cacheMutex.withLock {
            val cached = imageCache["query:$cleanQuery"]
            if (cached != null) return@withContext cached
        }

        // 1. Try Direct Envato Discovery Search Endpoint FIRST
        if (envatoApiToken.isNotBlank()) {
            val envatoUrl = searchEnvatoDirect(cleanQuery)
            if (envatoUrl != null) {
                cacheMutex.withLock { imageCache["query:$cleanQuery"] = envatoUrl }
                return@withContext envatoUrl
            }
        }

        // 2. Try Backend Proxy endpoint
        try {
            val responseText: String = client.get("$backendUrl/api/images/search") {
                parameter("term", cleanQuery)
            }.body()

            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            val root = json.parseToJsonElement(responseText).jsonObject
            val url = root["imageUrl"]?.jsonPrimitive?.contentOrNull
            if (!url.isNullOrBlank()) {
                val sanitized = sanitizeImageUrl(url)!!
                cacheMutex.withLock { imageCache["query:$cleanQuery"] = sanitized }
                return@withContext sanitized
            }
        } catch (_: Exception) { }

        // 3. Fallback to Pixabay Search
        val pixabayUrl = searchPixabayImage(cleanQuery)
        if (pixabayUrl != null) {
            cacheMutex.withLock { imageCache["query:$cleanQuery"] = pixabayUrl }
        }
        return@withContext pixabayUrl
    }

    suspend fun searchEnvatoDirect(query: String): String? = withContext(NonCancellable) {
        if (query.isBlank()) return@withContext null
        return@withContext try {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }

            // Attempt 1: Envato Market wide search (Photodune + Envato Elements / Market)
            val textWide: String = client.get(envatoBaseUrl) {
                parameter("term", query)
                if (envatoApiToken.isNotBlank()) {
                    header("Authorization", "Bearer $envatoApiToken")
                }
                header("User-Agent", "TattleApp/1.0 (Android; Mobile)")
            }.body()

            val urlWide = extractUrlFromJsonResponse(json, textWide)
            if (!urlWide.isNullOrBlank()) return@withContext urlWide

            // Attempt 2: Photodune stock photography site search
            val textPhotodune: String = client.get(envatoBaseUrl) {
                parameter("site", "photodune")
                parameter("term", query)
                if (envatoApiToken.isNotBlank()) {
                    header("Authorization", "Bearer $envatoApiToken")
                }
                header("User-Agent", "TattleApp/1.0 (Android; Mobile)")
            }.body()

            extractUrlFromJsonResponse(json, textPhotodune)
        } catch (e: Exception) {
            println("TATTLE_DEBUG: Envato direct image search failed for '$query': ${e.message}")
            null
        }
    }

    private fun extractUrlFromJsonResponse(json: Json, jsonText: String): String? {
        return try {
            val rootObj = json.parseToJsonElement(jsonText).jsonObject
            val matches = rootObj["matches"]?.jsonArray
                ?: rootObj["items"]?.jsonArray
                ?: rootObj["results"]?.jsonArray
                ?: return null

            for (matchElement in matches) {
                val matchObj = matchElement.jsonObject
                val previewsObj = matchObj["previews"]?.jsonObject

                val rawUrl = if (previewsObj != null) {
                    previewsObj["landscape_preview"]?.jsonObject?.get("landscape_url")?.jsonPrimitive?.contentOrNull
                        ?: previewsObj["landscape_preview"]?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                        ?: previewsObj["icon_with_landscape_preview"]?.jsonObject?.get("landscape_url")?.jsonPrimitive?.contentOrNull
                        ?: previewsObj["icon_with_square_preview"]?.jsonObject?.get("icon_url")?.jsonPrimitive?.contentOrNull
                        ?: previewsObj["live_site"]?.jsonObject?.get("icon_url")?.jsonPrimitive?.contentOrNull
                        ?: previewsObj["live_site"]?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                } else {
                    matchObj["image_url"]?.jsonPrimitive?.contentOrNull
                        ?: matchObj["preview_url"]?.jsonPrimitive?.contentOrNull
                        ?: matchObj["thumbnail_url"]?.jsonPrimitive?.contentOrNull
                }

                val sanitized = sanitizeImageUrl(rawUrl)
                if (!sanitized.isNullOrBlank()) return sanitized
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun searchPixabayImage(query: String): String? = withContext(NonCancellable) {
        if (query.isBlank()) return@withContext null
        return@withContext try {
            val responseText: String = client.get(pixabayBaseUrl) {
                parameter("key", pixabayApiKey)
                parameter("q", query)
                parameter("image_type", "photo")
                parameter("per_page", 3)
                parameter("safesearch", "true")
            }.body()
            
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            val rootObj = json.parseToJsonElement(responseText).jsonObject
            val hits = rootObj["hits"]?.jsonArray
            val firstHit = hits?.firstOrNull()?.jsonObject
            val rawUrl = firstHit?.get("webformatURL")?.jsonPrimitive?.contentOrNull
                ?: firstHit?.get("largeImageURL")?.jsonPrimitive?.contentOrNull

            sanitizeImageUrl(rawUrl)
        } catch (e: Exception) {
            println("TATTLE_DEBUG: Pixabay search failed for '$query': ${e.message}")
            null
        }
    }
}
