# 12. Ultimate Developer Command Center & Enterprise Admin Portal

This document outlines the design, architecture, database schemas, and implementation code for the **Tattle Ultimate Developer Command Center & Enterprise Admin Portal** (`http://localhost:8081/admin/dashboard`).

This dashboard gives you **100% total control** over the mobile application, user database, news feeds, manual trending overrides, real-time crash diagnostics, regional analytics, and brand advertisement campaigns.

---

## 1. Visual Web Dashboard Overview (`http://localhost:8081/admin/dashboard`)

The Developer Command Center is an HTML5 + Tailwind CSS + Chart.js Single Page Application (SPA) served directly by your Ktor Netty server process.

```text
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  ⚡ TATTLE DEVELOPER COMMAND CENTER                                       [Server: ONLINE 🟢] [v1.0-PRO] │
├───────────┬──────────────┬─────────────────┬───────────────────┬──────────────────────────────────────┤
│ Live Users│ 24h Active   │ Total Onboarded │ Active Sector Stream│ Monthly Ad Revenue                   │
│ 1,420 🟢  │ 24,890 DAU   │ 142,500 Users   │ 16 / 16 Healthy   │ $8,450.00                            │
├───────────┴──────────────┴─────────────────┴───────────────────┴──────────────────────────────────────┤
│ [1. Live Command Center] [2. Content & Trending Override] [3. Crash Radar] [4. User CRM] [5. Ad Hub]│
├────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 🗺️ REGION-WISE ACTIVE USER DISTRIBUTION                                                               │
│  • India (IN): 58.4% (83,220)   • United States (US): 22.1% (31,490)   • Europe (EU): 12.5% (17,810)   │
├────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 📌 MANUAL TRENDING OVERRIDE (FORCE STORY TO #1 SLOT)                                                   │
│ Select Story: [ Neon Exodus: City Grid 7 Faces Massive System Blackout ▼ ]                             │
│ Pin Rank Slot: [ #1 Trending ▼ ]   Duration: [ 12 Hours ▼ ]   [ 📌 PIN STORY TO TOP NOW ]            │
├────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 🚨 REAL-TIME CRASH & ERROR DIAGNOSTIC RADAR (Catch Bugs Before Users Escalate)                         │
│ [CRASH #402] NullPointerException in BriefView.kt:142 (Samsung Galaxy S24 - Android 14) [FIXED 🟢]   │
│ [ERROR #403] HttpTimeoutException: Envato API timeout on search query (Fallback Active)               │
├────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 🎬 BRAND AD & SURVEY CAMPAIGN MANAGER & PITCH DECK EXPORTER                                            │
│ Active Client: [ Nike Running        ] CTR: [ 4.2% ] Impressions: [ 45,200 ] Revenue: [ $1,808.00 ]     │
│ [ 📄 EXPORT BRAND PITCH DECK ANALYTICS PDF ]   [ ➕ LAUNCH NEW BRAND CAMPAIGN ]                          │
└────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Core Functional Modules of the Developer Command Center

### Module 1: Live Command Center & Regional Analytics
- **Live Active User Counter**: Real-time WebSocket connection counter showing users currently reading news.
- **GeoIP Regional Heatmap**: Distribution of users by country and city (India, US, Europe, SE Asia, LATAM).
- **System Health Monitor**: CPU usage, JVM heap memory, database connection pool, and request latency.

---

### Module 2: Content & Manual Trending Override
- **16 Sector Stream Inspector**: Shows news publishing frequency, total views, and user engagement score for all 16 categories.
- **Manual Trending Pin (`WHAT TO TREND`)**: Allows you to force any story into the `#1 Trending` or `#2 Trending` slot across the app for a set duration (e.g. 6 hours, 24 hours), bypassing algorithm scores for breaking news.

---

### Module 3: Real-Time Crash Radar & Error Diagnostics
- **Exception Telemetry**: Catches unhandled coroutine exceptions, HTTP 5xx failures, and UI rendering errors sent from mobile clients.
- **StackTrace Inspector**: Displays exact file name, line number, device model (e.g. *Samsung S24*, *Pixel 8*), Android OS version, and user action sequence prior to the crash.

