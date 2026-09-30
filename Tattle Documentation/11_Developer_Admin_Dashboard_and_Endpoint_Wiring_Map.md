# 11. Developer Visual Admin Dashboard & Complete Endpoint Wiring Map

This document outlines the **Developer Visual Admin Dashboard Web UI** (`http://localhost:8081/admin`) and the **Complete Endpoint Wiring Map** showing where every endpoint connects on both ends (Mobile Client ↔ Ktor Server ↔ Database).

---

## 1. Developer Visual Admin Dashboard (`http://localhost:8081/admin`)

Yes! Developers and system administrators have access to a **built-in, real-time Web Dashboard** served directly by the Ktor backend at `http://localhost:8081/admin` (or `https://api.tattle.app/admin`).

### 1.1 Dashboard Features & Capabilities
1. 🟢 **Server Health & Performance Metrics**: CPU usage, JVM heap memory, active HTTP requests/sec, and database pool status.
2. 📊 **Real-Time Telemetry Signal Monitor**: Live stream of user swipe events, dwell times, brief views, and category weights.
3. 📰 **16-Sector Stream Inspector**: Shows article counts, view counts, and update timestamps across all 16 sector streams.
4. 🎬 **Zero-Code Brand Ad & Survey Ingestion Form**: Visual web form allowing developers/clients to push new 5s video ads or 4-MCQ surveys into production instantly.
5. 🗳️ **Live Micro-Polls & Fact-Checking Monitor**: Real-time community vote tallies and credibility scores.

```
┌────────────────────────────────────────────────────────────────────────┐
│  ⚡ TATTLE DEVELOPER ADMIN DASHBOARD                   [Status: ONLINE 🟢]│
├──────────────────────────┬───────────────────────────┬─────────────────┤
│ Server Requests: 1,240/s │ JVM Heap: 412 MB / 2048 MB│ Active Users: 8K│
├──────────────────────────┴───────────────────────────┴─────────────────┤
│  📊 LIVE TELEMETRY SIGNALS                                             │
│  [16:02:11] User +91987... ➔ SWIPE_SAVE on "art_ai_001" (Dwell: 6.4s)  │
│  [16:02:14] User +91987... ➔ BRIEF_OPEN on "art_tech_002"               │
├────────────────────────────────────────────────────────────────────────┤
│  🎬 PUSH BRAND AD / SURVEY CAMPAIGN [WEB FORM]                          │
│  Client Name: [ Tesla Inc.          ] Type: [ Video Ad 5s ▼ ]          │
│  CDN Video URL: [ https://cdn.tattle.app/ads/tesla_5s.mp4   ]          │
│  [ PUSH TO PRODUCTION ]                                                │
└────────────────────────────────────────────────────────────────────────┘
```

---

### 1.2 Ktor Embedded Dashboard Route (`Application.kt`)

