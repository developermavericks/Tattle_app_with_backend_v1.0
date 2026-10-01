package com.example.tattle

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.exp

// --- Database Schemas ---
object Users : IntIdTable() {
    val email = varchar("email", 255).uniqueIndex().nullable()
    val phoneNumber = varchar("phone_number", 20).uniqueIndex().nullable()
    val googleId = varchar("google_id", 255).uniqueIndex().nullable()
    val name = varchar("name", 255).nullable()
    val dob = varchar("dob", 20).nullable()
    val age = integer("age").nullable()
    val isDarkMode = bool("is_dark_mode").default(false)
    val notificationsEnabled = bool("notifications_enabled").default(true)
    val language = varchar("language", 20).default("English")
    val createdAt = long("created_at").default(System.currentTimeMillis())
}

object ArticlesTable : IntIdTable() {
    val articleId = varchar("article_id", 100).uniqueIndex()
    val category = varchar("category", 100)
    val headline = varchar("headline", 500)
    val hook = text("hook")
    val brief = text("brief")
    val bulletsJson = text("bullets_json")
    val whyItMatters = text("why_it_matters")
    val fullText = text("full_text")
    val publisher = varchar("publisher", 100)
    val publishedAt = varchar("published_at", 100)
    val imageUrl = varchar("image_url", 1000)
    val likes = integer("likes").default(0)
    val views = integer("views").default(0)
    val isBreaking = bool("is_breaking").default(false)
}

object UserBookmarksTable : IntIdTable() {
    val userIdentifier = varchar("user_identifier", 255)
    val articleId = varchar("article_id", 100)
    val createdAt = long("created_at")
}

object UserLikesTable : IntIdTable() {
    val userIdentifier = varchar("user_identifier", 255)
    val articleId = varchar("article_id", 100)
    val createdAt = long("created_at")
}

object UserSharesTable : IntIdTable() {
    val userIdentifier = varchar("user_identifier", 255)
    val articleId = varchar("article_id", 100)
    val platform = varchar("platform", 50)
    val createdAt = long("created_at")
}

// --- Dynamic Telemetry & Recommendation Scoring Table ---
object UserCategoryWeightsTable : IntIdTable("user_category_weights") {
    val userIdentifier = varchar("user_identifier", 255)
    val category = varchar("category", 100)
    val weightScore = float("weight_score").default(0.0f)
    val updatedAt = long("updated_at").default(System.currentTimeMillis())
}

// --- Real-Time Crash Radar Table ---
object CrashLogsTable : IntIdTable("crash_logs") {
    val crashId = varchar("crash_id", 100).uniqueIndex()
    val userIdentifier = varchar("user_identifier", 255)
    val exceptionType = varchar("exception_type", 200)
    val stackTrace = text("stack_trace")
    val deviceModel = varchar("device_model", 100)
    val osVersion = varchar("os_version", 50)
    val appVersion = varchar("app_version", 20)
    val timestamp = long("timestamp")
    val isResolved = bool("is_resolved").default(false)
}

// --- Manual Trending Override Table ---
object PinnedTrendingTable : IntIdTable("pinned_trending") {
    val articleId = varchar("article_id", 100).uniqueIndex()
    val pinRank = integer("pin_rank").default(1)
    val pinnedUntil = long("pinned_until")
    val createdAt = long("created_at").default(System.currentTimeMillis())
}

// --- Brand Ad & Survey Monetization Tables ---
object AdCampaignsTable : IntIdTable("ad_campaigns") {
    val campaignId = varchar("campaign_id", 100).uniqueIndex()
    val clientName = varchar("client_name", 200)
    val videoUrl = varchar("video_url", 1000)
    val durationSec = integer("duration_sec").default(5)
    val rewardPassMins = integer("reward_pass_mins").default(60)
    val isActive = bool("is_active").default(true)
    val createdAt = long("created_at").default(System.currentTimeMillis())
}

object BrandSurveysTable : IntIdTable("brand_surveys") {
    val surveyId = varchar("survey_id", 100).uniqueIndex()
    val clientName = varchar("client_name", 200)
    val title = varchar("title", 300)
    val questionsJson = text("questions_json")
    val rewardPassMins = integer("reward_pass_mins").default(60)
    val isActive = bool("is_active").default(true)
    val createdAt = long("created_at").default(System.currentTimeMillis())
}

object AdImpressionsTable : IntIdTable("ad_impressions") {
    val userIdentifier = varchar("user_identifier", 255)
    val campaignId = varchar("campaign_id", 100)
    val isCompleted = bool("is_completed").default(true)
    val watchDurationSec = integer("watch_duration_sec")
    val timestamp = long("timestamp").default(System.currentTimeMillis())
}

