package com.example.tattle.data

import com.example.tattle.PlatformConfig
import com.example.tattle.models.UserPreferences
import com.example.tattle.utils.ApproxLocation
import com.example.tattle.utils.currentTimeMillis
import com.example.tattle.utils.deviceDetails
import com.example.tattle.utils.fetchApproxLocation
import com.example.tattle.utils.locationPermissionGranted
import com.example.tattle.utils.notificationsAllowed
import com.example.tattle.utils.requestNotificationPermission
import kotlinx.coroutines.flow.MutableSharedFlow
import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.TextContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonElement
import com.example.tattle.utils.Breadcrumbs
import com.example.tattle.utils.deletePendingCrash
import com.example.tattle.utils.locationFailureReason
import com.example.tattle.utils.readPendingCrashes
import com.example.tattle.utils.showLocalNotification
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

// Guest sign-in has been removed from the application; every reader signs in with phone, e-mail or Google.
// @Serializable data class GuestReq(@SerialName("device_id") val deviceId: String)
@Serializable data class TokenRes(val success: Boolean = false, val token: String? = null, val message: String? = null)
@Serializable data class EventDto(val articleId: String, val type: String, val sector: String? = null, val dwellMs: Int = 0, val source: String? = null)
@Serializable data class EventsReq(val events: List<EventDto>)
@Serializable data class DeviceReq(val model: String, val manufacturer: String, val os: String, val appVersion: String, val notificationsAllowed: Boolean? = null)
@Serializable data class LocationReq(val consent: Boolean, val lat: Double? = null, val lon: Double? = null, val city: String? = null, val region: String? = null, val country: String? = null, val status: String? = null)

@Serializable
data class RecapSectorDto(val slug: String = "", val name: String = "", val icon: String = "", val count: Int = 0, val percentage: Double = 0.0)