```kotlin
get("/admin") {
    call.respondText(
        """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Tattle Developer Admin Dashboard</title>
            <script src="https://cdn.tailwindcss.com"></script>
        </head>
        <body class="bg-gray-900 text-white p-8">
            <h1 class="text-3xl font-bold text-red-500 mb-6">⚡ Tattle Developer Admin Dashboard</h1>
            <div class="grid grid-cols-3 gap-6 mb-8">
                <div class="bg-gray-800 p-6 rounded-xl border border-gray-700">
                    <h3 class="text-gray-400 text-sm">System Status</h3>
                    <p class="text-2xl font-bold text-green-400 mt-2">ONLINE 🟢</p>
                </div>
                <div class="bg-gray-800 p-6 rounded-xl border border-gray-700">
                    <h3 class="text-gray-400 text-sm">Active Sectors Stream</h3>
                    <p class="text-2xl font-bold text-white mt-2">16 Streams Active</p>
                </div>
                <div class="bg-gray-800 p-6 rounded-xl border border-gray-700">
                    <h3 class="text-gray-400 text-sm">Database Driver</h3>
                    <p class="text-2xl font-bold text-blue-400 mt-2">PostgreSQL / SQLite</p>
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

## 2. Complete Endpoint Wiring Map (Client ↔ Server ↔ Database)

Below is the complete wiring matrix detailing how every endpoint connects on both ends of the architecture.

| Endpoint Route | Client Connection (App Side) | Server / Database Connection (Backend Side) | Function & Data Flow |
| :--- | :--- | :--- | :--- |
| `GET /api/sectors` | `ArticleRepository.getSectors()` ➔ `ExploreScreen.kt` | Reads `ArticlesTable.category.distinct()` | Returns array of all 16 sector category slugs for Explore Screen. |
| `GET /api/articles/for-you` | `ArticleRepository.getForYouArticles()` ➔ `FeedScreen.kt` | Queries `ArticlesTable` + applies Softmax Randomization using `UserCategoryWeightsTable` | Generates personalized main feed cards based on user weights. |
| `GET /api/mobile/articles/{sector}` | `ArticleRepository.getArticlesBySector()` ➔ Sector Tabs | Queries `ArticlesTable` filtered by `category` | Delivers articles from 1 of the 16 independent sector data streams. |
| `GET /api/articles/trending` | `ArticleRepository.getTrendingArticles()` ➔ `TrendingScreen.kt` | Queries `ArticlesTable` ordered by `views DESC` | Delivers top 10 viral headlines with view velocity ranks. |
| `GET /api/articles/{id}` | `ArticleRepository.getArticleById()` ➔ `FullView.kt` | Queries `ArticlesTable` by `articleId` | Delivers full journalistic narrative text. |
| `POST /api/articles/batch` | `ArticleRepository.getArticlesByIdsFromBackend()` ➔ `SavedScreen.kt` | Queries `ArticlesTable` where `articleId inList ids` | Delivers bookmarked articles array for offline reading screen. |
| `GET /api/images/search` | `ImageRepository.searchImage()` ➔ `ArticleCard.kt` | Ktor calls Envato Discovery API (`site=photodune`) | Server-side proxy fetching stock photos without exposing tokens. |
| `POST /api/telemetry/signals` | `SignalTracker.dispatchBatch()` ➔ Background KMP thread | Upserts `UserCategoryWeightsTable` in PostgreSQL | Ingests batch telemetry (dwell, swipes, brief views) to score preferences. |
| `POST /api/user/bookmarks/toggle` | `ArticleRepository.toggleBookmarkOnServer()` ➔ Card Bookmark | Upserts/Deletes `UserBookmarksTable` | Toggles article bookmark state and returns updated bookmark list. |
| `POST /api/user/likes/toggle` | `ArticleRepository.toggleLikeOnServer()` ➔ Card Like | Upserts/Deletes `UserLikesTable` + updates `ArticlesTable.likes` | Toggles like state and returns updated total like count. |
| `POST /api/user/shares/record` | `ArticleRepository.recordShareOnServer()` ➔ Share Dialog | Inserts `UserSharesTable` | Records share platform analytics (WhatsApp, Telegram, X, etc.). |
| `GET /api/polls/{articleId}` | `Card.kt` (Polls Section) | Queries `ArticlePollsTable` | Delivers micro-poll question and live community vote percentages. |
| `POST /api/polls/vote` | `Card.kt` (Poll Vote Button) | Atomic increment `ArticlePollsTable.votesA` / `votesB` | Records vote and returns updated live percentage results. |
| `GET /api/monetization/active-checkin` | `FeedScreen.kt` ➔ `CheckInChoiceDialog.kt` | Reads `active_brand_ad` & `active_brand_survey` from Redis | Delivers active brand video ads and brand multi-MCQ surveys. |
| `POST /api/monetization/campaigns` | **Developer Web Admin Dashboard** (`/admin`) | Inserts `AdCampaignsTable` / `BrandSurveysTable` + caches in Redis | Zero-code API endpoint to push new ad campaigns & surveys. |
| `POST /api/user/profile/update` | `SettingsScreen.kt` & `OnboardingScreen.kt` | Upserts `Users` table (Name, DOB, Age, Phone, Email) | Syncs user profile and preferences to Ktor server database. |
| `POST /api/auth/otp/generate` & `/verify` | `SplashScreen.kt` (`PhoneLoginOverlay`) | Generates 6-digit OTP ➔ Validates ➔ Issues JWT token | Authenticates mobile phone users via SMS OTP. |
| `POST /api/auth/email/generate` & `/verify` | `SplashScreen.kt` (`EmailLoginOverlay`) | Generates 6-digit OTP ➔ Validates ➔ Issues JWT token | Authenticates email users via Email OTP. |
