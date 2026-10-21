package com.example.tattle.data

import com.example.tattle.PlatformConfig
import com.example.tattle.models.UserPreferences
import com.example.tattle.utils.currentTimeMillis
import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

/** The login token of this install. Every request carries it (see the HttpClient in Koin.kt) so the server knows which user is asking. */
object AuthSession {
    private const val TOKEN_KEY = "tattle_auth_token"
    private val settings: Settings by lazy { Settings() }

    var token: String? = null
        get() = field ?: settings.getStringOrNull(TOKEN_KEY)?.takeIf { it.isNotBlank() }.also { field = it }
        private set

    fun save(value: String) { token = value; settings.putString(TOKEN_KEY, value) }
    fun clear() { token = null; settings.remove(TOKEN_KEY) }

    fun deviceId(): String {
        val existing = settings.getStringOrNull("tattle_device_id")
        if (!existing.isNullOrBlank()) return existing
        val fresh = "dev-" + Random.nextLong().toULong().toString(16) + Random.nextLong().toULong().toString(16)
        settings.putString("tattle_device_id", fresh)
        return fresh
    }
}

@Serializable data class GuestReq(@SerialName("device_id") val deviceId: String)
@Serializable data class TokenRes(val success: Boolean = false, val token: String? = null, val message: String? = null)
@Serializable data class EventDto(val articleId: String, val type: String, val sector: String? = null, val dwellMs: Int = 0, val source: String? = null)
@Serializable data class EventsReq(val events: List<EventDto>)

@Serializable
data class RecapSectorDto(val slug: String = "", val name: String = "", val icon: String = "", val count: Int = 0, val percentage: Double = 0.0)

@Serializable
data class RecapDto(
    @SerialName("cards_read") val cardsRead: Int = 0,
    val minutes: Int = 0,
    @SerialName("streak_days") val streakDays: Int = 0,
    @SerialName("total_cards") val totalCards: Int = 0,
    val reads: Int = 0, val likes: Int = 0, val bookmarks: Int = 0, val shares: Int = 0,
    @SerialName("top_interest") val topInterest: String? = null,
    @SerialName("top_percent") val topPercent: Int? = null,
    val sectors: List<RecapSectorDto> = emptyList(),
    val summary: String = ""
)

/**
 * Everything that ties the app to the user's account on the server: the guest/login token, the activity events the recommender and the daily recap
 * are built from (what was viewed, for how long, what was read), bookmarks/likes/shares, and the recap itself. Works silently: if the server is
 * unreachable the app keeps working and events are retried with the next batch.
 */
class UserSync(
    private val client: HttpClient,
    private val preferences: PreferencesRepository,
    private val articles: ArticleRepository
) {
    private val baseUrl = PlatformConfig.BASE_URL
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Mutex()
    private val queue = mutableListOf<EventDto>()
    private var flushJob: Job? = null
    private var lastTick = currentTimeMillis()
    private var firstCard = true

    /** A guest account for this install when nobody has logged in yet (reading, bookmarks, recommendations and the recap all work as a guest). */
    suspend fun ensureSession() {
        if (AuthSession.token != null) return
        try {
            val res: TokenRes = client.post("$baseUrl/api/auth/guest") {
                contentType(ContentType.Application.Json)
                setBody(GuestReq(AuthSession.deviceId()))
            }.body()
            if (res.success && res.token != null) AuthSession.save(res.token)
        } catch (_: Exception) { }
    }

    /** After a login: pull this account's bookmarks so the same user sees them on any phone. The server's list wins. */
    suspend fun pullBookmarks() {
        ensureSession()
        try {
            val resp = client.get("$baseUrl/api/user/bookmarks")
            if (resp.status == HttpStatusCode.Unauthorized) { AuthSession.clear(); return }
            val ids: List<String> = resp.body()
            val cur = preferences.userPreferences.value
            if (cur.bookmarks != ids) preferences.updatePreferences(cur.copy(bookmarks = ids))   // straight to storage: not a user action, nothing to report back
        } catch (_: Exception) { }
    }

    /** Called with every preferences change the user makes; turns the differences into server calls. */
    fun onPreferencesChanged(old: UserPreferences?, new: UserPreferences) {
        if (old == null) return
        val now = currentTimeMillis()
        var dwell = (now - lastTick).coerceIn(0, 120_000).toInt()
        if (firstCard && (old.seenIds != new.seenIds || old.readHistory != new.readHistory)) { dwell = dwell.coerceAtMost(15_000); firstCard = false }   // time before the first swipe includes launch/onboarding

        val newlySeen = new.seenIds.filter { it !in old.seenIds.toSet() }
        val newlyRead = new.readHistory.filter { it !in old.readHistory.toSet() }
        if (newlySeen.isNotEmpty() || newlyRead.isNotEmpty()) lastTick = now
        newlySeen.forEach { track(it, "view", dwell) }
        newlyRead.forEach { track(it, "read", dwell) }

        (new.bookmarks.toSet() - old.bookmarks.toSet()).forEach { id -> scope.launch { articles.toggleBookmarkOnServer("me", id) } }
        (old.bookmarks.toSet() - new.bookmarks.toSet()).forEach { id -> scope.launch { articles.toggleBookmarkOnServer("me", id) } }
        new.reactions.forEach { (id, r) -> if (old.reactions[id] != r) scope.launch { articles.toggleLikeOnServer("me", id) } }
    }

    fun recordShare(articleId: String, platform: String) {
        scope.launch { articles.recordShareOnServer("me", articleId, platform) }
    }

    private fun track(articleId: String, type: String, dwellMs: Int) {
        val sector = articles.getArticleById(articleId)?.category
        val source = if (articleId.startsWith("ocr_")) "ocr" else "nexus"
        scope.launch {
            lock.withLock { queue += EventDto(articleId, type, sector, dwellMs, source) }
            scheduleFlush()
        }
    }

    private fun scheduleFlush() {
        if (flushJob?.isActive == true) return
        flushJob = scope.launch {
            delay(2_500)
            flushNow()
        }
    }

    suspend fun flushNow() {
        val batch = lock.withLock { queue.take(150).also { repeat(it.size) { queue.removeAt(0) } } }
        if (batch.isEmpty()) return
        ensureSession()
        var delivered = false
        try {
            val resp = client.post("$baseUrl/api/user/events") {
                contentType(ContentType.Application.Json)
                setBody(EventsReq(batch))
            }
            if (resp.status == HttpStatusCode.Unauthorized) AuthSession.clear()      // token revoked or expired: a new guest token is issued next time
            delivered = resp.status.isSuccess()
        } catch (_: Exception) { }
        if (!delivered) lock.withLock { queue.addAll(0, batch.takeLast(300)) }        // retry with the next batch
        else if (lock.withLock { queue.isNotEmpty() }) scheduleFlush()
    }

    /** Today's numbers for the daily recap (null when offline or not logged in). Pending events are sent first so the recap includes the last swipes. */
    suspend fun fetchRecap(): RecapDto? {
        flushNow()
        ensureSession()
        return try {
            val resp = client.get("$baseUrl/api/user/recap")
            if (resp.status.isSuccess()) resp.body() else null
        } catch (_: Exception) { null }
    }

    suspend fun logout() {
        try { client.post("$baseUrl/api/auth/logout") } catch (_: Exception) { }
        AuthSession.clear()
    }
}