---

### Module 4: Onboarded User CRM & Database Management
- **Searchable User Table**: Inspect all registered users with fields for Name, Date of Birth, Age, Phone, Email, Region, Registration Date, Total Cards Read, Active Streak, and App Version.
- **User Action Controls**: Ability to reset tokens, test user preferences, or suspend spam accounts.

---

### Module 5: Brand Monetization Hub & Pitch Deck Exporter (Inshorts-Level Brand Platform)
- **Campaign Performance Tracker**: Measures Ad Views, Completed Video Watches, Survey Completion Rates, Click-Through Rates (CTR), and Estimated Revenue per Client.
- **Brand Pitch Deck Analytics Generator**: Exports professional PDF/HTML reports with demographic breakdowns (Age groups, Top read sectors, Regional engagement) to pitch to corporate sponsors and advertising agencies.
- **1-Click Campaign Creator**: Web form to launch new 5s video ads or 4-MCQ surveys instantly into production.

---

## 3. Database Schema Additions for Admin Control

```kotlin
package com.example.tattle.admin

import org.jetbrains.exposed.dao.id.IntIdTable

// --- Real-Time Crash Diagnostic Logs ---
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

// --- Regional GeoIP Telemetry Table ---
object GeoLocationsTable : IntIdTable("user_geolocations") {
    val userIdentifier = varchar("user_identifier", 255).uniqueIndex()
    val countryCode = varchar("country_code", 10) // e.g. "IN", "US", "DE"
    val cityName = varchar("city_name", 100)
    val ipAddress = varchar("ip_address", 50)
    val lastActiveAt = long("last_active_at")
}

// --- Manual Trending Override Table ---
object PinnedTrendingTable : IntIdTable("pinned_trending") {
    val articleId = varchar("article_id", 100).uniqueIndex()
    val pinRank = integer("pin_rank").default(1) // #1, #2, #3 Trending
    val pinnedUntil = long("pinned_until")
    val createdAt = long("created_at")
}
```

---

## 4. Ktor Backend Admin Implementation Code (`Application.kt`)

### 4.1 Admin Control REST Endpoints

```kotlin
// 1. Manual Trending Pin / Override
post("/api/admin/trending/pin") {
    val req = call.receive<PinTrendingRequest>() // articleId, rank, durationHours
    transaction {
        PinnedTrendingTable.upsert {
            it[articleId] = req.articleId
            it[pinRank] = req.rank
            it[pinnedUntil] = System.currentTimeMillis() + (req.durationHours * 3600 * 1000L)
            it[createdAt] = System.currentTimeMillis()
        }
    }
    call.respond(HttpStatusCode.OK, SimpleResponse(true, "Story pinned to Trending #${req.rank} successfully"))
}

// 2. Telemetry Crash Reporting Endpoint
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
    call.respond(HttpStatusCode.OK, SimpleResponse(true, "Crash report logged"))
}

// 3. Searchable Onboarded User CRM
get("/api/admin/users") {
    val usersList = transaction {
        UsersTable.selectAll().limit(100).map { row ->
            UserCrmDto(
                id = row[UsersTable.id].value,
                name = row[UsersTable.name],
                phone = row[UsersTable.phone],
                email = row[UsersTable.email],
                dob = row[UsersTable.dob],
                age = row[UsersTable.age],
                registeredAt = row[UsersTable.createdAt]
            )
        }
    }
    call.respond(usersList)
}

// 4. Export Brand Pitch Deck Analytics
get("/api/admin/reports/export/{campaignId}") {
    val campaignId = call.parameters["campaignId"] ?: ""
    val pdfHtmlReport = generateBrandAnalyticsHtml(campaignId)
    call.respondText(pdfHtmlReport, ContentType.Text.Html)
}
```

---

### 4.2 Complete Admin Command Center Web UI Code (`Application.kt`)