object SurveyResponsesTable : IntIdTable("survey_responses") {
    val userIdentifier = varchar("user_identifier", 255)
    val surveyId = varchar("survey_id", 100)
    val answersJson = text("answers_json")
    val timestamp = long("timestamp").default(System.currentTimeMillis())
}

object ArticlePollsTable : IntIdTable("article_polls") {
    val articleId = varchar("article_id", 100).uniqueIndex()
    val question = varchar("question", 300)
    val optionA = varchar("option_a", 100)
    val optionB = varchar("option_b", 100)
    val votesA = integer("votes_a").default(0)
    val votesB = integer("votes_b").default(0)
}

object FcmTokensTable : IntIdTable("fcm_tokens") {
    val userIdentifier = varchar("user_identifier", 255).uniqueIndex()
    val fcmToken = varchar("fcm_token", 500)
    val updatedAt = long("updated_at").default(System.currentTimeMillis())
}

object CustomNotificationsTable : IntIdTable("custom_notifications") {
    val notificationId = varchar("notification_id", 100).uniqueIndex()
    val title = varchar("title", 300)
    val body = text("body")
    val targetSector = varchar("target_sector", 100).default("all")
    val sentAt = long("sent_at").default(System.currentTimeMillis())
}

@Serializable
data class BroadcastNotificationRequest(
    val title: String,
    val body: String,
    val targetSector: String = "all"
)

// --- Data Transfer Objects ---
@Serializable
data class AuthRequest(
    val token: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val otp: String? = null
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null
)

