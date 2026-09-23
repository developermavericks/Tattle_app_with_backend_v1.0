package com.example.tattle

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.call.body
import io.ktor.client.request.get
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
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

val httpClient = HttpClient(CIO) {
    install(ClientContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

@Serializable
data class BackendImageSearchResponse(
    val success: Boolean,
    val imageUrl: String? = null,
    val source: String? = null
)

@Serializable
data class EnvatoSearchResponse(
    val matches: List<EnvatoItem> = emptyList()
)

@Serializable
data class EnvatoItem(
    val id: Long? = null,
    val name: String? = null,
    val previews: EnvatoPreviews? = null
)

@Serializable
data class EnvatoPreviews(
    val landscape_preview: EnvatoLandscapePreview? = null,
    val icon_with_square_preview: EnvatoSquarePreview? = null,
    val icon_with_landscape_preview: EnvatoLandscapePreview? = null
)

@Serializable
data class EnvatoLandscapePreview(
    val landscape_url: String? = null
)

@Serializable
data class EnvatoSquarePreview(
    val icon_url: String? = null
)

@Serializable
data class PixabayResponse(
    val total: Int = 0,
    val totalHits: Int = 0,
    val hits: List<PixabayHit> = emptyList()
)

@Serializable
data class PixabayHit(
    val id: Int = 0,
    val webformatURL: String? = null,
    val largeImageURL: String? = null,
    val tags: String? = null
)

@Serializable
data class UserProfileDto(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dob: String? = null,
    val age: Int? = null,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val language: String = "English"
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
data class ToggleBookmarkRequest(
    val userIdentifier: String,
    val articleId: String
)

@Serializable
data class ToggleBookmarkResponse(
    val success: Boolean,
    val isBookmarked: Boolean,
    val bookmarks: List<String>
)

@Serializable
data class ToggleLikeRequest(
    val userIdentifier: String,
    val articleId: String
)

@Serializable
data class ToggleLikeResponse(
    val success: Boolean,
    val isLiked: Boolean,
    val likesCount: Int
)

@Serializable
data class RecordShareRequest(
    val userIdentifier: String,
    val articleId: String,
    val platform: String = "generic"
)

@Serializable
data class BatchArticlesRequest(
    val ids: List<String>
)

@Serializable
data class SectorListResponse(
    val sectors: List<String>
)

// --- Configuration ---
object AuthConfig {
    const val SECRET = "TATTLE_LOCAL_SECRET_KEY_2026"
    const val ISSUER = "com.example.tattle"
    const val AUDIENCE = "tattle-users"
    const val GOOGLE_WEB_CLIENT_ID = "610417006948-m97qce1p5ot524tr542m1ijf19n93uou.apps.googleusercontent.com"
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
                SchemaUtils.createMissingTablesAndColumns(Users, ArticlesTable, UserBookmarksTable, UserLikesTable, UserSharesTable)
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
            println("SERVER NOTICE: Port 8081 is already in use by a running server instance. App can connect directly to http://127.0.0.1:8081.")
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
            SchemaUtils.createMissingTablesAndColumns(Users, ArticlesTable, UserBookmarksTable, UserLikesTable, UserSharesTable)
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
            fullText = """As urban centers face increasing strain on centralized cellular infrastructure, researchers and open-source developers have pioneered localized neural-mesh networks. These networks utilize edge computing devices and ambient low-power radio frequencies to create localized data webs. By embedding lightweight AI models directly into node endpoints, messages, sensor data, and emergency alerts are routed intelligently without touching external cloud servers.

For high-density metropolitan regions, the rise of neural-mesh networks presents a fundamental shift in how digital communications operate during peak congestion or physical power grid interruptions. Unlike traditional cellular towers that bottleneck during emergencies, every connected node in a neural-mesh network acts as both a transmitter and a real-time router. The system dynamically heals itself around offline nodes, ensuring that critical safety dispatches and community alerts continue flowing continuously.

Furthermore, privacy-advocates highlight that local neural inference ensures sensitive user telemetry never leaves the endpoint device. By processing data locally before routing micro-packets, these networks eliminate centralized tracking and mitigate surveillance vulnerabilities across municipal infrastructure.""",
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
            fullText = """The global technology sector reached a historic milestone today as post-quantum cryptographic standards were officially codified. With quantum computing hardware advancing rapidly, traditional RSA and ECC encryption methods face eventual vulnerability. The newly mandated lattice-based algorithms ensure that sensitive data remains uncrackable even against future fault-tolerant quantum processors.

Major financial institutions and international telecommunication carriers have officially begun rolling out firmware upgrades incorporating post-quantum public-key algorithms. Cybersecurity experts estimate that over 10 Billion digital certificates and security keys worldwide will require migration over the next three years to guarantee long-term data security.

The transition, described by engineers as the largest cryptographic overhaul in internet history, safeguards national defense grids, central bank clearing houses, and encrypted messaging channels against 'harvest now, decrypt later' adversary tactics.""",
            publisher = "CyberWire",
            publishedAt = "25m ago",
            imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5",
            likes = 890,
            commentsCount = 42,
            isBreaking = false,
            views = 5600
        ),
        ArticleDto(
            id = "art_biz_003",
            category = "money and business",
            headline = "Global Central Banks Launch Instant Cross-Border Settlement Grid",
            hook = "New unified protocol reduces multi-day international wire transfers to sub-second settlements.",
            brief = "Twenty-four central banks have integrated an interoperable settlement layer.",
            bullets = listOf(
                "Eliminates intermediate correspondent bank fees.",
                "Operates 24/7/365 with atomic liquidity verification.",
                "Expected to save global businesses $40 Billion annually in FX costs."
            ),
            whyItMatters = "Modernizes cross-border trade and democratizes global commerce for small and medium enterprises.",
            fullText = """International commerce experienced a paradigm shift today with the live launch of Project InterLink. By unifying national real-time payment rails under a standardized cryptographic ledger, cross-border remittances and trade settlements now execute in under two seconds. The system bypasses legacy messaging delays, drastically lowering transaction friction.

The multilateral network connects financial hubs across North America, Europe, Asia, and Latin America. Small businesses and multinational corporations alike can now settle foreign currency transactions instantly, eliminating multi-day settlement windows and volatile exchange rate risk during transit.

Financial analysts predict the sub-second protocol will save global supply chains upwards of $40 Billion annually in correspondent banking fees, providing significant economic tailwinds for emerging market exporters.""",
            publisher = "Financial Times Grid",
            publishedAt = "1h ago",
            imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3",
            likes = 2100,
            commentsCount = 130,
            isBreaking = true,
            views = 12500
        ),
        ArticleDto(
            id = "art_space_004",
            category = "science and space",
            headline = "Lunar Base Alpha Achieves First Closed-Loop Oxygen Production",
            hook = "Lunar regolith processing plant successfully extracts breathable oxygen continuously for 30 days.",
            brief = "Lunar industrialization reaches a crucial self-sustainability benchmark.",
            bullets = listOf(
                "Molten salt electrolysis extracts 99.8% pure oxygen directly from lunar soil.",
                "Provides life support and rocket propellant for deep space missions.",
                "Reduces Earth supply launch mass requirements by over 70%."
            ),
            whyItMatters = "Unlocks long-term human habitation on the Moon and serves as the staging ground for Mars exploration.",
            fullText = """Engineers at Lunar Base Alpha confirmed today that the surface oxygen extraction facility completed 720 hours of unbroken operation. Using solar-concentrated thermal reactors and molten salt electrolysis, the automated facility processed over 12 tons of lunar regolith, yielding clean oxygen for habitat atmospheric maintenance and cryogenic fuel synthesis.

The breakthrough demonstrates that permanent off-world habitats can extract life-sustaining resources directly from local extraterrestrial soil without relying on constant resupply launches from Earth. The processed regolith byproduct is additionally melted and extruded into high-strength structural bricks used for radiation shielding across expanding lunar living quarters.

With continuous oxygen production proven, mission directors have cleared the next operational phase: establishing cryogenic liquid oxygen storage depots to fuel outbound deep-space exploratory spacecraft heading toward Mars.""",
            publisher = "AstroChronicle",
            publishedAt = "2h ago",
            imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa",
            likes = 3450,
            commentsCount = 210,
            isBreaking = false,
            views = 18200
        ),
        ArticleDto(
            id = "art_world_005",
            category = "world news",
            headline = "Historic Global Water Treaty Signed by 140 Nations",
            hook = "Landmark international accord protects cross-border freshwater rivers and aquifers.",
            brief = "140 nations ratify the first binding treaty governing shared global freshwater reserves.",
            bullets = listOf(
                "Establishes equitable water sharing protocols for international river basins.",
                "Sets up $15B green fund for drought-resilient agricultural infrastructure.",
                "Mandates real-time satellite monitoring of major international aquifers."
            ),
            whyItMatters = "Prevents geopolitical water disputes and safeguards drinking water access for 2.5 Billion people.",
            fullText = """Delegates at the Geneva Environmental Summit voted unanimously to ratify the Global Freshwater Accord. The treaty establishes binding international law regarding cross-border water extraction, ecological flow minimums, and joint water management. Experts hail the agreement as a vital peacekeeping milestone for the 21st century.

Under the framework, member nations share real-time telemetry from satellite monitoring stations tracking reservoir depletion and river outflow rates. A dedicated $15 Billion climate resilience fund will deploy advanced drip-irrigation technology and brackish water desalinization plants across drought-vulnerable agricultural regions.

The diplomatic accord eliminates lingering friction over trans-boundary river systems, guaranteeing safe drinking water access and food security for over two billion people worldwide.""",
            publisher = "World Dispatch",
            publishedAt = "3h ago",
            imageUrl = "https://images.unsplash.com/photo-1500382017468-9049fed747ef",
            likes = 1890,
            commentsCount = 95,
            isBreaking = true,
            views = 11200
        ),
        ArticleDto(
            id = "art_climate_006",
            category = "climate and environment",
            headline = "Next-Gen Perovskite Solar Cells Exceed 35% Commercial Efficiency",
            hook = "Breakthrough silicon-perovskite tandem cells break efficiency barriers while cutting manufacturing costs.",
            brief = "Tandem photovoltaic panels double energy yield per square meter of solar installations.",
            bullets = listOf(
                "Combines silicon and perovskite layers to capture broader light spectra.",
                "Ultra-thin flexible panels can be integrated directly into building glass.",
                "Expected to lower solar electricity generation costs by 40%."
            ),
            whyItMatters = "Accelerates the global transition to renewable clean energy across residential and commercial sectors.",
            fullText = "Materials scientists have achieved a record-breaking 35.4% efficiency rating with tandem perovskite-silicon solar cells. By tuning the bandgap of perovskite micro-layers to absorb blue and ultraviolet light while silicon captures infrared wavelengths, the composite cells extract vastly more power from direct sunlight than conventional panels.",
            publisher = "EcoTech Journal",
            publishedAt = "4h ago",
            imageUrl = "https://images.unsplash.com/photo-1509391365360-2e959784a276",
            likes = 2400,
            commentsCount = 115,
            isBreaking = false,
            views = 14300
        ),
        ArticleDto(
            id = "art_geo_007",
            category = "geopolitics",
            headline = "Pacific Energy Corridor Accord Redefines Asian Trade Routes",
            hook = "Seven Pacific Rim economies establish an automated maritime logistics and energy grid.",
            brief = "A multilateral alliance launches automated deep-water shipping channels.",
            bullets = listOf(
                "Integrates autonomous container vessels with green hydrogen refueling hubs.",
                "Cuts trans-Pacific shipping times by up to 3 days.",
                "Standardizes digital customs clearing for 40% of Asian trade."
            ),
            whyItMatters = "Reshapes global supply chain resilience and reduces carbon emissions across international maritime corridors.",
            fullText = "Ministers from seven Pacific Rim nations signed the Maritime Trade Accord today, formalizing a joint high-speed shipping corridor. The initiative deploys autonomous guidance buoys, green ammonia bunkering ports, and unified digital customs clearing.",
            publisher = "Pacific Business Review",
            publishedAt = "5h ago",
            imageUrl = "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",
            likes = 1620,
            commentsCount = 74,
            isBreaking = false,
            views = 8900
        ),
        ArticleDto(
            id = "art_game_008",
            category = "gaming",
            headline = "Ultra-Low Latency Neural Audio Engines Revolutionize Immersive Gaming",
            hook = "Direct haptic audio synthesis allows players to pinpoint 3D environmental soundscapes down to millimeter precision.",
            brief = "Game developers integrate real-time acoustic wave-tracing into AAA game engines.",
            bullets = listOf(
                "Simulates sound reflection, diffraction, and absorption dynamically.",
                "Sub-1 millisecond audio latency over Bluetooth 6.0.",
                "Compatible with universal spatial audio headsets."
            ),
            whyItMatters = "Transforms spatial awareness in competitive gaming and virtual reality simulation.",
            fullText = "Game engine architects demonstrated a groundbreaking real-time acoustic wave-tracing engine today. Bypassing pre-baked audio samples, the system calculates full physical sound propagation through complex 3D environments in real-time.",
            publisher = "Polygon Pulse",
            publishedAt = "6h ago",
            imageUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc",
            likes = 2950,
            commentsCount = 180,
            isBreaking = false,
            views = 16700
        ),
        ArticleDto(
            id = "art_health_009",
            category = "healthcare",
            headline = "Universal Flu mRNA Vaccine Enters Phase III Global Trials",
            hook = "Single-dose candidate targets conserved viral stalk proteins to offer multi-decade strain immunity.",
            brief = "Clinical trials expand across 40 hospital networks to evaluate broad-spectrum seasonal protection.",
            bullets = listOf(
                "Targets invariant protein regions shared across H1N1, H3N2, and influenza B.",
                "Demonstrated 98% neutralization antibody retention in Phase II cohorts.",
                "Could replace annual flu booster shots with a single multi-year vaccine."
            ),
            whyItMatters = "Protects populations against emerging pandemic flu mutations and eliminates seasonal strain mismatches.",
            fullText = "Bio-pharmaceutical researchers announced the commencement of Phase III trials for a candidate universal influenza vaccine. Utilizing multi-valent mRNA technology, the shot triggers immune memory against core viral structures that do not mutate season to season.",
            publisher = "Lancet Biotech",
            publishedAt = "7h ago",
            imageUrl = "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982",
            likes = 3100,
            commentsCount = 220,
            isBreaking = true,
            views = 19800
        ),
        ArticleDto(
            id = "art_crypto_010",
            category = "crypto",
            headline = "Zero-Knowledge Identity Protocols Integrated into National Passports",
            hook = "Cryptographic proof systems allow citizens to verify age and citizenship without revealing personal data.",
            brief = "Three nations deploy zero-knowledge credential verification for border control and digital banking.",
            bullets = listOf(
                "Proves attributes (e.g. over 18, citizen) without disclosing birth dates or names.",
                "Prevents identity theft and centralized database leaks.",
                "Built on open-source zk-SNARK cryptographic primitives."
            ),
            whyItMatters = "Establishes privacy-first digital identity standards for global e-commerce and international travel.",
            fullText = "Borders and banking networks across three digital-first economies went live today with zero-knowledge passport verification. Citizens can now complete identity checks using cryptographic proofs generated on-device, sharing zero sensitive personal records with third-party servers.",
            publisher = "Cryptographic Security Daily",
            publishedAt = "8h ago",
            imageUrl = "https://images.unsplash.com/photo-1639762681485-074b7f938ba0",
            likes = 1750,
            commentsCount = 89,
            isBreaking = false,
            views = 10400
        ),
        ArticleDto(
            id = "art_lifestyle_011",
            category = "lifestyle",
            headline = "Circadian Smart Lighting Grids Lower Urban Burnout Ratings by 30%",
            hook = "Metropolitan wellness initiative replaces harsh municipal lighting with dynamic full-spectrum LED architecture.",
            brief = "City-wide pilot program synchronizes street and indoor lighting with natural solar biological rhythms.",
            bullets = listOf(
                "Adjusts color spectrum dynamically from 6500K daylight to 2200K evening amber.",
                "Clinical trials report a 35% improvement in sleep quality scores.",
                "Adopted across major corporate headquarters and residential complexes."
            ),
            whyItMatters = "Improves mental health and metabolic function across high-density urban populations.",
            fullText = "Municipal health authorities published findings from a two-year urban circadian lighting study today. By replacing static cold LEDs with biologically tuned dynamic lighting in office towers and public transit systems, participating cities observed marked reductions in stress biomarkers.",
            publisher = "Urban Wellness",
            publishedAt = "9h ago",
            imageUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f",
            likes = 1420,
            commentsCount = 61,
            isBreaking = false,
            views = 7800
        ),
        ArticleDto(
            id = "art_sports_012",
            category = "sports",
            headline = "Biometric Analytics AI Adopted Across Premier Football Leagues",
            hook = "Real-time muscle fatigue monitoring predicts injury risks and optimizes tactical substitutions dynamically.",
            brief = "Top-flight sports franchises deploy wearable micro-sensors to track physiological loads during matches.",
            bullets = listOf(
                "Sub-second physiological feedback flags acute fatigue before muscle tears occur.",
                "Reduces hamstring and ACL injury rates by an estimated 45%.",
                "Integrates seamlessly into match coaching tablet interfaces."
            ),
            whyItMatters = "Extends athlete careers and elevates high-performance tactical decision making in elite athletics.",
            fullText = "Sports medicine directors across European football clubs confirmed full deployment of predictive biometric analytics this season. Wearable telemetry suits transmit heart-rate variability, muscular torque, and hydration metrics in real-time.",
            publisher = "Athletic Performance Insider",
            publishedAt = "10h ago",
            imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2",
            likes = 2800,
            commentsCount = 140,
            isBreaking = false,
            views = 15200
        ),
        ArticleDto(
            id = "art_ai_013",
            category = "ai",
            headline = "Open Source 100B Model Runs Fully Offline on Smartphones",
            hook = "Quantized multimodal neural networks deliver desktop-class reasoning locally without cloud connections.",
            brief = "AI researchers release 4-bit quantized foundation models capable of offline voice, image, and text processing.",
            bullets = listOf(
                "Requires under 6GB RAM on modern mobile Neural Processing Units (NPUs).",
                "Zero data transmitted to external servers, guaranteeing total privacy.",
                "Executes complex coding, translation, and summary tasks in under 1 second."
            ),
            whyItMatters = "Democratizes state-of-the-art AI access worldwide while eliminating subscription fees and data harvesting.",
            fullText = "AI developers celebrated a massive milestone today as a high-capability 100-billion parameter quantized model was open-sourced. Optimized for mobile NPUs, the model runs completely offline on consumer hardware.",
            publisher = "AI Frontier",
            publishedAt = "11h ago",
            imageUrl = "https://images.unsplash.com/photo-1677442136019-21780efad99a",
            likes = 4200,
            commentsCount = 310,
            isBreaking = true,
            views = 24500
        ),
        ArticleDto(
            id = "art_tech_014",
            category = "tech",
            headline = "Solid-State Sodium-Ion Batteries Enter Mass Production",
            hook = "Abundant sodium chemistry replaces lithium, lowering EV battery pack costs by 60%.",
            brief = "Automotive manufacturers roll out low-cost, non-flammable sodium-ion electric vehicles.",
            bullets = listOf(
                "Uses zero scarce cobalt or nickel materials.",
                "Maintains 90% capacity retention at extreme sub-zero (-30°C) temperatures.",
                "Enables $15,000 long-range consumer electric vehicles."
            ),
            whyItMatters = "Solves battery raw material shortages and makes sustainable electric transportation universally affordable.",
            fullText = "Battery gigafactories commenced commercial production of solid-state sodium-ion battery cells today. Utilizing widely available sea salt derivative sodium, the technology eliminates expensive lithium reliance while offering total thermal safety.",
            publisher = "EV Technology World",
            publishedAt = "12h ago",
            imageUrl = "https://images.unsplash.com/photo-1558441719-67051675910d",
            likes = 3600,
            commentsCount = 240,
            isBreaking = false,
            views = 21000
        ),
        ArticleDto(
            id = "art_biz_015",
            category = "money and business",
            headline = "Autonomous Freight Fleets Approved for Commercial Highways",
            hook = "Regulators grant full driverless clearance for long-haul interstate trucking routes.",
            brief = "Self-driving logistics networks begin 24/7 autonomous freight shipping across major trade corridors.",
            bullets = listOf(
                "Platooning trucks maintain 5-meter gaps to reduce aerodynamic drag by 25%.",
                "Operates overnight without driver shift limitations, cutting transit times in half.",
                "Multi-redundant LiDAR and radar sensors ensure flawless safety records."
            ),
            whyItMatters = "Overcomes chronic truck driver shortages and drastically lowers freight shipping costs for consumer goods.",
            fullText = "Department of Transportation officials officially granted commercial operating certificates for level-4 driverless freight trucks today. Autonomous rigs are now hauling goods non-stop along 3,000 miles of interstate corridors.",
            publisher = "Logistics Today",
            publishedAt = "13h ago",
            imageUrl = "https://images.unsplash.com/photo-1601584115197-04ecc0da31d7",
            likes = 2250,
            commentsCount = 105,
            isBreaking = false,
            views = 13800
        ),
        ArticleDto(
            id = "art_space_016",
            category = "science and space",
            headline = "James Webb Telescope Detects Atmospheric Water & Methane on Exo-Earth",
            hook = "Spectroscopic signature of distant exoplanet K2-18b confirms habitability indicators.",
            brief = "Astronomers confirm atmospheric biosignatures on a habitable-zone planet 120 light-years away.",
            bullets = listOf(
                "Atmospheric transmission spectroscopy reveals carbon-based molecule concentrations.",
                "Liquid ocean signatures confirmed beneath a hydrogen-rich atmosphere.",
                "Opens new target horizon for direct high-resolution spectroscopic imaging."
            ),
            whyItMatters = "Provides strong observational evidence of potential alien life environments beyond our solar system.",
            fullText = "Astrophysicists analyzing Webb space telescope data published confirmation of atmospheric water vapor, methane, and carbon dioxide on exoplanet K2-18b today. Located 120 light-years from Earth, the Hycean ocean world orbits squarely within its red dwarf star's habitable temperature zone.",
            publisher = "Cosmic Discovery",
            publishedAt = "14h ago",
            imageUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9",
            likes = 4800,
            commentsCount = 410,
            isBreaking = true,
            views = 31000
        ),
        ArticleDto(
            id = "art_startups_017",
            category = "startups",
            headline = "Decentralized AI Compute Collective Reaches 1 Exaflop Power",
            hook = "Global peer-to-peer GPU network matches the supercomputing capacity of top national labs.",
            brief = "Over 100,000 independent GPU nodes link up to offer affordable AI model training.",
            bullets = listOf(
                "Aggregates spare consumer and enterprise GPU capacity worldwide.",
                "Reduces LLM pre-training compute expenses by 75%.",
                "Cryptographic proof-of-compute guarantees execution integrity."
            ),
            whyItMatters = "Allows bootstrapped AI startups to train mega-scale models without multi-million dollar cloud budgets.",
            fullText = "A decentralized compute protocol reached 1 Exaflop of aggregate AI processing power today. By pooling idle GPU hardware from gaming rigs, data centers, and university labs, the network enables open-source developers to execute massive distributed training jobs.",
            publisher = "Venture Byte",
            publishedAt = "15h ago",
            imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475",
            likes = 2900,
            commentsCount = 165,
            isBreaking = false,
            views = 17500
        ),
        ArticleDto(
            id = "art_edu_018",
            category = "education",
            headline = "Personalized Adaptive AI Tutors Adopted by 5,000 School Districts",
            hook = "Real-time diagnostic AI adapts learning pace to individual student comprehension levels.",
            brief = "National education trial demonstrates 2x accelerated math and literacy proficiency gains.",
            bullets = listOf(
                "Socratic dialogue engine identifies specific cognitive learning gaps instantly.",
                "Provides targeted 1-on-1 tutoring support in 30+ languages.",
                "Reduces teacher administrative grading load by 15 hours per week."
            ),
            whyItMatters = "Closes educational achievement gaps and personalizes learning for millions of K-12 students.",
            fullText = "School administrators across 5,000 public districts reported unprecedented academic gains following the deployment of adaptive AI tutors. Operating alongside classroom teachers, the interactive software analyzes student problem-solving steps in real-time.",
            publisher = "Education Reform Gazette",
            publishedAt = "16h ago",
            imageUrl = "https://images.unsplash.com/photo-1509062522246-3755977927d7",
            likes = 2100,
            commentsCount = 92,
            isBreaking = false,
            views = 11900
        ),
        ArticleDto(
            id = "art_pop_019",
            category = "pop culture",
            headline = "Synthetic Holographic Orchestras Sell Out Worldwide Arena Tours",
            hook = "AI-composed live spatial concerts blend classic symphonies with reactive visual holograms.",
            brief = "Next-generation music experience attracts 2 Million ticket buyers across major stadium venues.",
            bullets = listOf(
                "Real-time generative visuals react dynamically to acoustic cadence.",
                "Features digital avatars of historical and synthetic music legends.",
                "Pioneers high-fidelity spatial 360 audio concert engineering."
            ),
            whyItMatters = "Redefines live entertainment and demonstrates commercial viability of interactive AI musical performances.",
            fullText = "Live music promoters announced sell-out crowds across 40 global arenas for the inaugural tour of the Synthetic Philharmonic. Combining volumetric holography with AI-assisted orchestral compositions, the production delivers immersive visual and sonic experiences.",
            publisher = "Culture Wire",
            publishedAt = "17h ago",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745",
            likes = 3200,
            commentsCount = 190,
            isBreaking = false,
            views = 18600
        ),
        ArticleDto(
            id = "art_creators_020",
            category = "creator economy",
            headline = "Direct-to-Audience Micropayment Networks Surpass $1B Annual Payouts",
            hook = "Sub-cent streaming transactions allow independent creators to monetize content without ad networks.",
            brief = "Open micropayment rails enable direct per-second monetization for podcasters, writers, and streamers.",
            bullets = listOf(
                "Zero transaction fees for payments under 50 cents.",
                "Eliminates reliance on sponsor ad integrations and algorithms.",
                "3M independent creators actively earning monthly recurring income."
            ),
            whyItMatters = "Empowers independent journalists and digital creators to achieve financial autonomy directly from loyal audiences.",
            fullText = "Decentralized content monetization protocols reached $1 Billion in cumulative annual payouts to independent creators today. Utilizing sub-cent instant settlement rails, readers and viewers stream fractional micropayments continuously while consuming media.",
            publisher = "Creator Economy Insider",
            publishedAt = "18h ago",
            imageUrl = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f",
            likes = 2750,
            commentsCount = 135,
            isBreaking = false,
            views = 15800
        ),
        ArticleDto(
            id = "art_health_021",
            category = "healthcare",
            headline = "AI-Designed Enzyme Breaks Down Ocean Microplastics in 48 Hours",
            hook = "Engineered bio-catalyst transforms polyethylene waste into non-toxic organic compounds.",
            brief = "Environmental biotechnology firm scales ocean cleanup deployment across coastal estuaries.",
            bullets = listOf(
                "Protein folding models design hyper-efficient plastic-degrading enzymes.",
                "Operates safely in seawater ambient temperature ranges.",
                "Degrades microplastics without releasing harmful chemical byproducts."
            ),
            whyItMatters = "Offers a scalable biological remedy to remediate ocean plastic pollution across global marine ecosystems.",
            fullText = "Marine biologists and bio-engineers announced the successful trial of an AI-designed catalytic enzyme capable of digesting microplastics in open ocean conditions. Discovered through computational protein structure simulation, the enzyme breaks down synthetic polymers into natural organic acids.",
            publisher = "BioWorld Review",
            publishedAt = "19h ago",
            imageUrl = "https://images.unsplash.com/photo-1518837695005-2083093ee35b",
            likes = 3900,
            commentsCount = 280,
            isBreaking = true,
            views = 23200
        ),
        ArticleDto(
            id = "art_ai_022",
            category = "ai",
            headline = "Autonomous Code Synthesis AI Achieves 99% Bug-Free Benchmark",
            hook = "Formal verification AI agents construct and audit complex software enterprise systems automatically.",
            brief = "Software engineering organizations report 5x development velocity using self-correcting coding agents.",
            bullets = listOf(
                "Uses mathematical formal proof checkers to eliminate memory leaks and vulnerabilities.",
                "Refactors legacy codebases into modern high-performance Rust and Kotlin microservices.",
                "Reduces software deployment cycles from weeks to minutes."
            ),
            whyItMatters = "Revolutionizes software engineering standards and eliminates critical security vulnerabilities before production.",
            fullText = "Software architects confirmed that autonomous formal-verification coding agents passed national cybersecurity benchmarks with a 99.4% bug-free score. Bypassing probabilistic code generation, the agents generate mathematical proofs verifying every line of generated code.",
            publisher = "Developer Digest",
            publishedAt = "20h ago",
            imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c",
            likes = 4500,
            commentsCount = 380,
            isBreaking = false,
            views = 28000
        ),
        ArticleDto(
            id = "art_tech_023",
            category = "tech",
            headline = "Photonics Microchips Replace Copper Wiring in Data Center Servers",
            hook = "Light-based chip interconnects boost server communication bandwidth by 1,000x.",
            brief = "Major cloud providers begin deploying optical computing racks to slash power consumption.",
            bullets = listOf(
                "Transmits data using silicon laser beams instead of electrical copper traces.",
                "Reduces data center electricity demands by 50%.",
                "Eliminates heat throttling in massive high-density AI clusters."
            ),
            whyItMatters = "Solves server thermal limits and dramatically cuts global cloud energy consumption.",
            fullText = "Hyperscale data center operators announced commercial installation of silicon photonics server racks today. By replacing electrical copper interconnects with laser-guided optical channels, data servers process petabytes of information at the speed of light.",
            publisher = "Hardware Architecture Quarterly",
            publishedAt = "21h ago",
            imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475",
            likes = 3300,
            commentsCount = 210,
            isBreaking = false,
            views = 19500
        ),
        ArticleDto(
            id = "art_world_024",
            category = "world news",
            headline = "Global High-Speed Rail Corridor Connects 12 European Capitals",
            hook = "Zero-emission maglev corridor slashes trans-European passenger transit times.",
            brief = "500 km/h magnetic levitation network offers green alternative to regional airline travel.",
            bullets = listOf(
                "Powered 100% by dedicated solar and wind generation hubs.",
                "Connects Paris, Berlin, Vienna, and Madrid in under 3 hours.",
                "Expected to replace 60% of short-haul flights across continental Europe."
            ),
            whyItMatters = "Demonstrates sustainable continental transportation infrastructure and drastically reduces aviation carbon emissions.",
            fullText = "European transport ministers celebrated the official inauguration of the Trans-European Maglev Network today. Connecting 12 capital cities along 4,500 kilometers of high-speed guideways, passenger trains cruise silently at 500 km/h.",
            publisher = "Euro Transit Post",
            publishedAt = "22h ago",
            imageUrl = "https://images.unsplash.com/photo-1515165562839-978bbcf18277",
            likes = 2900,
            commentsCount = 175,
            isBreaking = true,
            views = 18900
        ),
        ArticleDto(
            id = "art_biz_025",
            category = "money and business",
            headline = "Decentralized Carbon Offset Exchange Reaches $50B Daily Volume",
            hook = "Transparent satellite-verified carbon credit trading eliminates greenwashing and corporate fraud.",
            brief = "Global carbon markets transition to real-time satellite verification and smart contract settlements.",
            bullets = listOf(
                "Satellite radar measures actual forest biomass density every 24 hours.",
                "Prevents double-counting and inflated carbon offset claims.",
                "Enables automated corporate carbon tax compliance."
            ),
            whyItMatters = "Brings radical transparency to global carbon markets, driving genuine investment into reforestation and clean technology.",
            fullText = "Commodity regulators confirmed that decentralized carbon credit exchanges exceeded $50 Billion in daily trading volumes. Utilizing synthetic aperture radar satellites to measure biomass growth continuously, the platform issues verified digital carbon credits.",
            publisher = "Global Finance Wire",
            publishedAt = "23h ago",
            imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa",
            likes = 2100,
            commentsCount = 98,
            isBreaking = false,
            views = 14100
        ),
        ArticleDto(
            id = "art_space_026",
            category = "science and space",
            headline = "Commercial Asteroid Mining Vessel Recovers $2B in Platinum Group Metals",
            hook = "Robotic space mining mission successfully harvests and returns high-grade asteroid metals to Earth orbit.",
            brief = "Deep-space mining drone returns 50 tons of refined rare earth metals from near-Earth asteroid 2024-PX.",
            bullets = listOf(
                "Robotic optical sorting purifies heavy metals in zero-gravity.",
                "Yields rare platinum, palladium, and neodymium required for green tech.",
                "Paves the way for off-planet resource independence."
            ),
            whyItMatters = "Shifts resource extraction from Earth's delicate biosphere to abundant asteroid belts in deep space.",
            fullText = "A commercial space mining enterprise announced the successful orbital return of its robotic extraction craft today. Harvesting regolith from a near-Earth asteroid, the automated vessel returned 50 metric tons of industrial-grade platinum group metals.",
            publisher = "New Space Frontier",
            publishedAt = "1d ago",
            imageUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa",
            likes = 4100,
            commentsCount = 330,
            isBreaking = true,
            views = 26500
        ),
        ArticleDto(
            id = "art_climate_027",
            category = "climate and environment",
            headline = "Direct Air Capture Plants Remove 10 Million Tons of CO2 Annually",
            hook = "Geothermal-powered industrial air scrubbers permanently mineralize atmospheric carbon into solid basalt rock.",
            brief = "Large-scale direct air capture facility reaches full operational capacity in Iceland.",
            bullets = listOf(
                "Pumps captured CO2 1,000 meters underground to react with basalt formations.",
                "Turns gaseous carbon into solid carbonate rock within two years.",
                "Powered 100% by localized geothermal energy."
            ),
            whyItMatters = "Provides a proven industrial pathway for negative emissions to reverse legacy atmospheric carbon levels.",
            fullText = "Environmental engineers confirmed that the Mammoth Direct Air Capture facility reached its annual target of removing 10 Million metric tons of carbon dioxide from ambient air. Powered by geothermal heat, the facility dissolves captured gas into water.",
            publisher = "Clean Energy Dispatches",
            publishedAt = "1d ago",
            imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b",
            likes = 3200,
            commentsCount = 185,
            isBreaking = false,
            views = 19100
        ),
        ArticleDto(
            id = "art_health_028",
            category = "healthcare",
            headline = "CRISPR Gene Therapy Eradicates Hereditary Blindness in Phase III Trial",
            hook = "Single targeted in-vivo gene edit restores 20/20 vision in patients with Leber Congenital Amaurosis.",
            brief = "Ophthalmology trial results confirm permanent vision restoration with zero adverse side effects.",
            bullets = listOf(
                "Delivers precise base-editing tools directly to retinal photoreceptor cells.",
                "Corrects single-point genetic mutations responsible for degenerative blindness.",
                "Paves the way for universal gene editing treatments for inherited diseases."
            ),
            whyItMatters = "Marks a historic victory for genetic medicine, offering functional cures for previously incurable genetic blindness.",
            fullText = "Clinical researchers published landmark trial findings in the Journal of Medicine today, documenting complete vision recovery across 120 patients suffering from hereditary retinal dystrophy. A single micro-injection of precise CRISPR base-editors corrected mutated retinal genes.",
            publisher = "Genomic Medicine Today",
            publishedAt = "1d ago",
            imageUrl = "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d",
            likes = 5100,
            commentsCount = 420,
            isBreaking = true,
            views = 32000
        ),
        ArticleDto(
            id = "art_geo_029",
            category = "geopolitics",
            headline = "Global AI Safety Governance Body Established at UN General Assembly",
            hook = "193 member states establish international inspection frameworks for frontier AI training clusters.",
            brief = "Unified global treaty mandates safety audits and compute caps for frontier autonomous AI models.",
            bullets = listOf(
                "Establishes international inspection teams to audit 100K+ GPU data centers.",
                "Mandates cryptographic kill-switches and alignment verification for autonomous military AI.",
                "Creates global emergency response network for digital synthetic bio-risks."
            ),
            whyItMatters = "Prevents uncontrolled AI proliferation and establishes global security guardrails for advanced autonomous systems.",
            fullText = "The United Nations General Assembly passed a historical resolution today establishing the International AI Safety Agency (IAISA). Modeled after nuclear watchdog frameworks, the treaty grants international inspectors authority to audit frontier AI training facilities.",
            publisher = "Global Affairs Quarterly",
            publishedAt = "1d ago",
            imageUrl = "https://images.unsplash.com/photo-1541872703-74c5e44368f9",
            likes = 2600,
            commentsCount = 145,
            isBreaking = false,
            views = 16300
        ),
        ArticleDto(
            id = "art_lifestyle_030",
            category = "lifestyle",
            headline = "Four-Day Work Week Mandate Adopted across 15 European Countries",
            hook = "Nationwide economic trials show 25% productivity increases and 40% reductions in worker sick leave.",
            brief = "Governments codify 32-hour work week laws without reducing employee compensation.",
            bullets = listOf(
                "32-hour work week standard implemented across public and private sectors.",
                "Corporate revenues increased by 18% due to higher employee retention and focus.",
                "Drastic reductions in workplace stress and mental health insurance claims."
            ),
            whyItMatters = "Transforms modern work-life balance and sets a new global benchmark for labor productivity and well-being.",
            fullText = "Labor ministries across 15 European nations officially enacted the 32-hour work week standard today following overwhelming success in multi-year economic trials. Participating companies reported significant boosts in employee morale, creative output, and operational efficiency.",
            publisher = "Modern Work Institute",
            publishedAt = "1d ago",
            imageUrl = "https://images.unsplash.com/photo-1497215728101-856f4ea42174",
            likes = 4900,
            commentsCount = 390,
            isBreaking = false,
            views = 29500
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
            call.respondText("Tattle Local Backend is running.")
        }

        // --- Explore Categories / Sectors ---
        get("/api/sectors") {
            val fullSectors = listOf(
                "ai", "tech", "world news", "money and business",
                "science and space", "climate and environment",
                "geopolitics", "gaming", "sports", "lifestyle",
                "healthcare", "startups", "creator economy",
                "education", "pop culture", "media and entertainment"
            )
            call.respond(SectorListResponse(fullSectors))
        }

        // --- Curated Mix Feed (For-You / Main Feed) ---
        get("/api/articles/for-you") {
            val articles = transaction {
                ArticlesTable.selectAll()
                    .orderBy(ArticlesTable.views to SortOrder.DESC)
                    .map { row ->
                        val bulletsList: List<String> = try {
                            Json.decodeFromString(row[ArticlesTable.bulletsJson])
                        } catch (_: Exception) {
                            listOf("Key analysis available in brief.")
                        }

                        ArticleDto(
                            id = row[ArticlesTable.articleId],
                            category = row[ArticlesTable.category],
                            headline = row[ArticlesTable.headline],
                            hook = row[ArticlesTable.hook],
                            brief = row[ArticlesTable.brief],
                            bullets = bulletsList,
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
            }
            call.respond(articles)
        }

        // --- Fetch Articles by Sector ---
        get("/api/mobile/articles/{sector}") {
            val sectorParam = call.parameters["sector"]?.lowercase() ?: "all"
            val articles = transaction {
                val query = if (sectorParam == "all" || sectorParam == "for_you") {
                    ArticlesTable.selectAll().orderBy(ArticlesTable.views to SortOrder.DESC)
                } else {
                    ArticlesTable.selectAll().where { ArticlesTable.category eq sectorParam }
                }

                query.map { row ->
                    val bulletsList: List<String> = try {
                        Json.decodeFromString(row[ArticlesTable.bulletsJson])
                    } catch (_: Exception) {
                        listOf("Key analysis available in brief.")
                    }

                    ArticleDto(
                        id = row[ArticlesTable.articleId],
                        category = row[ArticlesTable.category],
                        headline = row[ArticlesTable.headline],
                        hook = row[ArticlesTable.hook],
                        brief = row[ArticlesTable.brief],
                        bullets = bulletsList,
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
            }
            call.respond(articles)
        }

        // --- Trending Articles ---
        get("/api/articles/trending") {
            val articles = transaction {
                ArticlesTable.selectAll()
                    .orderBy(ArticlesTable.views to SortOrder.DESC)
                    .limit(10)
                    .map { row ->
                        val bulletsList: List<String> = try {
                            Json.decodeFromString(row[ArticlesTable.bulletsJson])
                        } catch (_: Exception) {
                            listOf("Key analysis available in brief.")
                        }

                        ArticleDto(
                            id = row[ArticlesTable.articleId],
                            category = row[ArticlesTable.category],
                            headline = row[ArticlesTable.headline],
                            hook = row[ArticlesTable.hook],
                            brief = row[ArticlesTable.brief],
                            bullets = bulletsList,
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
            }
            call.respond(articles)
        }

        // --- Fetch Single Article by ID ---
        get("/api/articles/{id}") {
            val artId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val article = transaction {
                val row = ArticlesTable.selectAll().where { ArticlesTable.articleId eq artId }.singleOrNull()
                row?.let {
                    val bulletsList: List<String> = try {
                        Json.decodeFromString(it[ArticlesTable.bulletsJson])
                    } catch (_: Exception) {
                        listOf("Key analysis available in brief.")
                    }

                    ArticleDto(
                        id = it[ArticlesTable.articleId],
                        category = it[ArticlesTable.category],
                        headline = it[ArticlesTable.headline],
                        hook = it[ArticlesTable.hook],
                        brief = it[ArticlesTable.brief],
                        bullets = bulletsList,
                        whyItMatters = it[ArticlesTable.whyItMatters],
                        fullText = it[ArticlesTable.fullText],
                        publisher = it[ArticlesTable.publisher],
                        publishedAt = it[ArticlesTable.publishedAt],
                        imageUrl = it[ArticlesTable.imageUrl],
                        likes = it[ArticlesTable.likes],
                        views = it[ArticlesTable.views],
                        isBreaking = it[ArticlesTable.isBreaking]
                    )
                }
            }

            if (article != null) {
                call.respond(article)
            } else {
                call.respond(HttpStatusCode.NotFound, SimpleResponse(false, "Article not found"))
            }
        }

        // --- Batch Articles by IDs (Saved Screen) ---
        post("/api/articles/batch") {
            val req = call.receive<BatchArticlesRequest>()
            val articles = transaction {
                ArticlesTable.selectAll().where { ArticlesTable.articleId inList req.ids }.map { row ->
                    val bulletsList: List<String> = try {
                        Json.decodeFromString(row[ArticlesTable.bulletsJson])
                    } catch (_: Exception) {
                        listOf("Key analysis available in brief.")
                    }

                    ArticleDto(
                        id = row[ArticlesTable.articleId],
                        category = row[ArticlesTable.category],
                        headline = row[ArticlesTable.headline],
                        hook = row[ArticlesTable.hook],
                        brief = row[ArticlesTable.brief],
                        bullets = bulletsList,
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
            }
            call.respond(articles)
        }

        // --- Toggle Bookmark / Saved Route ---
        post("/api/user/bookmarks/toggle") {
            val req = call.receive<ToggleBookmarkRequest>()
            val user = req.userIdentifier
            val artId = req.articleId

            val result = transaction {
                val existing = UserBookmarksTable.selectAll()
                    .where { (UserBookmarksTable.userIdentifier eq user) and (UserBookmarksTable.articleId eq artId) }
                    .singleOrNull()

                val nowBookmarked = if (existing != null) {
                    UserBookmarksTable.deleteWhere { (UserBookmarksTable.userIdentifier eq user) and (UserBookmarksTable.articleId eq artId) }
                    false
                } else {
                    UserBookmarksTable.insert {
                        it[userIdentifier] = user
                        it[articleId] = artId
                        it[createdAt] = System.currentTimeMillis()
                    }
                    true
                }

                val allBookmarks = UserBookmarksTable.selectAll()
                    .where { UserBookmarksTable.userIdentifier eq user }
                    .map { it[UserBookmarksTable.articleId] }

                ToggleBookmarkResponse(true, nowBookmarked, allBookmarks)
            }
            call.respond(result)
        }

        get("/api/user/bookmarks") {
            val user = call.request.queryParameters["userIdentifier"] ?: "default_user"
            val bookmarks = transaction {
                UserBookmarksTable.selectAll()
                    .where { UserBookmarksTable.userIdentifier eq user }
                    .map { it[UserBookmarksTable.articleId] }
            }
            call.respond(bookmarks)
        }

        // --- Toggle Like Route ---
        post("/api/user/likes/toggle") {
            val req = call.receive<ToggleLikeRequest>()
            val user = req.userIdentifier
            val artId = req.articleId

            val result = transaction {
                val existing = UserLikesTable.selectAll()
                    .where { (UserLikesTable.userIdentifier eq user) and (UserLikesTable.articleId eq artId) }
                    .singleOrNull()

                val isLiked = if (existing != null) {
                    UserLikesTable.deleteWhere { (UserLikesTable.userIdentifier eq user) and (UserLikesTable.articleId eq artId) }
                    ArticlesTable.update({ ArticlesTable.articleId eq artId }) {
                        with(SqlExpressionBuilder) {
                            it.update(likes, likes - 1)
                        }
                    }
                    false
                } else {
                    UserLikesTable.insert {
                        it[userIdentifier] = user
                        it[articleId] = artId
                        it[createdAt] = System.currentTimeMillis()
                    }
                    ArticlesTable.update({ ArticlesTable.articleId eq artId }) {
                        with(SqlExpressionBuilder) {
                            it.update(likes, likes + 1)
                        }
                    }
                    true
                }

                val updatedArticle = ArticlesTable.selectAll().where { ArticlesTable.articleId eq artId }.singleOrNull()
                val currentLikes = updatedArticle?.get(ArticlesTable.likes) ?: 0

                ToggleLikeResponse(true, isLiked, currentLikes)
            }
            call.respond(result)
        }

        get("/api/user/likes") {
            val user = call.request.queryParameters["userIdentifier"] ?: "default_user"
            val likes = transaction {
                UserLikesTable.selectAll()
                    .where { UserLikesTable.userIdentifier eq user }
                    .map { it[UserLikesTable.articleId] }
            }
            call.respond(likes)
        }

        // --- Record Share Route ---
        post("/api/user/shares/record") {
            val req = call.receive<RecordShareRequest>()
            transaction {
                UserSharesTable.insert {
                    it[userIdentifier] = req.userIdentifier
                    it[articleId] = req.articleId
                    it[platform] = req.platform
                    it[createdAt] = System.currentTimeMillis()
                }
            }
            call.respond(SimpleResponse(true, "Share recorded successfully"))
        }

        // --- Google Sign In ---
        post("/api/auth/google") {
            val req = call.receive<AuthRequest>()
            val idTokenString = req.token ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing token")
            
            val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
                .setAudience(listOf(AuthConfig.GOOGLE_WEB_CLIENT_ID))
                .build()

            val idToken = try {
                verifier.verify(idTokenString)
            } catch (e: Exception) {
                null
            }

            if (idToken != null) {
                val payload = idToken.payload
                val userId = payload.subject
                val email = payload.email
                val name = payload.get("name") as String?

                val dbId = transaction {
                    val existing = Users.selectAll().where { Users.googleId eq userId }.singleOrNull()
                    if (existing != null) {
                        existing[Users.id].value
                    } else {
                        Users.insertAndGetId {
                            it[googleId] = userId
                            it[Users.email] = email
                            it[Users.name] = name
                        }.value
                    }
                }
                
                val token = generateToken(dbId)
                call.respond(AuthResponse(true, token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, AuthResponse(false, message = "Invalid Google Token"))
            }
        }

        // --- Phone Auth ---
        post("/api/auth/otp/generate") {
            val req = call.receive<AuthRequest>()
            val phone = req.phone ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing phone")
            val otp = (100000..999999).random().toString()
            otpStore[phone] = otp
            
            println("------------------------------------")
            println("SIMULATED SMS to $phone: Your Tattle OTP is $otp")
            println("------------------------------------")
            
            call.respond(SimpleResponse(true, "OTP generated (Check server console)"))
        }

        post("/api/auth/otp/verify") {
            val req = call.receive<AuthRequest>()
            val phone = req.phone ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing phone")
            val otp = req.otp ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing otp")
            
            if (otpStore[phone] == otp) {
                otpStore.remove(phone)
                val dbId = transaction {
                    val existing = Users.selectAll().where { Users.phoneNumber eq phone }.singleOrNull()
                    if (existing != null) {
                        existing[Users.id].value
                    } else {
                        Users.insertAndGetId {
                            it[phoneNumber] = phone
                        }.value
                    }
                }
                val token = generateToken(dbId)
                call.respond(AuthResponse(true, token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, AuthResponse(false, message = "Invalid OTP"))
            }
        }

        // --- Email Auth ---
        post("/api/auth/email/generate") {
            val req = call.receive<AuthRequest>()
            val email = req.email ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing email")
            val otp = (100000..999999).random().toString()
            otpStore[email] = otp
            
            println("------------------------------------")
            println("SIMULATED EMAIL to $email: Your Tattle Login Code is $otp")
            println("------------------------------------")
            
            call.respond(SimpleResponse(true, "OTP generated (Check server console)"))
        }

        post("/api/auth/email/verify") {
            val req = call.receive<AuthRequest>()
            val email = req.email ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing email")
            val otp = req.otp ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing otp")
            
            if (otpStore[email] == otp) {
                otpStore.remove(email)
                val dbId = transaction {
                    val existing = Users.selectAll().where { Users.email eq email }.singleOrNull()
                    if (existing != null) {
                        existing[Users.id].value
                    } else {
                        Users.insertAndGetId {
                            it[Users.email] = email
                        }.value
                    }
                }
                val token = generateToken(dbId)
                call.respond(AuthResponse(true, token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, AuthResponse(false, message = "Invalid OTP"))
            }
        }

        // --- Profile Update ---
        post("/api/user/profile/update") {
            val req = call.receive<UpdateProfileRequest>()
            
            transaction {
                val queryPhone = req.phone
                val queryEmail = req.email

                val existing = if (!queryPhone.isNullOrBlank()) {
                    Users.selectAll().where { Users.phoneNumber eq queryPhone }.singleOrNull()
                } else if (!queryEmail.isNullOrBlank()) {
                    Users.selectAll().where { Users.email eq queryEmail }.singleOrNull()
                } else null

                if (existing != null) {
                    Users.update({ Users.id eq existing[Users.id] }) { row ->
                        req.name?.let { row[name] = it }
                        req.email?.let { row[email] = it }
                        req.phone?.let { row[phoneNumber] = it }
                        req.dob?.let { row[dob] = it }
                        req.age?.let { row[age] = it }
                        req.isDarkMode?.let { row[isDarkMode] = it }
                        req.notificationsEnabled?.let { row[notificationsEnabled] = it }
                        req.language?.let { row[language] = it }
                    }
                } else {
                    Users.insert { row ->
                        req.name?.let { row[name] = it }
                        req.email?.let { row[email] = it }
                        req.phone?.let { row[phoneNumber] = it }
                        req.dob?.let { row[dob] = it }
                        req.age?.let { row[age] = it }
                        req.isDarkMode?.let { row[isDarkMode] = it }
                        req.notificationsEnabled?.let { row[notificationsEnabled] = it }
                        req.language?.let { row[language] = it }
                    }
                }
            }

            call.respond(SimpleResponse(true, "Profile updated successfully"))
        }

        // --- Fetch User Profile ---
        get("/api/user/profile") {
            val phoneParam = call.request.queryParameters["phone"]
            val emailParam = call.request.queryParameters["email"]

            if (phoneParam.isNullOrBlank() && emailParam.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "phone or email parameter is required"))
                return@get
            }

            val profile = transaction {
                val row = if (!phoneParam.isNullOrBlank()) {
                    Users.selectAll().where { Users.phoneNumber eq phoneParam }.singleOrNull()
                } else {
                    Users.selectAll().where { Users.email eq emailParam }.singleOrNull()
                }

                row?.let {
                    UserProfileDto(
                        id = it[Users.id].value,
                        name = it[Users.name],
                        email = it[Users.email],
                        phone = it[Users.phoneNumber],
                        dob = it[Users.dob],
                        age = it[Users.age],
                        isDarkMode = it[Users.isDarkMode],
                        notificationsEnabled = it[Users.notificationsEnabled],
                        language = it[Users.language]
                    )
                }
            }

            if (profile != null) {
                call.respond(profile)
            } else {
                call.respond(HttpStatusCode.NotFound, SimpleResponse(false, "Profile not found"))
            }
        }

        // --- Envato Discovery Image Search Proxy Endpoint ---
        get("/api/images/search") {
            val term = call.request.queryParameters["term"]
            if (term.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, BackendImageSearchResponse(false, null, "Term parameter required"))
                return@get
            }

            val envatoToken = System.getenv("ENVATO_API_TOKEN") ?: System.getProperty("ENVATO_API_TOKEN") ?: "OP6NdHQ7vl9vb4TNaOqloZdaf0c9A2UF"
            val cleanQuery = term.take(100)

            var foundImageUrl: String? = null
            var imageSource = "none"

            // 1. Try Envato Discovery API Search on server
            if (envatoToken.isNotBlank()) {
                try {
                    val envatoRes: EnvatoSearchResponse = httpClient.get("https://api.envato.com/v1/discovery/search/search/item") {
                        parameter("site", "photodune.net")
                        parameter("term", cleanQuery)
                        header("Authorization", "Bearer $envatoToken")
                        header("User-Agent", "TattleApp/1.0 (Android; KtorServer)")
                    }.body()

                    val match = envatoRes.matches.firstOrNull()
                    val envatoUrl = match?.previews?.landscape_preview?.landscape_url
                        ?: match?.previews?.icon_with_landscape_preview?.landscape_url
                        ?: match?.previews?.icon_with_square_preview?.icon_url

                    if (!envatoUrl.isNullOrBlank()) {
                        foundImageUrl = if (envatoUrl.startsWith("//")) "https:$envatoUrl" else envatoUrl
                        imageSource = "envato"
                    }
                } catch (e: Exception) {
                    println("TATTLE_DEBUG: Server Envato search failed: ${e.message}")
                }
            }

            // 2. Fallback to Pixabay Search
            if (foundImageUrl == null) {
                try {
                    val pixabayRes: PixabayResponse = httpClient.get("https://pixabay.com/api/") {
                        parameter("key", "57288715-79ef2e54282e42425a088bb2e")
                        parameter("q", cleanQuery)
                        parameter("image_type", "photo")
                        parameter("per_page", 3)
                        parameter("safesearch", "true")
                    }.body()

                    val pixabayUrl = pixabayRes.hits.firstOrNull()?.webformatURL
                    if (!pixabayUrl.isNullOrBlank()) {
                        foundImageUrl = if (pixabayUrl.startsWith("//")) "https:$pixabayUrl" else pixabayUrl
                        imageSource = "pixabay"
                    }
                } catch (e: Exception) {
                    println("TATTLE_DEBUG: Server Pixabay search failed: ${e.message}")
                }
            }

            if (foundImageUrl != null) {
                call.respond(BackendImageSearchResponse(true, foundImageUrl, imageSource))
            } else {
                call.respond(BackendImageSearchResponse(false, null, "No image found"))
            }
        }
    }
}

fun generateToken(userId: Int): String {
    return JWT.create()
        .withAudience(AuthConfig.AUDIENCE)
        .withIssuer(AuthConfig.ISSUER)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + 604_800_000))
        .sign(Algorithm.HMAC256(AuthConfig.SECRET))
}