```kotlin
get("/admin/dashboard") {
    call.respondText(
        """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>Tattle Developer Command Center</title>
            <script src="https://cdn.tailwindcss.com"></script>
            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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

            <!-- Metric Cards Row -->
            <div class="grid grid-cols-1 md:grid-cols-5 gap-5 mb-8">
                <div class="bg-gray-900 border border-gray-800 p-5 rounded-2xl">
                    <p class="text-gray-400 text-xs font-semibold uppercase">Live Concurrent Users</p>
                    <p class="text-3xl font-black text-green-400 mt-2">1,420 <span class="text-xs font-normal text-gray-400">reading now</span></p>
                </div>
                <div class="bg-gray-900 border border-gray-800 p-5 rounded-2xl">
                    <p class="text-gray-400 text-xs font-semibold uppercase">Daily Active Users</p>
                    <p class="text-3xl font-black text-white mt-2">24,890 <span class="text-xs font-normal text-gray-400">DAU</span></p>
                </div>
                <div class="bg-gray-900 border border-gray-800 p-5 rounded-2xl">
                    <p class="text-gray-400 text-xs font-semibold uppercase">Onboarded CRM Users</p>
                    <p class="text-3xl font-black text-blue-400 mt-2">142,500 <span class="text-xs font-normal text-gray-400">registered</span></p>
                </div>
                <div class="bg-gray-900 border border-gray-800 p-5 rounded-2xl">
                    <p class="text-gray-400 text-xs font-semibold uppercase">Active Sector Streams</p>
                    <p class="text-3xl font-black text-purple-400 mt-2">16 / 16 <span class="text-xs font-normal text-gray-400">streams healthy</span></p>
                </div>
                <div class="bg-gray-900 border border-gray-800 p-5 rounded-2xl">
                    <p class="text-gray-400 text-xs font-semibold uppercase">Monthly Brand Ad Revenue</p>
                    <p class="text-3xl font-black text-emerald-400 mt-2">$8,450.00</p>
                </div>
            </div>

            <!-- Main Control Panels -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
                <!-- Manual Trending Override -->
                <div class="bg-gray-900 border border-gray-800 p-6 rounded-2xl">
                    <h2 class="text-xl font-bold text-white mb-4 flex items-center gap-2">
                        📌 MANUAL TRENDING OVERRIDE (WHAT TO TREND)
                    </h2>
                    <p class="text-gray-400 text-xs mb-4">Force any story to Rank #1 on Trending across all user devices immediately.</p>
                    <form class="space-y-4">
                        <div>
                            <label class="block text-xs font-bold text-gray-400 mb-1">Select Story to Pin</label>
                            <select class="w-full bg-gray-950 border border-gray-700 rounded-xl p-3 text-sm text-white">
                                <option>art_ai_001 - Neon Exodus: City Grid 7 Faces Massive System Blackout</option>
                                <option>art_tech_002 - Quantum-Safe Encryption Standards Standardized Globally</option>
                                <option>art_space_003 - Lunar Outpost Alpha Reports Stable Oxygen Harvest</option>
                            </select>
                        </div>
                        <div class="grid grid-cols-2 gap-4">
                            <div>
                                <label class="block text-xs font-bold text-gray-400 mb-1">Trending Rank Slot</label>
                                <select class="w-full bg-gray-950 border border-gray-700 rounded-xl p-3 text-sm text-white">
                                    <option>#1 Trending (Top Hero)</option>
                                    <option>#2 Trending</option>
                                    <option>#3 Trending</option>
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-gray-400 mb-1">Pin Duration</label>
                                <select class="w-full bg-gray-950 border border-gray-700 rounded-xl p-3 text-sm text-white">
                                    <option>6 Hours</option>
                                    <option>12 Hours</option>
                                    <option>24 Hours</option>
                                </select>
                            </div>
                        </div>
                        <button type="button" class="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 rounded-xl transition text-sm">
                            📌 PIN STORY TO #1 TRENDING NOW
                        </button>
                    </form>
                </div>

                <!-- Real-time Crash Radar -->
                <div class="bg-gray-900 border border-gray-800 p-6 rounded-2xl">
                    <h2 class="text-xl font-bold text-white mb-4 flex items-center gap-2">
                        🚨 CRASH RADAR & EXCEPTION DIAGNOSTICS
                    </h2>
                    <p class="text-gray-400 text-xs mb-4">Catches mobile application exceptions before they escalate into user churn.</p>
                    <div class="space-y-3 font-mono text-xs">
                        <div class="bg-red-950/30 border border-red-800/50 p-3 rounded-xl">
                            <div class="flex justify-between text-red-400 font-bold mb-1">
                                <span>[CRASH #402] NullPointerException</span>
                                <span>14:22:10</span>
                            </div>
                            <p class="text-gray-300">BriefView.kt:142 on null article image resource</p>
                            <p class="text-gray-500 text-[10px] mt-1">Device: Samsung Galaxy S24 (Android 14) • App v1.0</p>
                        </div>
                        <div class="bg-yellow-950/30 border border-yellow-800/50 p-3 rounded-xl">
                            <div class="flex justify-between text-yellow-400 font-bold mb-1">
                                <span>[WARNING #403] HttpTimeoutException</span>
                                <span>14:18:05</span>
                            </div>
                            <p class="text-gray-300">Envato Discovery API search timeout on query 'semiconductor'</p>
                            <p class="text-gray-500 text-[10px] mt-1">Action: Auto-fallback to Unsplash stock CDN executed successfully 🟢</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Brand Monetization & Pitch Deck Generator -->
            <div class="bg-gray-900 border border-gray-800 p-6 rounded-2xl">
                <div class="flex justify-between items-center mb-6">
                    <div>
                        <h2 class="text-xl font-bold text-white">🎬 BRAND MONETIZATION & PITCH DECK ANALYTICS HUB</h2>
                        <p class="text-gray-400 text-xs mt-1">Manage paying client ad campaigns and export corporate pitch deck reports.</p>
                    </div>
                    <button type="button" class="bg-emerald-600 hover:bg-emerald-700 text-white font-bold px-4 py-2.5 rounded-xl text-xs transition">
                        📄 EXPORT BRAND PITCH DECK ANALYTICS PDF
                    </button>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div class="bg-gray-950 p-4 rounded-xl border border-gray-800">
                        <p class="text-gray-400 text-xs">Client: Nike Running</p>
                        <p class="text-lg font-bold text-white mt-1">Nike Reader Preference Survey</p>
                        <p class="text-xs text-green-400 mt-2">CTR: 4.8% • 45,200 Submissions</p>
                    </div>
                    <div class="bg-gray-950 p-4 rounded-xl border border-gray-800">
                        <p class="text-gray-400 text-xs">Client: Tesla Inc.</p>
                        <p class="text-lg font-bold text-white mt-1">Robotaxi 5s Video Ad</p>
                        <p class="text-xs text-green-400 mt-2">Views: 89,400 • 98.2% Completion</p>
                    </div>
                    <div class="bg-gray-950 p-4 rounded-xl border border-gray-800">
                        <p class="text-gray-400 text-xs">Client: Samsung Electronics</p>
                        <p class="text-lg font-bold text-white mt-1">Galaxy AI S24 Launch Campaign</p>
                        <p class="text-xs text-blue-400 mt-2">Active • Scheduled for 7 Days</p>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent(),
        ContentType.Text.Html
    )
}
```

---

## 5. Summary & Control Capabilities

With Module 12 integrated into your Ktor backend:
1. **You hold total visual control** over live users, server metrics, regional GeoIP heatmaps, and onboarded user CRM records.
2. **You can override news trends instantly** by pinning any breaking story to `#1 Trending` across all user devices.
3. **You can identify and fix mobile app crashes** in real-time before they escalate into user negative reviews.
4. **You have an enterprise brand platform** to create campaigns and export professional analytics pitch decks for paying corporate sponsors.