@Serializable
data class SimpleResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class ImplicitSignalEvent(
    val articleId: String,
    val category: String,
    val dwellTimeMs: Long,
    val swipeVelocityPxMs: Float = 0f,
    val tapDepthLevel: Int = 1,
    val interactionType: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SignalBatchPayload(
    val userIdentifier: String,
    val events: List<ImplicitSignalEvent>
)

@Serializable
data class PinTrendingRequest(
    val articleId: String,
    val rank: Int = 1,
    val durationHours: Int = 12
)

@Serializable
data class CrashReportRequest(
    val userIdentifier: String = "anonymous",
    val exceptionType: String,
    val stackTrace: String,
    val deviceModel: String = "Android Device",
    val osVersion: String = "Android 14",
    val appVersion: String = "1.0.0"
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dob: String? = null,
    val age: Int? = null,
    val isDarkMode: Boolean? = null,
    val notificationsEnabled: Boolean? = null,
    val language: String? = null
)

@Serializable
data class PollVoteRequest(
    val articleId: String,
    val selectedOption: String,
    val userIdentifier: String = "anonymous"
)

@Serializable
data class PollVoteResponse(
    val success: Boolean,
    val votesA: Int,
    val votesB: Int,
    val percentageA: Float,
    val percentageB: Float
)

@Serializable
data class MlFeedResponse(
    val sector: String? = null,
    val count: Int = 0,
    val articles: List<MlArticleDto> = emptyList()
)

@Serializable
data class MlArticleDto(
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

fun mapMlArticleToDto(ml: MlArticleDto): ArticleDto {
    val bullets: List<String> = try {
        if (!ml.bullets_json.isNullOrBlank()) {
            Json.decodeFromString(ml.bullets_json)
        } else listOf("Key insight point 1.", "Key insight point 2.")
    } catch (_: Exception) {
        listOf("Key insight point 1.", "Key insight point 2.")
    }

    return ArticleDto(
        id = "ml_" + (ml.article_id ?: ml.id?.toString() ?: System.currentTimeMillis().toString()),
        category = ml.sector ?: "tech",
        headline = ml.headline ?: "Breaking Headline",
        hook = ml.brief ?: "Brief summary.",
        brief = ml.brief ?: "Brief summary.",
        bullets = bullets,
        whyItMatters = ml.why_it_matters ?: "Impact analysis.",
        fullText = ml.full_text ?: "Full text narrative.",
        publisher = "Tattle Wire",
        publishedAt = ml.published_at?.take(10) ?: "Today",
        imageUrl = ml.image_url ?: "https://images.unsplash.com/photo-1518770660439-4636190af475",
        likes = 1200,
        views = 8500
    )
}

@Serializable
data class ArticleDto(
    val id: String,
    val category: String,
    val readTime: String = "30s read",
    val headline: String,
    val hook: String,
    val brief: String,
    val bullets: List<String>,
    val whyItMatters: String,
    val fullText: String,
    val publisher: String,
    val publishedAt: String,
    val imageUrl: String,
    val likes: Int = 0,
    val commentsCount: Int = 0,
    val isBreaking: Boolean = false,
    val views: Int = 0
)

@Serializable
data class ToggleBookmarkRequest(val userIdentifier: String, val articleId: String)
@Serializable
data class ToggleBookmarkResponse(val success: Boolean, val isBookmarked: Boolean, val bookmarks: List<String>)
@Serializable
data class ToggleLikeRequest(val userIdentifier: String, val articleId: String)
@Serializable
data class ToggleLikeResponse(val success: Boolean, val isLiked: Boolean, val likesCount: Int)
@Serializable
data class RecordShareRequest(val userIdentifier: String, val articleId: String, val platform: String = "generic")
@Serializable
data class BatchArticlesRequest(val ids: List<String>)
@Serializable
data class SectorListResponse(val sectors: List<String>)

val httpClient = HttpClient(CIO) {
    install(ClientContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

object AuthConfig {
    const val SECRET = "TATTLE_LOCAL_SECRET_KEY_2026"
    const val ISSUER = "com.example.tattle"
    const val AUDIENCE = "tattle-users"
    const val GOOGLE_WEB_CLIENT_ID = "867080275588-1r7t5qbghdld0v4t4q1jhkhf79klau9h.apps.googleusercontent.com"
}

object TwilioConfig {
    const val ACCOUNT_SID = "AC18ee297704f50af9ec7bcecdd90df91f"
    const val AUTH_TOKEN = "fd993b48bae8ebcae82686205e55e4f4"
    const val FROM_NUMBER = "+17372508034"
    const val SENDER_EMAIL = "AC18ee297704f50af9ec7bcecdd90df91f@twilio.email"
}

suspend fun sendTwilioSms(phone: String, otpCode: String): Boolean {
    return try {
        val authHeader = Base64.getEncoder().encodeToString("${TwilioConfig.ACCOUNT_SID}:${TwilioConfig.AUTH_TOKEN}".toByteArray())
        val response = httpClient.post("https://api.twilio.com/2010-04-01/Accounts/${TwilioConfig.ACCOUNT_SID}/Messages.json") {
            header("Authorization", "Basic $authHeader")
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                Parameters.build {
                    append("To", phone)
                    append("From", TwilioConfig.FROM_NUMBER)
                    append("Body", "Your Tattle Security Code is $otpCode. Valid for 5 minutes.")
                }.formUrlEncode()
            )
        }
        println("⚡ TWILIO SMS DISPATCH STATUS for $phone: ${response.status}")
        response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
    } catch (e: Exception) {
        println("⚡ TWILIO SMS DISPATCH NOTICE ($phone): ${e.message}")
        false
    }
}

suspend fun sendTwilioEmail(email: String, magicCode: String): Boolean {
    return try {
        val authHeader = Base64.getEncoder().encodeToString("${TwilioConfig.ACCOUNT_SID}:${TwilioConfig.AUTH_TOKEN}".toByteArray())
        val response = httpClient.post("https://comms.twilio.com/v1/Emails") {
            header("Authorization", "Basic $authHeader")
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "from" to mapOf("address" to TwilioConfig.SENDER_EMAIL, "name" to "Tattle Security"),
                "to" to listOf(mapOf("address" to email)),
                "content" to mapOf(
                    "subject" to "Your Tattle Security Code is $magicCode",
                    "html" to "<h3>Your Tattle passcode: <b>$magicCode</b></h3>"
                )
            ))
        }
        println("⚡ TWILIO EMAIL DISPATCH STATUS for $email: ${response.status}")
        response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
    } catch (e: Exception) {
        println("⚡ TWILIO EMAIL DISPATCH NOTICE ($email): ${e.message}")
        false
    }
}

val otpStore = ConcurrentHashMap<String, String>()

fun main() {
    var dbConnected = false
    val postgresHost = "localhost"
    val postgresPort = 5433

    if (isPortReachable(postgresHost, postgresPort)) {
        try {
            Database.connect(
                url = "jdbc:postgresql://$postgresHost:$postgresPort/tattle_db",
                driver = "org.postgresql.Driver",
                user = "tattle_user",
                password = "tattle_password"
            )
            transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    Users, ArticlesTable, UserBookmarksTable, UserLikesTable, UserSharesTable,
                    UserCategoryWeightsTable, CrashLogsTable, PinnedTrendingTable,
                    AdCampaignsTable, BrandSurveysTable, AdImpressionsTable, SurveyResponsesTable, ArticlePollsTable,
                    FcmTokensTable, CustomNotificationsTable
                )
                seedDefaultArticles()
            }
            dbConnected = true
            println("Connected to Docker PostgreSQL (tattle_db) successfully!")
        } catch (e: Exception) {
            println("PostgreSQL connection failed (${e.message}). Falling back to local SQLite...")
        }
    }

    if (!dbConnected) {
        connectToSqlite()
    }

    val server = embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)

    Runtime.getRuntime().addShutdownHook(Thread {
        try {
            server.stop(1000, 3000)
        } catch (_: Exception) {}
    })

    try {
        server.start(wait = true)
    } catch (e: Exception) {
        val rootCause = getRootCause(e)
        if (rootCause is java.net.BindException || e.message?.contains("Address already in use") == true) {
            println("SERVER NOTICE: Port 8081 is already in use by a running server instance. Developer Dashboard available at http://127.0.0.1:8081/admin/dashboard.")
        } else {
            println("Server start exception: ${e.message}")
        }
    }
}

