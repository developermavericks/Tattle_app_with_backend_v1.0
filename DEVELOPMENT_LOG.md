# Tattle Project - Development & Change Log

This document maintains a detailed record of all changes, updates, refactorings, and bug fixes implemented across the **Tattle** multiplatform codebase (Backend Server & Android/Multiplatform App). Use this log to track progress or revert specific changes if needed.

---

## 📋 Table of Contents
1. [Backend & Database Infrastructure](#1-backend--database-infrastructure)
2. [Splash Screen & Authentication UI](#2-splash-screen--authentication-ui)
3. [Feed Screen & Article Card Enhancements](#3-feed-screen--article-card-enhancements)
4. [Lavender Share Card Modal](#4-lavender-share-card-modal)
5. [DOB & Precise Age Calculation](#5-dob--precise-age-calculation)
6. [Explore Page Improvements](#6-explore-page-improvements)
7. [Trending Page Layout Refinement](#7-trending-page-layout-refinement)
8. [Settings Screen Cleanup & Navigation Fixes](#8-settings-screen-cleanup--navigation-fixes)
9. [Dark Theme System Consistency](#9-dark-theme-system-consistency)

---

## 1. Backend & Database Infrastructure
* **File Modified**: `server/src/main/kotlin/com/example/tattle/Application.kt`
* **Changes Implemented**:
  - Added TCP socket reachability pre-check `isPortReachable("localhost", 5433)` before attempting PostgreSQL JDBC connection.
  - Prevents noisy `PSQLException` and `ConnectException` stack traces when Docker PostgreSQL container (`tattle-db`) is offline.
  - Automatically falls back to local SQLite (`tattle.db`) if PostgreSQL port `5433` is closed.
  - Handled `BindException` when port `8081` is already occupied by a running server instance, displaying a clean warning message instead of a crash stack trace.
* **Environment Files**:
  - `local.properties`: Added `sdk.dir` definition.
  - `gradle.properties`: Added `kotlin.native.ignoreDisabledTargets=true` to suppress iOS target warnings on Windows hosts.

---

## 2. Splash Screen & Authentication UI
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SplashScreen.kt`
* **Changes Implemented**:
  - Reverted to official Tattle logo image (`Res.drawable.logo`) and world map background (`Res.drawable.world_map` with `ContentScale.FillWidth`).
  - Configured all 3 action buttons (**Continue with Google**, **Continue with Phone Number**, and **Continue with Email**) with brand red background (`Primary = Color(0xFFE14344)`), rounded shape (`28.dp`), and white text/icons.
  - Uniform OTP Input Boxes: Updated `PhoneLoginOverlay` and `EmailLoginOverlay` OTP boxes to use equal weights (`weight(1f)`), ensuring all 6 OTP boxes have uniform width across the dialog.
  - Added dark theme compatibility so text and buttons remain high-contrast in both light and dark modes.
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/App.kt`
  - Added a 1.2-second splash screen display duration on startup (`delay(1200)`) so the branded splash screen shows smoothly before auto-redirecting to the feed.

---

## 3. Feed Screen & Article Card Enhancements
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/FeedScreen.kt`
  - Updated top header bar (`FeedHeader`) to display the official **Tattle logo image** (`Res.drawable.logo`) instead of text.
  - Added theme tinting (`ColorFilter.tint`) to adapt the logo seamlessly between light mode (black) and dark mode (white).
  - Removed category filter tags row (`TabsRow`) below the top bar for a cleaner feed layout.
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/ui/components/ArticleCard.kt`
  - Removed top overlay badges ("Trending" & Category Name tag) covering the top-left section of the feed card image.
  - Lowered headline font size from `24.sp` to `19.sp` (`lineHeight = 24.sp`) for better card layout and readability.
  - Added dark theme card background and text color adaptability.

---

## 4. Lavender Share Card Modal
* **File Created**: `app/shared/src/commonMain/kotlin/com/example/tattle/ui/components/ShareCardDialog.kt`
  - Created a modal dialog with a soft **lavender gradient background** (`#E9D5FF` ➔ `#F3E8FF` ➔ `#DDD6FE`).
  - Displays Tattle branding, article image thumbnail, news headline title, summary hook, agency info (`Publisher • Time`), and a **Share Story** action button.
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/App.kt`
  - Wired `ShareCardDialog` to open whenever the Share button is clicked on `ArticleCard`, `BriefView`, or `FullView`.

---

## 5. DOB & Dynamic Precise Age Calculation
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/OnboardingScreen.kt`
  - Created `DetailedAge` data class and `calculateDetailedAgeFromDob(dobString: String)` function.
  - Dynamically calculates precise age down to **Years, Months, and Days** (e.g. `"21 years, 10 months, 30 days old"`).
  - Integrated 12:01 AM midnight rollover cutoff logic: the age value holds steady throughout the day and evaluates the new calendar day's age precisely at 12:01 AM every night.
* **Files Updated**:
  - `OnboardingScreen.kt` (DOB step displays dynamic detailed age string).
  - `SettingsScreen.kt` (Profile DOB displays dynamic detailed age string).

---

## 6. Explore Page Improvements
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/ExploreScreen.kt`
  - Removed top search text bar ("Search stories..."), leaving a clean, unobstructed 3-column grid of categories/sectors.
  - Added dark theme background and card support (`DarkBackground` and `DarkSurface`).

---

## 7. Trending Page Layout Refinement
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/TrendingScreen.kt`
  - Redesigned trending items into horizontal cards.
  - Card layout: Rank badge (`#1`, `#2`...), Category tag, Headline title, Publisher & Time, and a small square thumbnail image (`72.dp`) on the right.
  - Added dark theme card background (`DarkSurface`) and text color adaptability.

---

## 8. Settings Screen Cleanup & Navigation Fixes
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SettingsScreen.kt`
  - Fixed Push Notifications layout overflow: Applied `weight(1f)` and explicit two-line text wrapping (`"Enable or disable all breaking alerts &\ndaily briefings"`, `maxLines = 2`, `lineHeight = 16.sp`) to prevent the toggle switch from being pushed off the screen.
  - Redesigned "Edit Profile" card input fields with clean white container backgrounds in light mode (`Color.White`) and dark surface in dark mode (`DarkSurface`).
  - Integrated detailed DOB age calculation.
  - Added bottom scroll padding (`Spacer(modifier = Modifier.height(100.dp))`) so bottom fields, logout button, and version text are never overlapped by the bottom navigation bar.

---

## 9. Dark Theme System Consistency
* **Files Modified**: `App.kt`, `FeedScreen.kt`, `ArticleCard.kt`, `BriefView.kt`, `FullView.kt`, `TrendingScreen.kt`, `ExploreScreen.kt`, `SavedScreen.kt`, `SettingsScreen.kt`
* **Changes Implemented**:
  - Added dark mode support to `BriefView.kt` sheet container, headline text, summary text, takeaway bullet cards (`DarkSurfaceDim`), and Why It Matters card.
  - Added dark mode support to `FullView.kt` scaffold, top bar, headline, body text, and takeaway cards.
  - Passed `isDark = preferences?.isDarkMode == true` down to `BriefView` and `FullView` overlays in `App.kt`.
  - Standardized color mappings across all screens.

---

## 10. Production-Grade Audit & Reliability Enhancements
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/data/ArticleRepository.kt`
  - Replaced thread-unsafe map mutations with a `Mutex` lock to guarantee safe concurrent coroutine access.
  - Seeded random metric generators with `articleId.hashCode()` so likes, comments, and views remain stable and deterministic across feed refreshes.
  - Optimized `getForYouArticles` and `getTrendingArticles` to fetch categories in parallel using `coroutineScope` and `async`/`awaitAll`, removing network latency bottlenecks.
* **File Modified**: `app/shared/src/commonMain/kotlin/com/example/tattle/ui/components/ArticleCard.kt`
  - Increased touch drag threshold from 5% to 20% to eliminate accidental card swipes from micro-touches during vertical scrolling.
* **File Modified**: `server/src/main/kotlin/com/example/tattle/Application.kt`
  - Secured `/api/user/profile` endpoint to reject unauthenticated parameterless requests and prevent arbitrary user profile data leaks.

---

## 🔄 Rollback Instructions

If you ever need to revert a specific feature to a previous state:

1. **Revert Share Card Modal**: Remove `ShareCardDialog.kt` and revert `onShareArticle` in `App.kt` to call `shareArticleContent(article)` directly.
2. **Revert Splash Screen Buttons**: Open `SplashScreen.kt` and modify button `colors` or layout in the `Column` block under `SplashScreen`.
3. **Revert Category Tags / Headline Font in Feed**: Open `ArticleCard.kt` and re-add the `Overlay Tags` `Row` block or increase `fontSize = 24.sp`.
4. **Revert Category Filter Bar**: Open `FeedScreen.kt` and re-add `TabsRow` in the `topBar` `Column`.
5. **Revert Detailed Age**: In `OnboardingScreen.kt` / `SettingsScreen.kt`, switch back to `calculateAgeFromDob`.

### ⚡ Auto-Tracked Changes (2026-09-03 10:20:28)
* **Branch**: `ITS_DIVS`
* **Status**:
```
A  DEVELOPMENT_LOG.md
 M app/shared/src/commonMain/kotlin/com/example/tattle/App.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/data/LoginRepository.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/data/PreferencesRepository.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/models/Article.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/components/ArticleCard.kt
A  app/shared/src/commonMain/kotlin/com/example/tattle/ui/components/ShareCardDialog.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/ExploreScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/FeedScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/OnboardingScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SavedScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SettingsScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SplashScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/TrendingScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/theme/Theme.kt
 M build.gradle.kts
 M gradle.properties
 M gradle/wrapper/gradle-wrapper.properties
 M server/src/main/kotlin/com/example/tattle/Application.kt
 M server/src/test/kotlin/com/example/tattle/ApplicationTest.kt
?? server/tattle.db
```


### ⚡ Auto-Tracked Changes (2026-09-03 10:22:24)
* **Branch**: `ITS_DIVS`
* **Status**:
```
AM DEVELOPMENT_LOG.md
 M app/shared/src/commonMain/kotlin/com/example/tattle/App.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/data/LoginRepository.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/data/PreferencesRepository.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/models/Article.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/components/ArticleCard.kt
A  app/shared/src/commonMain/kotlin/com/example/tattle/ui/components/ShareCardDialog.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/ExploreScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/FeedScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/OnboardingScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SavedScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SettingsScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SplashScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/TrendingScreen.kt
 M app/shared/src/commonMain/kotlin/com/example/tattle/ui/theme/Theme.kt
 M build.gradle.kts
 M gradle.properties
 M gradle/wrapper/gradle-wrapper.properties
 M server/src/main/kotlin/com/example/tattle/Application.kt
 M server/src/test/kotlin/com/example/tattle/ApplicationTest.kt
?? server/tattle.db
```


### ⚡ Auto-Tracked Changes [2026-09-03 10:32:53]
* **Branch**: `ITS_DIVS`
* **Agent Note**: Created standalone Change Tracker Agent in codebase

**Modified Files**:
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/App.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/data/LoginRepository.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/data/PreferencesRepository.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/models/Article.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/ui/components/ArticleCard.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/ExploreScreen.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/FeedScreen.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/OnboardingScreen.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SavedScreen.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SettingsScreen.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/SplashScreen.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/ui/screens/TrendingScreen.kt`
- 📝 `app/shared/src/commonMain/kotlin/com/example/tattle/ui/theme/Theme.kt`
- 📝 `build.gradle.kts`
- 📝 `gradle.properties`
- 📝 `gradle/wrapper/gradle-wrapper.properties`
- 📝 `server/src/main/kotlin/com/example/tattle/Application.kt`
- 📝 `server/src/test/kotlin/com/example/tattle/ApplicationTest.kt`

**New / Staged Files**:
- ✨ `DEVELOPMENT_LOG.md`
- ✨ `agent/change_tracker_agent.ps1`
- ✨ `agent/change_tracker_agent.py`
- ✨ `app/shared/src/commonMain/kotlin/com/example/tattle/ui/components/ShareCardDialog.kt`
- ✨ `server/tattle.db`

**Diff Summary**:
```
DEVELOPMENT_LOG.md                                 |  56 ++
 .../commonMain/kotlin/com/example/tattle/App.kt    |  95 ++--
 .../com/example/tattle/data/LoginRepository.kt     |  96 +++-
 .../example/tattle/data/PreferencesRepository.kt   |   7 +-
 .../kotlin/com/example/tattle/models/Article.kt    |   9 +-
 .../example/tattle/ui/components/ArticleCard.kt    |  67 +--
 .../com/example/tattle/ui/screens/ExploreScreen.kt | 196 +++----
 .../com/example/tattle/ui/screens/FeedScreen.kt    |  53 +-
 .../example/tattle/ui/screens/OnboardingScreen.kt  | 442 +++++----------
 .../com/example/tattle/ui/screens/SavedScreen.kt   |  32 +-
 .../example/tattle/ui/screens/SettingsScreen.kt    | 628 ++++++++++++++-------
 .../com/example/tattle/ui/screens/SplashScreen.kt  | 187 ++++--
 .../example/tattle/ui/screens/TrendingScreen.kt    | 156 +++--
 .../kotlin/com/example/tattle/ui/theme/Theme.kt    |  24 +-
 build.gradle.kts                                   |   2 +-
 gradle.properties                                  |   3 +-
 gradle/wrapper/gradle-wrapper.properties           |   8 +-
 .../main/kotlin/com/example/tattle/Application.kt  | 228 ++++++--
 .../kotlin/com/example/tattle/ApplicationTest.kt   |   2 +-
 19 files changed, 1366 insertions(+), 925 deletions(-)
```

--------------------------------------------------