@Serializable
data class RecapDto(
    @SerialName("cards_read") val cardsRead: Int = 0,
    val minutes: Int = 0,
    @SerialName("streak_days") val streakDays: Int = 0,
    @SerialName("total_cards") val totalCards: Int = 0,
    val reads: Int = 0, val likes: Int = 0, val bookmarks: Int = 0, val shares: Int = 0,
    @SerialName("surveys_answered") val surveysAnswered: Int = 0,
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
    private val queueJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    /** The events not yet delivered (reads made with no connection) survive the app being closed: they are stored and sent at the next chance. */
    private fun persistQueue() {
        try { settings.putString("tattle_event_queue", queueJson.encodeToString(queue.takeLast(500))) } catch (_: Exception) { }
    }

    private fun restoreQueue() {
        try {
            settings.getStringOrNull("tattle_event_queue")?.let { queue.addAll(queueJson.decodeFromString<List<EventDto>>(it)) }
        } catch (_: Exception) { }
    }
    private var flushJob: Job? = null
    private var lastTick = currentTimeMillis()
    private var firstCard = true
    private val settings: Settings by lazy { Settings() }

    /** Fires when the server no longer knows this account (deleted by an admin, or the login expired): the app then returns to login + onboarding. */
    val sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private suspend fun handleUnauthorized() {
        if (AuthSession.token == null) return                         // nothing to lose: a guest session is created on demand
        AuthSession.clear()
        lock.withLock { queue.clear(); persistQueue() }               // those events belonged to the account that is gone
        settings.remove("tattle_location_choice"); settings.remove("tattle_device_sent"); settings.remove("tattle_location_sent")
        sessionExpired.emit(Unit)
    }

    /**
     * Asks the server whether this phone's login is still valid (an admin may have deleted the account). Also repairs a profile whose date of birth
     * never reached the server (the app has it, the server does not).
     */
    suspend fun validateSession() {
        if (AuthSession.token == null) return
        try {
            val resp = client.get("$baseUrl/api/user/profile")
            if (resp.status == HttpStatusCode.Unauthorized) { handleUnauthorized(); return }
            if (resp.status.isSuccess()) {
                val server: UserProfileData = resp.body()
                val local = preferences.userPreferences.value
                _needsUsername.value = local.isOnboarded && server.username.isNullOrBlank()          // people from before usernames existed are asked once
                if (!server.username.isNullOrBlank() && local.username != server.username) preferences.updatePreferences(local.copy(username = server.username, name = server.name ?: local.name))
                if (server.dob.isNullOrBlank() && local.dob.isNotBlank()) {
                    postProfile(UpdateProfileData(dob = local.dob, age = local.ageGroup.toIntOrNull(), notificationsEnabled = local.notifications.dailyBriefings))
                }
            }
        } catch (_: Exception) { }
    }

    private val _needsUsername = MutableStateFlow(false)
    val needsUsername: StateFlow<Boolean> = _needsUsername

    fun usernameDone() { _needsUsername.value = false }

    private suspend fun postProfile(body: UpdateProfileData): Boolean = try {
        val resp = client.post("$baseUrl/api/user/profile/update") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        resp.status.isSuccess()
    } catch (_: Exception) { false }

    /** Sends the profile from onboarding. Runs in the app's own scope: the onboarding screen is gone a moment later and would cancel its own request. */
    fun saveProfileInBackground(dob: String, age: Int?, notificationsEnabled: Boolean) {
        scope.launch {
            repeat(4) { attempt ->
                ensureSession()
                if (postProfile(UpdateProfileData(dob = dob, age = age, notificationsEnabled = notificationsEnabled))) return@launch
                delay(2_000L * (attempt + 1))
            }
        }
    }

    // ---- time in the app: a beat every 30 s while the app is in the foreground
    private var beatJob: Job? = null
    private var lastBeat = 0L

    fun appForeground() {
        lastBeat = currentTimeMillis()
        beatJob?.cancel()
        beatJob = scope.launch {
            var tick = 0
            while (true) {
                delay(30_000)
                beat()
                if (bookmarksDirty) syncBookmarks()                  // a bookmark made without a connection reaches the server once there is one, whichever screen is open
                if (++tick % 2 == 0) checkInbox()                    // notifications from the dashboard: about once a minute while the app is open
            }
        }
        scope.launch {
            articles.setSeen(preferences.userPreferences.value.seenIds)
            articles.pinBookmarks(preferences.userPreferences.value.bookmarks)
            uploadCrashes(); reportDeviceAndLocationIfDue(); checkInbox()
            if (queue.isEmpty()) lock.withLock { if (queue.isEmpty()) restoreQueue() }          // events left over from a session that ended offline
            if (queue.isNotEmpty()) scheduleFlush()
            syncBookmarks()
            retryPushToken()
        }
    }

    fun appBackground() {
        beatJob?.cancel()
        scope.launch { beat(); flushNow() }
    }

    private suspend fun beat() {
        val now = currentTimeMillis()
        val ms = (now - lastBeat).coerceIn(0, 120_000).toInt()
        lastBeat = now
        if (ms > 0) lock.withLock { queue += EventDto("_session", "session", null, ms, null) }
        scheduleFlush()
    }

    // ---- device and approximate location (location only after the user said yes)
    var locationChoice: String?
        get() = settings.getStringOrNull("tattle_location_choice")          // "yes", "no", or null = never asked
        set(value) { if (value == null) settings.remove("tattle_location_choice") else settings.putString("tattle_location_choice", value) }

    private suspend fun reportDeviceAndLocationIfDue() {
        ensureSession()
        val now = currentTimeMillis()
        val allowedNow = notificationsAllowed()
        // also whenever the phone's notification permission changed: someone who just switched it on must not wait 12 hours
        val permissionChanged = settings.getStringOrNull("tattle_notif_allowed_sent") != allowedNow.toString()
        if (permissionChanged || now - settings.getLong("tattle_device_sent", 0L) > 12 * 3600_000L) {
            val d = deviceDetails()
            try {
                val resp = client.post("$baseUrl/api/user/device") {
                    contentType(ContentType.Application.Json)
                    setBody(DeviceReq(d.model, d.manufacturer, d.os, d.appVersion, allowedNow))
                }
                if (resp.status == HttpStatusCode.Unauthorized) { handleUnauthorized(); return }
                if (resp.status.isSuccess()) {
                    settings.putLong("tattle_device_sent", now)
                    settings.putString("tattle_notif_allowed_sent", allowedNow.toString())
                }
            } catch (_: Exception) { }
        }
        if (locationChoice == "yes" && now - settings.getLong("tattle_location_sent", 0L) > 6 * 3600_000L) pushLocation(fetchApproxLocation())
    }

    private suspend fun pushLocation(loc: ApproxLocation?): Boolean {
        return try {
            val resp = client.post("$baseUrl/api/user/location") {
                contentType(ContentType.Application.Json)
                // no fix yet (slow GPS, indoors): the consent is still recorded, the position follows at the next start
                setBody(if (loc == null) LocationReq(true, status = locationFailureReason()) else LocationReq(true, loc.lat, loc.lon, loc.city, loc.region, loc.country))
            }
            if (resp.status.isSuccess() && loc != null) {
                settings.putLong("tattle_location_sent", currentTimeMillis())
                loc.city?.let { settings.putString("tattle_location_city", it) }
            }
            resp.status.isSuccess()
        } catch (_: Exception) { false }
    }

    /** The user agreed to share an approximate location: ask the OS, read it once, send it. False when the OS permission was refused. */
    suspend fun enableLocation(): Boolean {
        ensureSession()
        val loc = fetchApproxLocation()
        if (loc == null && !locationPermissionGranted()) { locationChoice = "no"; return false }      // refused in the system dialog
        locationChoice = "yes"
        pushLocation(loc)
        return true
    }

    /** The answer from the onboarding screen. Runs in the app's own scope so it survives the screen being closed. */
    fun chooseLocation(on: Boolean) {
        locationChoice = if (on) "yes" else "no"            // answered: the feed must not ask again while the system dialog is still open
        scope.launch { if (on) enableLocation() else disableLocation() }
    }

    /** The notification answer from onboarding: ask Android for the permission when they said yes, then report the system state too. */
    fun chooseNotifications(on: Boolean) {
        scope.launch {
            if (on) requestNotificationPermission()
            settings.remove("tattle_device_sent")                  // report the phone's notification setting right away
            reportDeviceAndLocationIfDue()
        }
    }

    /** The user switched sharing off: the server erases the stored location. */
    suspend fun disableLocation() {
        locationChoice = "no"
        settings.remove("tattle_location_sent")
        settings.remove("tattle_location_city")
        try {
            client.post("$baseUrl/api/user/location") {
                contentType(ContentType.Application.Json)
                setBody(LocationReq(false))
            }
        } catch (_: Exception) { }
    }

    /** Guest accounts no longer exist: every reader signs in (phone, e-mail or Google). Kept so older callers compile; there is nothing to create. */
    @Suppress("RedundantSuspendModifier")
    suspend fun ensureSession() { }

    /** Deletes the account on the server (its data is erased; the server keeps an archive record). True only when the server confirmed it. */
    suspend fun deleteAccount(): Boolean = try {
        val resp = client.delete("$baseUrl/api/user/me")
        resp.status.isSuccess() || resp.status == HttpStatusCode.Unauthorized         // 401 = the account is already gone
    } catch (_: Exception) { false }

    /** The city shown in Settings (the server keeps the exact position; the app only ever shows the city). */
    val locationCity: String?
        get() = settings.getStringOrNull("tattle_location_city")?.takeIf { it.isNotBlank() }

    /** After a login: pull this account's bookmarks so the same user sees them on any phone. The server's list wins. */
    suspend fun pullBookmarks() {
        ensureSession()
        retryPushToken()
        if (bookmarksDirty) { syncBookmarks(); return }          // changes made on this phone (offline, perhaps) are sent first, never overwritten by the server's older list
        try {
            val resp = client.get("$baseUrl/api/user/bookmarks")
            if (resp.status == HttpStatusCode.Unauthorized) { handleUnauthorized(); return }
            val ids: List<String> = resp.body()
            val cur = preferences.userPreferences.value
            if (cur.bookmarks != ids) preferences.updatePreferences(cur.copy(bookmarks = ids))   // straight to storage: not a user action, nothing to report back
        } catch (_: Exception) { }
    }

    private val bookmarkLock = Mutex()
    /** The phone's bookmarks changed and the server has not confirmed yet (kept across restarts: a bookmark made offline must survive closing the app). */
    private var bookmarksDirty: Boolean
        get() = settings.getBoolean("tattle_bookmarks_dirty", false)
        set(value) { settings.putBoolean("tattle_bookmarks_dirty", value) }

    /**
     * Brings the server's bookmarks level with the phone's (the phone is the truth). A bookmark made while offline is sent when the connection is back;
     * the server only offers "toggle", so the two lists are compared first and only the differences are toggled, once.
     */
    suspend fun syncBookmarks() {
        bookmarkLock.withLock {
            try {
                ensureSession()
                val resp = client.get("$baseUrl/api/user/bookmarks")
                if (resp.status == HttpStatusCode.Unauthorized) { handleUnauthorized(); return }
                if (!resp.status.isSuccess()) return
                val server: List<String> = resp.body()
                val local = preferences.userPreferences.value.bookmarks
                var failed = false
                (local.toSet() - server.toSet()).forEach { if (articles.toggleBookmarkOnServer("me", it) == null) failed = true }
                (server.toSet() - local.toSet()).forEach { if (articles.toggleBookmarkOnServer("me", it) == null) failed = true }
                bookmarksDirty = failed
            } catch (_: Exception) { bookmarksDirty = true }                // no connection: tried again with the next heartbeat (every 30 s while the app is open)
        }
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

        if (new.seenIds != old.seenIds) articles.setSeen(new.seenIds)
        if (new.bookmarks != old.bookmarks) {
            bookmarksDirty = true
            scope.launch { articles.pinBookmarks(new.bookmarks) }           // the full stories are kept on the phone (the Saved tab works offline)
            scope.launch { syncBookmarks() }                                // and the server is brought level (now, or at the next connection)
        }
        new.reactions.forEach { (id, r) -> if (old.reactions[id] != r) scope.launch { articles.toggleLikeOnServer("me", id) } }
    }

    fun recordShare(articleId: String, platform: String) {
        scope.launch { articles.recordShareOnServer("me", articleId, platform) }
    }

    private fun track(articleId: String, type: String, dwellMs: Int) {
        val sector = articles.getArticleById(articleId)?.category
        val source = if (articleId.startsWith("ocr_")) "ocr" else "nexus"
        scope.launch {
            lock.withLock { queue += EventDto(articleId, type, sector, dwellMs, source); persistQueue() }
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
        val batch = lock.withLock { queue.take(150).also { repeat(it.size) { queue.removeAt(0) }; persistQueue() } }
        if (batch.isEmpty()) return
        ensureSession()
        var delivered = false
        try {
            val resp = client.post("$baseUrl/api/user/events") {
                contentType(ContentType.Application.Json)
                setBody(EventsReq(batch))
            }
            if (resp.status == HttpStatusCode.Unauthorized) { handleUnauthorized(); return }      // account gone or login expired: back to login + onboarding
            delivered = resp.status.isSuccess()
        } catch (_: Exception) { }
        if (!delivered) lock.withLock { queue.addAll(0, batch.takeLast(300)); persistQueue() }        // retry with the next batch
        else if (lock.withLock { queue.isNotEmpty() }) scheduleFlush()
    }

    /** Today's numbers for the daily recap (null when offline or not logged in). Pending events are sent first so the recap includes the last swipes. */
    suspend fun fetchRecap(): RecapDto? {
        flushNow()
        ensureSession()
        return try {
            val resp = client.get("$baseUrl/api/user/recap")
            if (resp.status == HttpStatusCode.Unauthorized) { handleUnauthorized(); null }
            else if (resp.status.isSuccess()) resp.body() else null
        } catch (_: Exception) { null }
    }

    // ---- username chosen in onboarding
    suspend fun suggestUsername(name: String): UsernameSuggestRes? = try {
        ensureSession()
        client.get("$baseUrl/api/user/username/suggest") { parameter("name", name) }.body()
    } catch (_: Exception) { null }

    suspend fun checkUsername(username: String): UsernameCheckRes? = try {
        ensureSession()
        client.get("$baseUrl/api/user/username/check") { parameter("u", username) }.body()
    } catch (_: Exception) { null }

    suspend fun claimUsername(username: String, name: String): UsernameClaimRes? = try {
        ensureSession()
        client.post("$baseUrl/api/user/username") {
            contentType(ContentType.Application.Json)
            setBody(UsernameClaimReq(username, name))
        }.body()
    } catch (_: Exception) { null }

    // ---- notifications sent from the dashboard (people who chose "notifications on" in onboarding)
    suspend fun checkInbox(): List<InboxItem> {
        if (AuthSession.token == null) return emptyList()
        return try {
            val resp = client.get("$baseUrl/api/user/inbox")
            if (resp.status == HttpStatusCode.Unauthorized) { handleUnauthorized(); return emptyList() }
            if (!resp.status.isSuccess()) return emptyList()
            val items = resp.body<InboxRes>().notifications
            items.forEach { showLocalNotification(it.id, it.title, it.body, it.articleId, it.sentAt) }
            items
        } catch (_: Exception) { emptyList() }
    }

    fun notificationOpened(id: Int) {
        scope.launch { try { client.post("$baseUrl/api/user/inbox/$id/opened") } catch (_: Exception) { } }
    }

    // ---- Firebase push: the phone's token goes to the server so notifications can be pushed (they are also fetched by polling, as a fallback)
    fun registerPushToken(token: String) {
        settings.putString("tattle_fcm_token", token)
        scope.launch { sendPushToken(token) }
    }

    private suspend fun sendPushToken(token: String) {
        try {
            ensureSession()
            val resp = client.post("$baseUrl/api/user/push-token") {
                contentType(ContentType.Application.Json)
                setBody(PushTokenReq(token))
            }
            if (resp.status.isSuccess()) settings.putString("tattle_fcm_token_sent", token + "|" + (AuthSession.token ?: ""))     // sent FOR this login
        } catch (_: Exception) { }                                      // tried again at the next start
    }

    /** At every start: a token that changed, or that the server never confirmed (no connection then), is sent now. */
    private suspend fun retryPushToken() {
        val token = settings.getStringOrNull("tattle_fcm_token") ?: return
        if (AuthSession.token == null) return
        // the server keeps one owner per phone token: a different account signing in on this phone must send it again
        if (settings.getStringOrNull("tattle_fcm_token_sent") != token + "|" + AuthSession.token) sendPushToken(token)
    }

    // ---- surveys and ads switched on in the dashboard
    suspend fun fetchCampaigns(): List<CampaignDto> {
        ensureSession()
        return try {
            val resp = client.get("$baseUrl/api/user/campaigns")
            if (resp.status == HttpStatusCode.Unauthorized) { handleUnauthorized(); return emptyList() }
            if (resp.status.isSuccess()) resp.body<CampaignList>().campaigns else emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun campaignEvent(id: Int, event: String) {
        scope.launch {
            try {
                client.post("$baseUrl/api/user/campaigns/$id/event") {
                    contentType(ContentType.Application.Json)
                    setBody(CampaignEventReq(event))
                }
            } catch (_: Exception) { }
        }
    }

    /** problem = null means saved; otherwise the reason to show the person. Poll questions come back with everyone's votes. */
    suspend fun submitSurvey(id: Int, answers: Map<String, JsonElement>): SurveyOutcome = try {
        val res: SimpleOk = client.post("$baseUrl/api/user/campaigns/$id/respond") {
            contentType(ContentType.Application.Json)
            setBody(SurveyAnswersReq(answers))
        }.body()
        if (res.success || res.message?.contains("already", ignoreCase = true) == true) SurveyOutcome(null, res.results)
        else SurveyOutcome(res.message ?: "Could not save your answers.")
    } catch (_: Exception) { SurveyOutcome("No connection. Please try again.") }

    // ---- crash reports
    /** Uploads the crashes saved by the crash handler (at the next start, so a crash before login is still reported). */
    suspend fun uploadCrashes() {
        for (c in readPendingCrashes()) {
            try {
                val resp = client.post("$baseUrl/api/user/crash") { setBody(TextContent(c.json, ContentType.Application.Json)) }
                if (resp.status.isSuccess() || resp.status.value in 400..499) deletePendingCrash(c.id)       // a report the server will never accept is dropped
            } catch (_: Exception) { return }                                                              // offline: try again next start
        }
    }

    fun note(what: String) = Breadcrumbs.add(what)

    suspend fun logout() {
        settings.getStringOrNull("tattle_fcm_token")?.let { token ->           // a logged-out phone must not keep getting this account's notifications
            try { client.delete("$baseUrl/api/user/push-token") { contentType(ContentType.Application.Json); setBody(PushTokenReq(token)) } } catch (_: Exception) { }
        }
        settings.remove("tattle_fcm_token_sent")
        try { client.post("$baseUrl/api/auth/logout") } catch (_: Exception) { }
        AuthSession.clear()
    }
}