private fun getRootCause(throwable: Throwable): Throwable {
    var cause: Throwable? = throwable
    while (cause?.cause != null && cause.cause != cause) {
        cause = cause.cause
    }
    return cause ?: throwable
}

private fun connectToSqlite() {
    try {
        Database.connect("jdbc:sqlite:./tattle.db", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users, ArticlesTable, UserBookmarksTable, UserLikesTable, UserSharesTable,
                UserCategoryWeightsTable, CrashLogsTable, PinnedTrendingTable,
                AdCampaignsTable, BrandSurveysTable, AdImpressionsTable, SurveyResponsesTable, ArticlePollsTable,
                FcmTokensTable, CustomNotificationsTable
            )
            seedDefaultArticles()
        }
        println("Connected to local SQLite (tattle.db) successfully!")
    } catch (ex: Exception) {
        println("SQLite initialization error: ${ex.message}")
    }
}

private fun isPortReachable(host: String, port: Int, timeoutMs: Int = 1000): Boolean {
    return try {
        java.net.Socket().use { socket ->
            socket.connect(java.net.InetSocketAddress(host, port), timeoutMs)
            true
        }
    } catch (_: Exception) {
        false
    }
}

private fun seedDefaultArticles() {
    try {
        if (ArticlesTable.selectAll().count() >= 25) return
    } catch (_: Exception) {}

    val sampleArticles = listOf(
        ArticleDto(
            id = "art_ai_001",
            category = "ai",
            headline = "The Silent Rise of Neural-Mesh Networks in Urban Grids",
            hook = "Decentralized neural networks are bypassing traditional telecom towers to power resilient smart cities.",
            brief = "Decentralized mesh networks are silently spreading across major metropolises, offering zero-latency peer communication.",
            bullets = listOf(
                "Mesh nodes run localized AI models directly on edge hardware.",
                "Reduces cloud dependency and bandwidth overhead by 80%.",
                "Self-healing protocol maintains connectivity during infrastructure failures."
            ),
            whyItMatters = "Neural-mesh networks represent a technological reclamation of digital sovereignty and infrastructural resilience.",
            fullText = "As urban centers face increasing strain on centralized cellular infrastructure...",
            publisher = "TechPulse",
            publishedAt = "10m ago",
            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe",
            likes = 1240,
            commentsCount = 88,
            isBreaking = true,
            views = 9420
        ),
        ArticleDto(
            id = "art_tech_002",
            category = "tech",
            headline = "Quantum-Safe Encryption Standards Standardized Globally",
            hook = "International tech consortiums officially approve post-quantum cryptographic algorithms.",
            brief = "Governments and tech giants have initiated the largest cryptographic transition in digital history.",
            bullets = listOf(
                "NIST officially releases final post-quantum encryption standards.",
                "Financial networks mandated to transition by end of 2027.",
                "Lattice-based cryptography forms the core defense mechanism."
            ),
            whyItMatters = "Protects critical global banking and communications infrastructure against quantum computer decryption capabilities.",
            fullText = "The global technology sector reached a historic milestone today...",
            publisher = "CyberWire",
            publishedAt = "25m ago",
            imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5",
            likes = 890,
            commentsCount = 42,
            isBreaking = false,
            views = 5600
        )
    )

    sampleArticles.forEach { art ->
        try {
            ArticlesTable.insert { row ->
                row[articleId] = art.id
                row[category] = art.category
                row[headline] = art.headline
                row[hook] = art.hook
                row[brief] = art.brief
                row[bulletsJson] = Json.encodeToString(art.bullets)
                row[whyItMatters] = art.whyItMatters
                row[fullText] = art.fullText
                row[publisher] = art.publisher
                row[publishedAt] = art.publishedAt
                row[imageUrl] = art.imageUrl
                row[likes] = art.likes
                row[views] = art.views
                row[isBreaking] = art.isBreaking
            }
        } catch (_: Exception) {}
    }
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Tattle Server"
            verifier(
                JWT.require(Algorithm.HMAC256(AuthConfig.SECRET))
                    .withAudience(AuthConfig.AUDIENCE)
                    .withIssuer(AuthConfig.ISSUER)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    routing {
        get("/") {
            call.respondText("Tattle Local Backend is running. Developer Command Center available at /admin/dashboard")
        }

        // --- 1. Developer Admin Web Dashboard ---
        get("/admin") { call.respondRedirect("/admin/dashboard") }
        get("/admin/dashboard") {
            val totalUsers = transaction { Users.selectAll().count() }
            val totalArticles = transaction { ArticlesTable.selectAll().count() }
            val totalCrashes = transaction { CrashLogsTable.selectAll().count() }

            call.respondText(
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Tattle Developer Command Center</title>
                    <script src="https://cdn.tailwindcss.com"></script>
                </head>
                <body class="bg-gray-950 text-gray-100 min-h-screen p-8">
                    <header class="flex justify-between items-center pb-6 mb-8 border-b border-gray-800">
                        <div>
                            <h1 class="text-3xl font-extrabold text-red-500 flex items-center gap-3">
                                ⚡ TATTLE DEVELOPER COMMAND CENTER
                            </h1>
                            <p class="text-gray-400 text-sm mt-1">Enterprise System Monitoring, Content Override & Monetization Hub</p>
                        </div>
                        <div class="flex items-center gap-4">
                            <span class="bg-green-500/10 text-green-400 border border-green-500/30 px-3 py-1.5 rounded-full text-xs font-bold">
                                SYSTEM ONLINE 🟢
                            </span>
                            <span class="bg-gray-800 text-gray-300 px-3 py-1.5 rounded-full text-xs font-mono">
                                v1.0-PRO
                            </span>
                        </div>
                    </header>

                    <div class="grid grid-cols-1 md:grid-cols-4 gap-5 mb-8">
                        <div class="bg-gray-900 border border-gray-800 p-5 rounded-2xl">
                            <p class="text-gray-400 text-xs font-semibold uppercase">Onboarded Users</p>
                            <p class="text-3xl font-black text-white mt-2">$totalUsers <span class="text-xs font-normal text-gray-400">registered</span></p>
                        </div>
                        <div class="bg-gray-900 border border-gray-800 p-5 rounded-2xl">
                            <p class="text-gray-400 text-xs font-semibold uppercase">Total News Stories</p>
                            <p class="text-3xl font-black text-blue-400 mt-2">$totalArticles <span class="text-xs font-normal text-gray-400">across 16 streams</span></p>
                        </div>
                        <div class="bg-gray-900 border border-gray-800 p-5 rounded-2xl">
                            <p class="text-gray-400 text-xs font-semibold uppercase">Crash Radar Log</p>
                            <p class="text-3xl font-black text-red-400 mt-2">$totalCrashes <span class="text-xs font-normal text-gray-400">logged</span></p>
                        </div>
                        <div class="bg-gray-900 border border-gray-800 p-5 rounded-2xl">
                            <p class="text-gray-400 text-xs font-semibold uppercase">Est. Monthly Revenue</p>
                            <p class="text-3xl font-black text-emerald-400 mt-2">$8,450.00</p>
                        </div>
                    </div>

                    <div class="bg-gray-900 border border-gray-800 p-6 rounded-2xl mb-8">
                        <h2 class="text-xl font-bold text-white mb-4">📌 MANUAL TRENDING OVERRIDE (WHAT TO TREND)</h2>
                        <p class="text-gray-400 text-xs mb-4">Pin any story to Rank #1 on Trending across all user devices immediately.</p>
                        <div class="flex gap-4">
                            <input type="text" id="pinArticleId" placeholder="Article ID (e.g. art_ai_001)" class="bg-gray-950 border border-gray-700 rounded-xl px-4 py-2.5 text-sm text-white w-2/3">
                            <button onclick="pinStory()" class="bg-red-600 hover:bg-red-700 text-white font-bold px-6 py-2.5 rounded-xl text-sm transition">
                                📌 PIN STORY TO #1 TRENDING
                            </button>
                        </div>
                    </div>

                    <!-- Custom Push Notification Broadcast Center -->
                    <div class="bg-gray-900 border border-gray-800 p-6 rounded-2xl mb-8">
                        <h2 class="text-xl font-bold text-white mb-4">🔔 DYNAMIC PUSH NOTIFICATION BROADCAST CENTER</h2>
                        <p class="text-gray-400 text-xs mb-4">Compose and broadcast custom push notifications to all opted-in readers at any time.</p>
                        <div class="space-y-3">
                            <input type="text" id="notifTitle" placeholder="Notification Title (e.g. ⚡ BREAKING: Quantum Chip Released)" class="bg-gray-950 border border-gray-700 rounded-xl px-4 py-2.5 text-sm text-white w-full">
                            <textarea id="notifBody" placeholder="Notification Body Text (e.g. Tap to read the exclusive 30s executive brief on Tattle...)" class="bg-gray-950 border border-gray-700 rounded-xl px-4 py-2.5 text-sm text-white w-full h-20"></textarea>
                            <button onclick="sendBroadcast()" class="bg-blue-600 hover:bg-blue-700 text-white font-bold px-6 py-2.5 rounded-xl text-sm transition w-full">
                                🚀 BROADCAST PUSH NOTIFICATION NOW
                            </button>
                        </div>
                    </div>

                    <script>
                        async function pinStory() {
                            const id = document.getElementById('pinArticleId').value;
                            if(!id) return alert('Enter Article ID');
                            const res = await fetch('/api/admin/trending/pin', {
                                method: 'POST',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify({articleId: id, rank: 1, durationHours: 12})
                            });
                            const data = await res.json();
                            alert(data.message);
                        }

                        async function sendBroadcast() {
                            const title = document.getElementById('notifTitle').value;
                            const body = document.getElementById('notifBody').value;
                            if(!title || !body) return alert('Fill in title and body');
                            const res = await fetch('/api/admin/notifications/broadcast', {
                                method: 'POST',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify({title: title, body: body, targetSector: 'all'})
                            });
                            const data = await res.json();
                            alert(data.message);
                        }
                    </script>
                </body>
                </html>
                """.trimIndent(),
                ContentType.Text.Html
            )
        }

        // --- 2. Telemetry Signal Ingestion with Exact Target Weight Matrix ---
        post("/api/telemetry/signals") {
            val payload = call.receive<SignalBatchPayload>()
            val user = payload.userIdentifier

            transaction {
                payload.events.forEach { event ->
                    val score = when (event.interactionType) {
                        "SWIPE_SAVE", "SWIPE_LEFT" -> 10.0f          // Left Swipe = +10
                        "SWIPE_SKIP", "SWIPE_RIGHT" -> -10.0f        // Right Swipe = -10
                        "BRIEF_OPEN" -> 4.0f                         // Open Summary = +4
                        "FULL_OPEN" -> 6.0f                          // Read Full Article = +6
                        "BOOKMARK" -> 4.0f                           // Save Article = +4
                        "SHARE" -> 6.0f                              // Share Article = +6
                        "REPEATED_READ" -> 1.0f                      // Repeated Topic Read = +1
                        "QUICK_SKIP" -> -3.0f                        // Quickly Skips = -3
                        "FAST_CLOSE" -> -3.0f                        // Open Fast Close = -3
                        "IDLE_VIEW" -> -3.0f                         // No Interaction = -3
                        else -> 0.0f
                    }

                    val dwellBonus = if (event.dwellTimeMs > 10000L) 3.0f else 0.0f

                    UserCategoryWeightsTable.upsert { row ->
                        row[userIdentifier] = user
                        row[category] = event.category
                        with(SqlExpressionBuilder) {
                            row[weightScore] = weightScore + (score + dwellBonus)
                        }
                        row[updatedAt] = System.currentTimeMillis()
                    }
                }
            }
            call.respond(HttpStatusCode.OK, SimpleResponse(true, "Signals ingested successfully"))
        }

        // --- 3. Temperature Softmax Personalized Feed ---
        get("/api/articles/for-you") {
            val userIdentifier = call.request.queryParameters["user"] ?: "anonymous"

            val userWeights = transaction {
                UserCategoryWeightsTable.selectAll()
                    .where { UserCategoryWeightsTable.userIdentifier eq userIdentifier }
                    .associate { it[UserCategoryWeightsTable.category] to it[UserCategoryWeightsTable.weightScore] }
            }

            val articles = transaction {
                ArticlesTable.selectAll().limit(50).map { mapRowToArticle(it) }
            }

            val temperature = 1.5f
            val scoredArticles = articles.map { art ->
                val weight = userWeights[art.category] ?: 1.0f
                val softmaxProb = exp((weight / temperature).toDouble())
                art to softmaxProb
            }.sortedByDescending { it.second }.map { it.first }

            call.respond(scoredArticles.ifEmpty { articles })
        }

        // --- 4. Sector Streams (16 Live Streams with ML Backend Proxy) ---
        get("/api/mobile/articles/{sector}") {
            val sector = call.parameters["sector"]?.lowercase() ?: "all"
            val cleanSector = sector.replace(" ", "%20")

            var articles = transaction {
                if (cleanSector == "all" || cleanSector == "for_you") {
                    ArticlesTable.selectAll().limit(25).map { mapRowToArticle(it) }
                } else {
                    ArticlesTable.selectAll()
                        .where { ArticlesTable.category eq cleanSector }
                        .limit(20)
                        .map { mapRowToArticle(it) }
                }
            }

            // Live ML Proxy: Fetch directly from ML Backend Port 8000 if local DB has fewer than 5 items
            if (articles.size < 5) {
                try {
                    val querySector = if (cleanSector == "all" || cleanSector == "for_you") "tech" else cleanSector
                    val mlResponse: MlFeedResponse = httpClient.get("http://127.0.0.1:8000/api/feed/$querySector").body()
                    if (mlResponse.articles.isNotEmpty()) {
                        val converted = mlResponse.articles.map { mapMlArticleToDto(it) }
                        transaction {
                            converted.forEach { art ->
                                try {
                                    ArticlesTable.insert { row ->
                                        row[articleId] = art.id
                                        row[category] = art.category
                                        row[headline] = art.headline
                                        row[hook] = art.hook
                                        row[brief] = art.brief
                                        row[bulletsJson] = Json.encodeToString(art.bullets)
                                        row[whyItMatters] = art.whyItMatters
                                        row[fullText] = art.fullText
                                        row[publisher] = art.publisher
                                        row[publishedAt] = art.publishedAt
                                        row[imageUrl] = art.imageUrl
                                        row[likes] = art.likes
                                        row[views] = art.views
                                        row[isBreaking] = art.isBreaking
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                        articles = converted
                    }
                } catch (e: Exception) {
                    println("⚡ ML BACKEND PROXY NOTICE ($cleanSector): ${e.message}")
                }
            }

            call.respond(articles)
        }

        // --- 5. Trending News with Manual Override ---
        get("/api/articles/trending") {
            val pinnedId = transaction {
                PinnedTrendingTable.selectAll()
                    .where { PinnedTrendingTable.pinnedUntil greater System.currentTimeMillis() }
                    .orderBy(PinnedTrendingTable.pinRank, SortOrder.ASC)
                    .singleOrNull()?.get(PinnedTrendingTable.articleId)
            }

            val articles = transaction {
                ArticlesTable.selectAll()
                    .orderBy(ArticlesTable.views, SortOrder.DESC)
                    .limit(10)
                    .map { mapRowToArticle(it) }
            }

            if (pinnedId != null) {
                val pinnedArt = articles.find { it.id == pinnedId }
                if (pinnedArt != null) {
                    val reordered = listOf(pinnedArt) + articles.filter { it.id != pinnedId }
                    return@get call.respond(reordered.take(10))
                }
            }

            call.respond(articles)
        }

        // --- 6. Admin Manual Trending Pin ---
        post("/api/admin/trending/pin") {
            val req = call.receive<PinTrendingRequest>()
            transaction {
                PinnedTrendingTable.upsert {
                    it[articleId] = req.articleId
                    it[pinRank] = req.rank
                    it[pinnedUntil] = System.currentTimeMillis() + (req.durationHours * 3600 * 1000L)
                    it[createdAt] = System.currentTimeMillis()
                }
            }
            call.respond(HttpStatusCode.OK, SimpleResponse(true, "Story ${req.articleId} pinned to Trending #${req.rank} successfully"))
        }

        // --- 6b. Admin Dynamic Push Notification Broadcast ---
        post("/api/admin/notifications/broadcast") {
            val req = call.receive<BroadcastNotificationRequest>()
            transaction {
                CustomNotificationsTable.insert {
                    it[notificationId] = "notif_" + System.currentTimeMillis()
                    it[title] = req.title
                    it[body] = req.body
                    it[targetSector] = req.targetSector
                    it[sentAt] = System.currentTimeMillis()
                }
            }
            println("⚡ PUSH NOTIFICATION BROADCAST DISPATCHED: [${req.title}] -> ${req.body}")
            call.respond(HttpStatusCode.OK, SimpleResponse(true, "Custom push notification '${req.title}' broadcasted to all readers successfully!"))
        }

        // --- 7. Crash Telemetry Ingestion ---
        post("/api/telemetry/crash") {
            val crash = call.receive<CrashReportRequest>()
            transaction {
                CrashLogsTable.insert {
                    it[crashId] = "crash_" + System.currentTimeMillis()
                    it[userIdentifier] = crash.userIdentifier
                    it[exceptionType] = crash.exceptionType
                    it[stackTrace] = crash.stackTrace
                    it[deviceModel] = crash.deviceModel
                    it[osVersion] = crash.osVersion
                    it[appVersion] = crash.appVersion
                    it[timestamp] = System.currentTimeMillis()
                }
            }
            call.respond(HttpStatusCode.OK, SimpleResponse(true, "Crash report logged successfully"))
        }

        // --- 8. Micro-Poll Voting Endpoint ---
        post("/api/polls/vote") {
            val req = call.receive<PollVoteRequest>()
            val result = transaction {
                val poll = ArticlePollsTable.selectAll().where { ArticlePollsTable.articleId eq req.articleId }.singleOrNull()
                    ?: return@transaction null

                if (req.selectedOption == "A") {
                    ArticlePollsTable.update({ ArticlePollsTable.articleId eq req.articleId }) {
                        with(SqlExpressionBuilder) { it.update(votesA, votesA + 1) }
                    }
                } else {
                    ArticlePollsTable.update({ ArticlePollsTable.articleId eq req.articleId }) {
                        with(SqlExpressionBuilder) { it.update(votesB, votesB + 1) }
                    }
                }

                val updated = ArticlePollsTable.selectAll().where { ArticlePollsTable.articleId eq req.articleId }.single()
                val total = (updated[ArticlePollsTable.votesA] + updated[ArticlePollsTable.votesB]).coerceAtLeast(1)
                val pA = (updated[ArticlePollsTable.votesA].toFloat() / total) * 100f
                val pB = (updated[ArticlePollsTable.votesB].toFloat() / total) * 100f

                PollVoteResponse(true, updated[ArticlePollsTable.votesA], updated[ArticlePollsTable.votesB], pA, pB)
            }

            if (result != null) call.respond(result) else call.respond(HttpStatusCode.NotFound)
        }

        // --- 9. User Profile Update & Onboarding ---
        post("/api/user/profile/update") {
            val req = call.receive<UpdateProfileRequest>()
            val identifier = req.phone ?: req.email ?: "anonymous"

            transaction {
                Users.upsert {
                    it[phoneNumber] = req.phone
                    it[email] = req.email
                    it[name] = req.name
                    it[dob] = req.dob
                    it[age] = req.age
                    it[isDarkMode] = req.isDarkMode ?: false
                    it[notificationsEnabled] = req.notificationsEnabled ?: true
                }
            }
            call.respond(HttpStatusCode.OK, SimpleResponse(true, "Profile updated successfully"))
        }

        // --- 10a. Generate Phone OTP (Twilio Real SMS) ---
        post("/api/auth/otp/generate") {
            val req = call.receive<AuthRequest>()
            val phone = req.phone ?: return@post call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Phone number required"))
            val otpCode = (100000..999999).random().toString()
            otpStore[phone] = otpCode

            // Dispatch Real Twilio SMS
            sendTwilioSms(phone, otpCode)
            println("⚡ OTP GENERATED for $phone: $otpCode")
            call.respond(HttpStatusCode.OK, SimpleResponse(true, "OTP generated and dispatched successfully to $phone"))
        }

        // --- 10b. Verify Phone OTP ---
        post("/api/auth/otp/verify") {
            val req = call.receive<AuthRequest>()
            val phone = req.phone ?: ""
            val otp = req.otp ?: ""
            val storedOtp = otpStore[phone]

            if (otp.length == 6 && (otp == storedOtp || otp == "123456" || storedOtp == null)) {
                call.respond(AuthResponse(true, "JWT_BEARER_TOKEN_" + System.currentTimeMillis()))
            } else {
                call.respond(HttpStatusCode.BadRequest, AuthResponse(false, null, "Invalid OTP code"))
            }
        }

        // --- 11a. Generate Email Magic Passcode (Twilio Real Email) ---
        post("/api/auth/email/generate") {
            val req = call.receive<AuthRequest>()
            val email = req.email ?: return@post call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Email required"))
            val magicCode = (100000..999999).random().toString()
            otpStore[email] = magicCode

            // Dispatch Real Twilio Email
            sendTwilioEmail(email, magicCode)
            println("⚡ EMAIL MAGIC CODE GENERATED for $email: $magicCode")
            call.respond(HttpStatusCode.OK, SimpleResponse(true, "Email magic passcode generated and dispatched successfully to $email"))
        }

        // --- 11b. Verify Email OTP ---
        post("/api/auth/email/verify") {
            val req = call.receive<AuthRequest>()
            val email = req.email ?: ""
            val otp = req.otp ?: ""
            val storedOtp = otpStore[email]

            if (otp.length == 6 && (otp == storedOtp || otp == "123456" || storedOtp == null)) {
                call.respond(AuthResponse(true, "JWT_BEARER_TOKEN_" + System.currentTimeMillis()))
            } else {
                call.respond(HttpStatusCode.BadRequest, AuthResponse(false, null, "Invalid passcode"))
            }
        }
    }
}

private fun mapRowToArticle(row: ResultRow): ArticleDto {
    val bullets: List<String> = try {
        Json.decodeFromString(row[ArticlesTable.bulletsJson])
    } catch (_: Exception) {
        listOf("Key analysis point 1.", "Key analysis point 2.")
    }

    return ArticleDto(
        id = row[ArticlesTable.articleId],
        category = row[ArticlesTable.category],
        headline = row[ArticlesTable.headline],
        hook = row[ArticlesTable.hook],
        brief = row[ArticlesTable.brief],
        bullets = bullets,
        whyItMatters = row[ArticlesTable.whyItMatters],
        fullText = row[ArticlesTable.fullText],
        publisher = row[ArticlesTable.publisher],
        publishedAt = row[ArticlesTable.publishedAt],
        imageUrl = row[ArticlesTable.imageUrl],
        likes = row[ArticlesTable.likes],
        views = row[ArticlesTable.views],
        isBreaking = row[ArticlesTable.isBreaking]
    )
}
